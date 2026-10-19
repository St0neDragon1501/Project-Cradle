package net.ok.project_cradle.items;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.ok.project_cradle.server.items.ProgressionItemActions;

public final class RemnantBindingItem extends Item {
    public RemnantBindingItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return player instanceof ServerPlayer serverPlayer
                ? ProgressionItemActions.absorbRemnant(serverPlayer, hand)
                : InteractionResult.SUCCESS;
    }
}
