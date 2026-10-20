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
import net.ok.project_cradle.server.items.ProgressionItemActions;
import net.ok.project_cradle.server.progression.RemnantAspectData;

public final class RemnantBindingItem extends Item {
    public RemnantBindingItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        var aspects = RemnantAspectData.getTypes(stack);
        // Older typed bindings did not have their ITEM_NAME component populated.
        return aspects.isEmpty() ? super.getName(stack) : RemnantAspectData.typedName(aspects);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
            Consumer<Component> tooltip, TooltipFlag flag) {
        var aspects = RemnantAspectData.getTypes(stack);
        if (!aspects.isEmpty()) {
            tooltip.accept(Component.translatable("tooltip.project_cradle.remnant_binding_aspects",
                    RemnantAspectData.typeNames(aspects)).withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return player instanceof ServerPlayer serverPlayer
                ? ProgressionItemActions.absorbRemnant(serverPlayer, hand)
                : InteractionResult.SUCCESS;
    }
}
