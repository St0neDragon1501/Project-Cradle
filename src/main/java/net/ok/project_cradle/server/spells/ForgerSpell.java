package net.ok.project_cradle.server.spells;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.madra.MadraType;

public final class ForgerSpell {
    private ForgerSpell() {
    }

    public static boolean cast(ServerPlayer player, ItemStack stack) {
        return MadraTechniqueSpell.cast(player, stack, 18.0, 140, ForgerSpell::apply);
    }

    private static boolean apply(ServerLevel level, ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, net.ok.project_cradle.spells.TechniqueScaling.duration(player, 200),
                Math.min(4, (int) net.ok.project_cradle.spells.TechniqueScaling.power(player)), false, true, true));
        MadraType.Element aspect = player.getData(ModAttachments.MADRA_TYPE).getTypes().get(0);
        TechniqueEffectUtil.emitAround(level, aspect, player, 32);
        return true;
    }
}
