package net.ok.project_cradle.items;

import net.ok.project_cradle.capability.test.ModAttachments;
import net.ok.project_cradle.utils.Utils;
import net.ok.project_cradle.capability.test.Core;

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

            Core core = player.getData(ModAttachments.CORE);
            
            if(core.getALevel().equals(Utils.UNFOUNDED)) {
            	core.setALevel(Utils.FOUNDATION);
            } else {
            	core.setALevel(Utils.UNFOUNDED);
            }
            
            player.sendSystemMessage(
                    Component.literal("Core " + core.getALevel())
                            .withStyle(ChatFormatting.BOLD)
            );
        }

        return InteractionResult.PASS;
    }
}
