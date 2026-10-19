package net.ok.project_cradle.server.spells;

import java.util.function.BiPredicate;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.ItemStack;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.core.advancement.AdvancementLevel;
import net.ok.project_cradle.capability.madra.Madra;
import net.ok.project_cradle.capability.madra.MaxMadra;

/** Server-only payment, cooldown, and effect execution for general techniques. */
final class MadraTechniqueSpell {
    private MadraTechniqueSpell() {
    }

    static boolean cast(ServerPlayer player, ItemStack stack, double madraCost, int cooldownTicks,
            BiPredicate<ServerLevel, ServerPlayer> effect) {
        ServerLevel level = player.level();
        if (player.getCooldowns().isOnCooldown(stack)) {
            return false;
        }
        if (player.getData(ModAttachments.CORE).getAdvancement() == AdvancementLevel.UNFOUNDED) {
            player.sendSystemMessage(Component.translatable("message.project_cradle.need_foundation"));
            return false;
        }

        Madra madra = player.getData(ModAttachments.MADRA);
        if (!madra.trySpendMadra(madraCost)) {
            player.sendSystemMessage(Component.translatable(
                    "message.project_cradle.not_enough_madra", madraCost));
            return false;
        }

        if (!effect.test(level, player)) {
            MaxMadra maxMadra = player.getData(ModAttachments.MAXMADRA);
            madra.addMadra(madraCost, maxMadra.getLevel());
            player.syncData(ModAttachments.MADRA);
            return false;
        }

        player.getCooldowns().addCooldown(stack, cooldownTicks);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 0.7F, 1.0F);
        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
        player.syncData(ModAttachments.MADRA);
        return true;
    }
}
