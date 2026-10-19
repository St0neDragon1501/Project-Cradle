package net.ok.project_cradle.items;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.ok.project_cradle.capability.core.IronBodyType;
import net.ok.project_cradle.server.items.ProgressionItemActions;

public final class IronBodyElixirItem extends Item {
    private final IronBodyType bodyType;

    public IronBodyElixirItem(Properties properties) {
        this(properties, IronBodyType.PERFECT);
    }

    public IronBodyElixirItem(Properties properties, IronBodyType bodyType) {
        super(properties);
        this.bodyType = bodyType;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return player instanceof ServerPlayer serverPlayer
                ? ProgressionItemActions.completeIronBody(serverPlayer, hand, this.bodyType)
                : InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
            Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("tooltip.project_cradle.iron_body." + this.bodyType.id())
                .withStyle(ChatFormatting.GRAY));
        if (this.bodyType == IronBodyType.BASIC) {
            tooltip.accept(Component.translatable("tooltip.project_cradle.basic_iron_limit")
                    .withStyle(ChatFormatting.GOLD));
        }
    }
}
