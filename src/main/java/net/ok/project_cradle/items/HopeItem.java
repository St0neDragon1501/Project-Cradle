package net.ok.project_cradle.items;

import net.ok.project_cradle.capability.test.ModAttachments;
import net.ok.project_cradle.capability.test.Shaper;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class HopeItem extends Item {

    public HopeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(
            Level level,
            Player player,
            InteractionHand usedHand
    ) {

        if (!level.isClientSide()) {

            Shaper shaper = player.getData(ModAttachments.SHAPER);

            player.sendSystemMessage(
                    Component.literal("Shaper " + shaper.getShaper())
                            .withStyle(ChatFormatting.BOLD)
            );
        }

        return InteractionResult.PASS;
    }
}
