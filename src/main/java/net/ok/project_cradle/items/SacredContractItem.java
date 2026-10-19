package net.ok.project_cradle.items;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.server.progression.SpiritBondService;

/** Use on a stronger sacred beast or wild spirit to form a mutual contract. */
@EventBusSubscriber(modid = ProjectCradle.MODID)
public final class SacredContractItem extends Item {
    public SacredContractItem(Properties properties) {
        super(properties);
    }

    /** Handle before mob interaction so wild Allays cannot take the contract as a held item. */
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getEntity().getItemInHand(event.getHand());
        if (!(stack.getItem() instanceof SacredContractItem contract)
                || !(event.getTarget() instanceof LivingEntity target)) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(contract.interactLivingEntity(stack, event.getEntity(), target, event.getHand()));
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target,
            InteractionHand hand) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        InteractionResult result = SpiritBondService.contract(serverPlayer, target);
        if (result == InteractionResult.SUCCESS && !serverPlayer.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return result;
    }
}
