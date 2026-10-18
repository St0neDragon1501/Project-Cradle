package net.ok.project_cradle.networking;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.QuickDropInventory;
import net.ok.project_cradle.items.BasicStrikerItem;
import net.ok.project_cradle.menus.QuickDropMenu;
import net.ok.project_cradle.networking.packet.QuickDropKeyC2S;
import net.ok.project_cradle.spells.BasicStrikerSpell;

public final class QuickDropKeyPayloadHandler {
    private QuickDropKeyPayloadHandler() {
    }

    public static void handle(QuickDropKeyC2S packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }

        if (packet.key() == 6) {
            openQuickDropMenu(player);
            return;
        }

        int slotIndex = packet.key() - 1;
        if (slotIndex < 0 || slotIndex >= QuickDropMenu.QUICK_DROP_SLOT_COUNT) {
            return;
        }

        ItemStack stack = player.getData(ModAttachments.QUICK_DROP_INVENTORY).getItem(slotIndex);
        if (stack.isEmpty()) {
            return;
        }

        if (stack.getItem() instanceof BasicStrikerItem) {
            BasicStrikerSpell.castFromQuickSlot(player, stack);
        } else {
            dropStack(player, slotIndex, stack);
        }
    }

    private static void openQuickDropMenu(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, owner) -> new QuickDropMenu(containerId, inventory),
                Component.translatable("container.project_cradle.quick_drop")
        ));
    }

    private static void dropStack(ServerPlayer player, int slotIndex, ItemStack stack) {
        QuickDropInventory quickDrop = player.getData(ModAttachments.QUICK_DROP_INVENTORY);
        ItemStack dropped = quickDrop.removeItem(slotIndex, stack.getCount());
        if (!dropped.isEmpty() && player.drop(dropped, true) == null) {
            quickDrop.setItem(slotIndex, dropped);
        }
    }
}
