package net.ok.project_cradle.server.progression;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.core.ProgressionHints;
import net.ok.project_cradle.entities.BasicStrikerProjectile;

/** Small practiced expressions of Authority stay within the selected Icon's domain. */
public final class IconAuthorityService {
    private IconAuthorityService() {
    }
    public static boolean issue(ServerPlayer player) {
        var core = player.getData(ModAttachments.CORE);
        String icon = core.getIcons().contains(core.getPendingIcon()) ? core.getPendingIcon() : core.getIcon();
        List<Mob> targets = player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(8.0), mob -> mob.isAlive() && mob.getTarget() == player);
        var projectiles = player.level().getEntitiesOfClass(BasicStrikerProjectile.class, player.getBoundingBox().inflate(8.0), spell -> spell.getOwner() != player);
        boolean healing = icon.equals("life") && player.getHealth() < player.getMaxHealth();
        boolean clearing = icon.equals("void") && !projectiles.isEmpty();
        if ((icon.equals("life") && !healing) || (icon.equals("void") && !clearing)) return false;
        if (!healing && !clearing && targets.isEmpty()) return false;
        if (!List.of("strength", "force", "earth", "winter", "water", "air", "storm", "sword", "fire", "dragon", "death", "destruction", "light", "shadow", "dream", "life", "void").contains(icon)) return false;
        if (!player.getData(ModAttachments.MADRA).trySpendMadra(15.0)) {
            player.sendOverlayMessage(Component.translatable("message.project_cradle.not_enough_authority_madra"));
            return true;
        }
        if (healing) player.heal(4.0F);
        if (clearing) projectiles.forEach(BasicStrikerProjectile::discard);
        for (Mob target : targets) {
            switch (icon) {
                case "strength", "force", "earth" -> {
                    target.setTarget(null);
                    target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 1, true, false, false));
                    var direction = target.position().subtract(player.position()).normalize();
                    target.push(direction.x * 0.7, 0.15, direction.z * 0.7);
                    target.hurtMarked = true;
                }
                case "winter", "water", "air", "storm" -> target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 2, true, false, false));
                case "fire", "dragon" -> target.igniteForSeconds(4.0F);
                case "sword", "destruction" -> target.hurt(player.damageSources().magic(), 4.0F);
                case "death" -> target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0, true, false, false));
                case "light" -> target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0, true, false, false));
                case "shadow" -> target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0, true, false, false));
                case "dream" -> target.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 80, 0, true, false, false));
                default -> { }
            }
        }
        player.syncData(ModAttachments.MADRA);
        player.sendOverlayMessage(Component.translatable("message.project_cradle.authority_expression", ProgressionHints.iconName(icon)));
        return true;
    }
}
