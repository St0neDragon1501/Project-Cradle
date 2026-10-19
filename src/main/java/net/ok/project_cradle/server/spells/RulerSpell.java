package net.ok.project_cradle.server.spells;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.server.level.ServerPlayer;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.core.Core;
import net.ok.project_cradle.capability.core.advancement.AdvancementLevel;
import net.ok.project_cradle.capability.madra.MadraType;

import net.minecraft.world.item.ItemStack;

public final class RulerSpell {
    private RulerSpell() {
    }

    public static boolean cast(ServerPlayer player, ItemStack stack) {
        return MadraTechniqueSpell.cast(player, stack, 26.0, 80, RulerSpell::apply);
    }

    private static boolean apply(ServerLevel level, ServerPlayer player) {
        Core core = player.getData(ModAttachments.CORE);
        if (core.getAdvancement().order() < AdvancementLevel.JADE.order()) {
            player.sendSystemMessage(Component.translatable("message.project_cradle.ruler_requires_jade"));
            return false;
        }

        List<Monster> targets = level.getEntitiesOfClass(Monster.class,
                player.getBoundingBox().inflate(net.ok.project_cradle.spells.TechniqueScaling.range(player, 7.0)), monster -> monster.isAlive());
        if (targets.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.project_cradle.ruler_no_targets"));
            return false;
        }

        float damage = 5.0F;
        MadraType.Element aspect = player.getData(ModAttachments.MADRA_TYPE).getTypes().get(0);
        for (Monster target : targets) {
            if (!TechniqueCombatService.hurt(player, player, target, damage)) continue;
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1, false, true, true));
            var direction = target.position().subtract(player.position()).normalize();
            target.push(direction.x * 0.65, 0.18, direction.z * 0.65);
            target.hurtMarked = true;
            TechniqueEffectUtil.emitAround(level, aspect, target, 10);
        }
        return true;
    }
}
