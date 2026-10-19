package net.ok.project_cradle.items;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.ok.project_cradle.server.progression.RemnantFusionService;

/** Manifests the user's own spirit; an ordinary external Remnant cannot replace this challenge. */
public final class SpiritMirrorItem extends Item {
    public SpiritMirrorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        InteractionResult result = RemnantFusionService.begin(serverPlayer);
        if (result == InteractionResult.SUCCESS) {
            serverPlayer.getCooldowns().addCooldown(serverPlayer.getItemInHand(hand), 40);
        }
        return result;
    }
}
