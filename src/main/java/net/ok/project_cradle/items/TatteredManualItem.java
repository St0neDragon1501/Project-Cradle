package net.ok.project_cradle.items;

import net.ok.project_cradle.capability.test.ModAttachments;
import net.ok.project_cradle.utils.Utils;
import net.ok.project_cradle.capability.test.Core;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class TatteredManualItem extends Item {

    public TatteredManualItem(Properties properties) {
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
            
            if (core.getALevel().equals(Utils.UNFOUNDED)) {
            	TatteredManualItem.advanceToFoundation(core, player, usedHand);
            } else {
            	player.sendSystemMessage(Component.literal("You have already begun your path; you now must walk it.").withColor(16711680));
            }
        }

        return InteractionResult.PASS;
    }
    
    
    private static void advanceToFoundation(Core core, Player player, InteractionHand usedHand) {
    	core.setALevel(Utils.FOUNDATION);
        
        player.sendSystemMessage(Component.literal("Core " + core.getALevel()).withStyle(ChatFormatting.BOLD));
        
        player.setItemInHand(usedHand, new ItemStack(Items.PAPER));
    }
}
