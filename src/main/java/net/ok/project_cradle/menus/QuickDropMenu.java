package net.ok.project_cradle.menus;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.QuickDropInventory;

public class QuickDropMenu extends AbstractContainerMenu {
    public static final int QUICK_DROP_SLOT_COUNT = 5;

    public QuickDropMenu(int containerId, Inventory inventory) {
        super(ModMenus.QUICK_DROP.get(), containerId);

        // These slots use their own player attachment and are independent of the inventory.
        QuickDropInventory quickDropInventory = inventory.player.getData(ModAttachments.QUICK_DROP_INVENTORY);
        for (int slot = 0; slot < QUICK_DROP_SLOT_COUNT; slot++) {
            this.addSlot(new Slot(quickDropInventory, slot, 43 + slot * 18, 31));
        }

        // Show the main inventory below the quick-drop slots.
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int inventorySlot = 9 + row * 9 + column;
                this.addSlot(new Slot(inventory, inventorySlot, 8 + column * 18, 68 + row * 18));
            }
        }

        // Show the full hotbar below the main inventory.
        for (int slot = 0; slot < 9; slot++) {
            this.addSlot(new Slot(inventory, slot, 8 + slot * 18, 132));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= this.slots.size()) {
            return ItemStack.EMPTY;
        }

        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack original = slot.getItem().copy();
        ItemStack moving = slot.getItem();
        boolean moved = slotIndex < QUICK_DROP_SLOT_COUNT
                ? this.moveItemStackTo(moving, QUICK_DROP_SLOT_COUNT, this.slots.size(), true)
                : this.moveItemStackTo(moving, 0, QUICK_DROP_SLOT_COUNT, false);

        if (!moved) {
            return ItemStack.EMPTY;
        }

        if (moving.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY, original);
        } else {
            slot.setChanged();
        }

        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
