package net.ok.project_cradle.server.networking;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.QuickDropInventory;
import net.ok.project_cradle.capability.core.Core;
import net.ok.project_cradle.items.TechniqueItem;
import net.ok.project_cradle.menus.QuickDropMenu;
import net.ok.project_cradle.menus.TechniqueWheelMenu;
import net.ok.project_cradle.networking.packet.QuickDropKeyC2S;

public final class QuickDropKeyPayloadHandler {
    private QuickDropKeyPayloadHandler() {
    }

    public static void handle(QuickDropKeyC2S packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || !player.isAlive() || player.isSpectator()
                || player.containerMenu != player.inventoryMenu) {
            return;
        }

        if (packet.key() == 0) {
            openTechniqueWheel(player);
            return;
        }
        if (packet.key() == -1) {
            castSelectedTechnique(player);
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

        int techniqueSlots = Math.min(4, player.getData(ModAttachments.CORE).getAdvancement().techniqueSlots());
        if (slotIndex < techniqueSlots && stack.getItem() instanceof TechniqueItem technique) {
            technique.castTechnique(player, stack);
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

    private static void openTechniqueWheel(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, owner) -> new TechniqueWheelMenu(containerId, inventory),
                Component.translatable("container.project_cradle.technique_wheel")
        ));
    }

    private static void castSelectedTechnique(ServerPlayer player) {
        Core core = player.getData(ModAttachments.CORE);
        int selected = core.getSelectedTechnique();
        if (selected < 0 || selected >= Math.min(TechniqueWheelMenu.TECHNIQUE_SLOT_COUNT,
                core.getAdvancement().techniqueSlots())) {
            player.sendSystemMessage(Component.translatable("message.project_cradle.technique_slot_locked"));
            return;
        }
        ItemStack stack = player.getData(ModAttachments.QUICK_DROP_INVENTORY).getItem(selected);
        if (stack.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.project_cradle.empty_technique_slot"));
        } else if (stack.getItem() instanceof TechniqueItem technique) {
            technique.castTechnique(player, stack);
        } else {
            player.sendSystemMessage(Component.translatable("message.project_cradle.item_not_technique"));
        }
    }

    private static void dropStack(ServerPlayer player, int slotIndex, ItemStack stack) {
        QuickDropInventory quickDrop = player.getData(ModAttachments.QUICK_DROP_INVENTORY);
        ItemStack dropped = quickDrop.removeItem(slotIndex, stack.getCount());
        if (!dropped.isEmpty() && player.drop(dropped, true) == null) {
            quickDrop.setItem(slotIndex, dropped);
        }
    }
}
