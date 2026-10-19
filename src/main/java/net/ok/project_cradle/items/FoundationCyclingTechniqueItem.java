package net.ok.project_cradle.items;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.core.advancement.AdvancementLevel;
import net.ok.project_cradle.server.items.ProgressionItemActions;

/** A five-second channel; the server decides whether the core can be opened. */
public final class FoundationCyclingTechniqueItem extends Item {
    private static final int CHANNEL_TICKS = 100;

    public FoundationCyclingTechniqueItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            return ProgressionItemActions.beginFoundation(serverPlayer, hand);
        }
        // Use synchronized state only to predict the channel animation; the server rechecks eligibility.
        if (player.getData(ModAttachments.CORE).hasReceivedTutorial()) {
            return InteractionResult.FAIL;
        }
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return CHANNEL_TICKS;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
        if (user instanceof ServerPlayer serverPlayer) {
            ProgressionItemActions.finishFoundation(serverPlayer, stack);
        }
        return stack;
    }
}
