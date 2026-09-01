package net.ok.project_cradle.items;

import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.core.Core;
import net.ok.project_cradle.capability.madra.Madra;
import net.ok.project_cradle.capability.madra.MaxMadra;
import net.ok.project_cradle.utils.Utils;
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

public class MadraDetectionItem extends Item {

    public MadraDetectionItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(
            Level level,
            Player player,
            InteractionHand usedHand
    ) {

        if (!level.isClientSide()) {
            Madra madra = player.getData(ModAttachments.MADRA);
            MaxMadra maxMadra = player.getData(ModAttachments.MAXMADRA);

            madra.addMadra(10.0, maxMadra.getLevel());
            
            player.sendSystemMessage(Component.literal("Madra " + madra.getALevel()).withStyle(ChatFormatting.BOLD));
            
        }

        return InteractionResult.PASS;
    }
    
    
    
}
