package net.ok.project_cradle.server.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

/** Shared harvest timers, saved as a level attachment and measured in that level's game time. */
public final class AuraHarvestCooldowns implements ValueIOSerializable {
    public static final int BLOCK_COOLDOWN_TICKS = 600;
    public static final int ENTITY_COOLDOWN_TICKS = 600;

    private static final int MAX_ENTRIES_PER_KIND = 8192;
    private static final int PRUNE_INTERVAL_TICKS = 20;
    private static final Codec<BlockCooldown> BLOCK_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf("position").forGetter(BlockCooldown::position),
            Codec.LONG.fieldOf("expiresAt").forGetter(BlockCooldown::expiresAt)
    ).apply(instance, BlockCooldown::new));
    private static final Codec<EntityCooldown> ENTITY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("uuid").forGetter(EntityCooldown::uuid),
            Codec.LONG.fieldOf("expiresAt").forGetter(EntityCooldown::expiresAt)
    ).apply(instance, EntityCooldown::new));

    private final Map<Long, Long> blocks = new HashMap<>();
    private final Map<UUID, Long> entities = new HashMap<>();
    private long nextBlockExpiration = Long.MAX_VALUE;
    private long nextEntityExpiration = Long.MAX_VALUE;
    private long lastPruneTime = -1L;
    private long lastObservedTime = -1L;
    // Preserve protection even if an oversized save contains more entries than the bounded maps hold.
    private long blockOverflowUntil;
    private long entityOverflowUntil;

    public boolean ready(BlockPos position, long now) {
        return remaining(position, now) == 0L;
    }

    public long remaining(BlockPos position, long now) {
        long key = position.asLong();
        if (!validTime(now)) return BLOCK_COOLDOWN_TICKS;
        prepare(now);
        return remaining(this.blocks, key, now, this.nextBlockExpiration, this.blockOverflowUntil);
    }

    /** Starts a timer only when this position can be harvested; replacing its block never clears it. */
    public void consume(BlockPos position, long now) {
        if (!ready(position, now)) return;
        long expiresAt = now + BLOCK_COOLDOWN_TICKS;
        this.blocks.put(position.asLong(), expiresAt);
        this.nextBlockExpiration = Math.min(this.nextBlockExpiration, expiresAt);
    }

    public boolean ready(UUID uuid, long now) {
        return remaining(uuid, now) == 0L;
    }

    public long remaining(UUID uuid, long now) {
        Objects.requireNonNull(uuid, "uuid");
        if (!validTime(now)) return ENTITY_COOLDOWN_TICKS;
        prepare(now);
        return remaining(this.entities, uuid, now, this.nextEntityExpiration, this.entityOverflowUntil);
    }

    /** Starts a timer for this living object's identity, shared by every player in this level. */
    public void consume(UUID uuid, long now) {
        if (!ready(uuid, now)) return;
        long expiresAt = now + ENTITY_COOLDOWN_TICKS;
        this.entities.put(uuid, expiresAt);
        this.nextEntityExpiration = Math.min(this.nextEntityExpiration, expiresAt);
    }

    /** Removes expired timers only; active block positions survive block removal and replacement. */
    public void prune(long now) {
        if (now < 0L) return;
        this.blocks.values().removeIf(expiresAt -> expiresAt <= now);
        this.entities.values().removeIf(expiresAt -> expiresAt <= now);
        this.nextBlockExpiration = earliestExpiration(this.blocks);
        this.nextEntityExpiration = earliestExpiration(this.entities);
        if (this.blockOverflowUntil <= now) this.blockOverflowUntil = 0L;
        if (this.entityOverflowUntil <= now) this.entityOverflowUntil = 0L;
        this.lastPruneTime = now;
        this.lastObservedTime = now;
    }

    private void prepare(long now) {
        this.lastObservedTime = now;
        if (this.lastPruneTime < 0L || now < this.lastPruneTime
                || now - this.lastPruneTime >= PRUNE_INTERVAL_TICKS
                || now >= this.nextBlockExpiration || now >= this.nextEntityExpiration) {
            prune(now);
        }
    }

    private static boolean validTime(long now) {
        // Reject a clock value that cannot represent a complete timer without overflowing.
        return now >= 0L && now <= Long.MAX_VALUE - Math.max(BLOCK_COOLDOWN_TICKS, ENTITY_COOLDOWN_TICKS);
    }

    private static <K> long remaining(Map<K, Long> timers, K key, long now,
            long nextExpiration, long overflowUntil) {
        Long expiresAt = timers.get(key);
        if (expiresAt != null) return Math.max(0L, expiresAt - now);
        if (overflowUntil > now) return overflowUntil - now;
        // Never evict an active timer to admit a new object: a full map must reject that harvest.
        if (timers.size() >= MAX_ENTRIES_PER_KIND) return Math.max(1L, nextExpiration - now);
        return 0L;
    }

    private static long earliestExpiration(Map<?, Long> timers) {
        long earliest = Long.MAX_VALUE;
        for (long expiresAt : timers.values()) earliest = Math.min(earliest, expiresAt);
        return earliest;
    }

    @Override
    public void serialize(ValueOutput output) {
        if (this.lastObservedTime >= 0L) prune(this.lastObservedTime);
        var blockEntries = output.list("blocks", BLOCK_CODEC);
        this.blocks.forEach((position, expiresAt) -> blockEntries.add(new BlockCooldown(position, expiresAt)));
        var entityEntries = output.list("entities", ENTITY_CODEC);
        this.entities.forEach((uuid, expiresAt) -> entityEntries.add(new EntityCooldown(uuid, expiresAt)));
        output.putLong("blockOverflowUntil", this.blockOverflowUntil);
        output.putLong("entityOverflowUntil", this.entityOverflowUntil);
    }

    @Override
    public void deserialize(ValueInput input) {
        this.blocks.clear();
        this.entities.clear();
        this.lastPruneTime = -1L;
        this.lastObservedTime = -1L;
        this.blockOverflowUntil = Math.max(0L, input.getLongOr("blockOverflowUntil", 0L));
        this.entityOverflowUntil = Math.max(0L, input.getLongOr("entityOverflowUntil", 0L));
        for (BlockCooldown entry : input.listOrEmpty("blocks", BLOCK_CODEC)) {
            if (entry.expiresAt() <= 0L) continue;
            if (this.blocks.containsKey(entry.position()) || this.blocks.size() < MAX_ENTRIES_PER_KIND) {
                this.blocks.merge(entry.position(), entry.expiresAt(), Long::max);
            } else {
                this.blockOverflowUntil = Math.max(this.blockOverflowUntil, entry.expiresAt());
            }
        }
        for (EntityCooldown entry : input.listOrEmpty("entities", ENTITY_CODEC)) {
            if (entry.expiresAt() <= 0L) continue;
            if (this.entities.containsKey(entry.uuid()) || this.entities.size() < MAX_ENTRIES_PER_KIND) {
                this.entities.merge(entry.uuid(), entry.expiresAt(), Long::max);
            } else {
                this.entityOverflowUntil = Math.max(this.entityOverflowUntil, entry.expiresAt());
            }
        }
        this.nextBlockExpiration = earliestExpiration(this.blocks);
        this.nextEntityExpiration = earliestExpiration(this.entities);
    }

    private record BlockCooldown(long position, long expiresAt) {}

    private record EntityCooldown(UUID uuid, long expiresAt) {}
}
