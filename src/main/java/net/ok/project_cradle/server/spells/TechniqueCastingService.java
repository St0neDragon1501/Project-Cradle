package net.ok.project_cradle.server.spells;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.QuickDropInventory;
import net.ok.project_cradle.capability.core.Core;
import net.ok.project_cradle.capability.core.advancement.AdvancementLevel;
import net.ok.project_cradle.capability.madra.MadraType;
import net.ok.project_cradle.items.TechniqueItem;
import net.ok.project_cradle.networking.packet.CastingStateS2C;
import net.ok.project_cradle.spells.TechniqueCastProfile;
import net.ok.project_cradle.server.progression.TechniqueKnowledge;

/** One server-owned preparation per player. Resources are paid by the spell at completion. */
public final class TechniqueCastingService {
    private static final Map<UUID, Cast> CASTS = new HashMap<>();

    private TechniqueCastingService() {
    }

    public static boolean begin(ServerPlayer player, ItemStack stack) {
        if (CASTS.containsKey(player.getUUID()) || !canAct(player)) return false;
        TechniqueCastProfile profile = TechniqueCastProfile.fromItemStack(stack);
        if (profile == null || !(stack.getItem() instanceof TechniqueItem technique)) return false;
        Source source = findSource(player, stack);
        if (source == null || !ready(player, stack, profile, true)) return false;

        if (profile.castTimeTicks() <= 0) {
            boolean cast = technique.castTechnique(player, stack);
            if (cast) {
                TechniqueKnowledge.recordSuccessfulCast(player, stack);
                emitRunes(player, profile, 16, 0.32);
                TechniqueVisualService.complete(player, stack, profile);
            }
            return cast;
        }
        long now = player.level().getGameTime();
        Cast cast = new Cast(player, stack, stack.copy(), source, profile, player.level().dimension(),
                now, now + profile.castTimeTicks());
        CASTS.put(player.getUUID(), cast);
        player.connection.send(new CastingStateS2C(stack.getItem().getDescriptionId(), profile.castTimeTicks(), true));
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.25F, 1.4F);
        emitRunes(player, profile, 8, 0.22);
        return true;
    }

    public static void clear() { CASTS.clear(); }

    public static boolean isCasting(ServerPlayer player) {
        Cast cast = CASTS.get(player.getUUID());
        return cast != null && cast.owner() == player;
    }

    /** Used by manual cancellation, interruptions, and counterspells. */
    public static boolean cancel(ServerPlayer player) {
        Cast cast = CASTS.get(player.getUUID());
        if (cast == null || cast.owner() != player) return false;
        CASTS.remove(player.getUUID());
        player.connection.send(new CastingStateS2C("", 0, false));
        return true;
    }

    public static void tick(ServerPlayer player) {
        Cast cast = CASTS.get(player.getUUID());
        if (cast == null) return;
        if (cast.owner() != player) {
            CASTS.remove(player.getUUID());
            player.connection.send(new CastingStateS2C("", 0, false));
            return;
        }
        if (!canAct(player) || !player.level().dimension().equals(cast.dimension())
                || !cast.source().stillContains(player, cast.stack())
                || cast.stack().getCount() != cast.original().getCount()
                || !ItemStack.isSameItemSameComponents(cast.stack(), cast.original())) {
            cancel(player);
            return;
        }
        long now = player.level().getGameTime();
        if (now >= cast.endsAt()) {
            CASTS.remove(player.getUUID());
            player.connection.send(new CastingStateS2C("", 0, false));
            if (ready(player, cast.stack(), cast.profile(), true)
                    && ((TechniqueItem) cast.stack().getItem()).castTechnique(player, cast.stack())) {
                TechniqueKnowledge.recordSuccessfulCast(player, cast.stack());
                emitRunes(player, cast.profile(), 16, 0.32);
                TechniqueVisualService.complete(player, cast.stack(), cast.profile());
            }
        } else if ((now - cast.startedAt()) % 4L == 0L) {
            emitRunes(player, cast.profile(), 8, 0.22);
        }
    }

    private static boolean canAct(ServerPlayer player) {
        return player.isAlive() && !player.isSpectator() && !player.isSleeping()
                && player.containerMenu == player.inventoryMenu;
    }

    private static boolean ready(ServerPlayer player, ItemStack stack, TechniqueCastProfile profile, boolean feedback) {
        if (player.getCooldowns().isOnCooldown(stack)) return false;
        Core core = player.getData(ModAttachments.CORE);
        if (core.getAdvancement() == AdvancementLevel.UNFOUNDED) {
            if (feedback) player.sendOverlayMessage(Component.translatable("message.project_cradle.need_foundation"));
            return false;
        }
        double madraCost = profile.madraCost(player);
        double madra = player.getData(ModAttachments.MADRA).getALevel();
        if (!Double.isFinite(madra) || madra < madraCost) {
            if (feedback) player.sendOverlayMessage(Component.translatable(
                    "message.project_cradle.not_enough_madra", madraCost));
            return false;
        }
        if (!core.hasSoulfire(profile.soulfireCost())) {
            if (feedback) player.sendOverlayMessage(Component.translatable(
                    "message.project_cradle.technique_needs_soulfire", profile.soulfireCost()));
            return false;
        }
        return true;
    }

    private static Source findSource(ServerPlayer player, ItemStack stack) {
        for (InteractionHand hand : InteractionHand.values()) {
            if (player.getItemInHand(hand) == stack) return new Source(hand, -1);
        }
        QuickDropInventory quickDrop = player.getData(ModAttachments.QUICK_DROP_INVENTORY);
        for (int slot = 0; slot < QuickDropInventory.SLOT_COUNT; slot++) {
            if (quickDrop.getItem(slot) == stack) return new Source(null, slot);
        }
        return null;
    }

    private static void emitRunes(ServerPlayer player, TechniqueCastProfile profile, int count, double radius) {
        MadraType.Element aspect = profile.aspect();
        if (aspect == null) aspect = player.getData(ModAttachments.MADRA_TYPE).getTypes().get(0);
        DustParticleOptions particle = new DustParticleOptions(TechniqueVisualService.color(aspect), 0.55F);
        double yaw = Math.toRadians(player.getYRot());
        Vec3 across = new Vec3(Math.cos(yaw), 0.0, Math.sin(yaw));
        Vec3 look = player.getLookAngle();
        Vec3 up = look.cross(across).normalize();
        Vec3 center = player.getEyePosition().add(look.scale(0.55)).add(0.0, -0.3, 0.0);
        double phase = player.level().getGameTime() * 0.12;
        for (int point = 0; point < count; point++) {
            double angle = phase + Math.PI * 2.0 * point / count;
            Vec3 position = center.add(across.scale(Math.cos(angle) * radius)).add(up.scale(Math.sin(angle) * radius));
            player.level().sendParticles(particle, position.x, position.y, position.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    private record Source(InteractionHand hand, int quickSlot) {
        boolean stillContains(ServerPlayer player, ItemStack stack) {
            if (stack.isEmpty()) return false;
            return hand != null ? player.getItemInHand(hand) == stack
                    : player.getData(ModAttachments.QUICK_DROP_INVENTORY).getItem(quickSlot) == stack;
        }
    }

    private record Cast(ServerPlayer owner, ItemStack stack, ItemStack original, Source source,
            TechniqueCastProfile profile, ResourceKey<Level> dimension, long startedAt, long endsAt) {
    }
}
