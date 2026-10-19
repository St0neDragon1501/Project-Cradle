package net.ok.project_cradle.server.networking;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.QuickDropInventory;
import net.ok.project_cradle.capability.core.Core;
import net.ok.project_cradle.items.TechniqueItem;
import net.ok.project_cradle.menus.TechniqueWheelMenu;
import net.ok.project_cradle.networking.packet.SelectTechniqueC2S;

public final class TechniqueWheelPayloadHandler {
    private TechniqueWheelPayloadHandler() {
    }

    public static void handle(SelectTechniqueC2S packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || !player.isAlive() || player.isSpectator()
                || !(player.containerMenu instanceof TechniqueWheelMenu)) {
            return;
        }

        int slot = packet.slot();
        Core core = player.getData(ModAttachments.CORE);
        int unlockedSlots = Math.min(TechniqueWheelMenu.TECHNIQUE_SLOT_COUNT,
                core.getAdvancement().techniqueSlots());
        if (slot < 0 || slot >= unlockedSlots) {
            player.sendSystemMessage(Component.translatable("message.project_cradle.technique_slot_locked"));
            return;
        }

        QuickDropInventory quickDrop = player.getData(ModAttachments.QUICK_DROP_INVENTORY);
        ItemStack stack = quickDrop.getItem(slot);
        if (stack.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.project_cradle.empty_technique_slot"));
            return;
        }
        if (!(stack.getItem() instanceof TechniqueItem)) {
            player.sendSystemMessage(Component.translatable("message.project_cradle.item_not_technique"));
            return;
        }

        core.setSelectedTechnique(slot);
        player.syncData(ModAttachments.CORE);
        player.sendSystemMessage(Component.translatable("message.project_cradle.technique_selected", slot + 1));
    }
}
