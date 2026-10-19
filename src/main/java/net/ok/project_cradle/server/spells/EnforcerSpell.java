package net.ok.project_cradle.server.spells;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.server.level.ServerPlayer;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.madra.MadraType;

import net.minecraft.world.item.ItemStack;

public final class EnforcerSpell {
    private EnforcerSpell() {
    }

    public static boolean cast(ServerPlayer player, ItemStack stack) {
        return MadraTechniqueSpell.cast(player, stack, 22.0, 180, EnforcerSpell::apply);
    }

    private static boolean apply(ServerLevel level, ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, net.ok.project_cradle.spells.TechniqueScaling.duration(player, 160), 0, false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.SPEED, net.ok.project_cradle.spells.TechniqueScaling.duration(player, 160), 0, false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, net.ok.project_cradle.spells.TechniqueScaling.duration(player, 100), 0, false, true, true));
        MadraType.Element aspect = player.getData(ModAttachments.MADRA_TYPE).getTypes().get(0);
        EnforcerPowerService.empower(player, 160, aspect);
        TechniqueEffectUtil.emitAround(level, aspect, player, 24);
        return true;
    }
}
