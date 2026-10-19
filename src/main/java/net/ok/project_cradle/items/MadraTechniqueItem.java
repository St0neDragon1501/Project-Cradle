package net.ok.project_cradle.items;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/** Common item adapter: the client predicts use; the server validates and casts the technique. */
public abstract class MadraTechniqueItem extends Item implements TechniqueItem {
    protected MadraTechniqueItem(Properties properties) {
        super(properties);
    }

    @Override
    public final InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        return castTechnique(serverPlayer, serverPlayer.getItemInHand(hand))
                ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }
}
