package net.ok.project_cradle.server.progression;

import java.util.ArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

/** A brief attack restriction for one pacified victim and each protected caster independently. */
public final class DreamPacificationService {
    private static final String PACIFICATION_KEY = "project_cradle.dream_pacification";

    private DreamPacificationService() {
    }

    public static void apply(ServerPlayer caster, LivingEntity victim, int durationTicks) {
        if (caster == null || victim == null || caster == victim || durationTicks <= 0
                || !caster.isAlive() || caster.isSpectator() || !victim.isAlive()
                || !(victim.level() instanceof ServerLevel level) || caster.level() != level) {
            return;
        }
        long now = now(level);
        CompoundTag entries = victim.getPersistentData().getCompoundOrEmpty(PACIFICATION_KEY);
        // Drop old caster entries whenever another hit extends the restriction.
        for (String id : new ArrayList<>(entries.keySet())) {
            if (entries.getLongOr(id, 0L) <= now) entries.remove(id);
        }
        String casterId = caster.getUUID().toString();
        entries.putLong(casterId, Math.max(entries.getLongOr(casterId, 0L), now + durationTicks));
        victim.getPersistentData().put(PACIFICATION_KEY, entries);

        if (victim instanceof Mob mob && mob.getTarget() != null
                && mob.getTarget().getUUID().equals(caster.getUUID())) {
            mob.setTarget(null);
        }
        var attackTarget = victim.getBrain().getMemoryInternal(MemoryModuleType.ATTACK_TARGET);
        if (attackTarget != null && attackTarget.filter(target -> target.getUUID().equals(caster.getUUID())).isPresent()) {
            victim.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        }
    }

    public static boolean isBlocked(LivingEntity attacker, LivingEntity protectedTarget) {
        if (attacker == null || protectedTarget == null || attacker == protectedTarget
                || !(attacker.level() instanceof ServerLevel level)) {
            return false;
        }
        var saved = attacker.getPersistentData().getCompound(PACIFICATION_KEY);
        if (saved.isEmpty()) return false;
        CompoundTag entries = saved.get();
        String targetId = protectedTarget.getUUID().toString();
        long until = entries.getLongOr(targetId, 0L);
        if (until > now(level)) return true;
        entries.remove(targetId);
        if (entries.isEmpty()) attacker.getPersistentData().remove(PACIFICATION_KEY);
        return false;
    }

    private static long now(ServerLevel level) {
        return level.getServer().overworld().getGameTime();
    }
}
