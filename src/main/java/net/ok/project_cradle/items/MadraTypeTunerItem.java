package net.ok.project_cradle.items;

import java.util.stream.Collectors;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.madra.MadraType;

public class MadraTypeTunerItem extends Item {
    private static final int COOLDOWN_TICKS = 5;

    public MadraTypeTunerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            MadraType madraType = player.getData(ModAttachments.MADRA_TYPE);
            madraType.cycleType(player.isShiftKeyDown());
            player.syncData(ModAttachments.MADRA_TYPE);

            String typeNames = madraType.getTypes().stream()
                    .map(element -> Component.translatable(element.translationKey()).getString())
                    .collect(Collectors.joining(", "));
            player.sendSystemMessage(
                    Component.translatable("message.project_cradle.madra_types_changed", typeNames)
            );

            ItemStack itemStack = player.getItemInHand(hand);
            player.getCooldowns().addCooldown(itemStack, COOLDOWN_TICKS);
        }

        return InteractionResult.SUCCESS;
    }
}
