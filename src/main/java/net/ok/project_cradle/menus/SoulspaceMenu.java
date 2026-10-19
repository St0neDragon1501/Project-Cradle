package net.ok.project_cradle.menus;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.SoulspaceInventory;

/** Server-controlled access to the one item stored in an opened soulspace. */
public final class SoulspaceMenu extends AbstractContainerMenu {
    public static final int SOULSPACE_SLOT_COUNT = SoulspaceInventory.SLOT_COUNT;
    private final Player owner;

    public SoulspaceMenu(int containerId, Inventory inventory) {
        super(ModMenus.SOULSPACE.get(), containerId);
        this.owner = inventory.player;

        SoulspaceInventory soulspace = this.owner.getData(ModAttachments.SOULSPACE_INVENTORY);
        this.addSlot(new Slot(soulspace, 0, 80, 31) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return SoulspaceMenu.this.owner.getData(ModAttachments.CORE).hasSoulspace();
            }

            @Override
            public boolean mayPickup(Player player) {
                return SoulspaceMenu.this.stillValid(player);
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(inventory, 9 + row * 9 + column,
                        8 + column * 18, 68 + row * 18));
            }
        }
        for (int slot = 0; slot < 9; slot++) {
            this.addSlot(new Slot(inventory, slot, 8 + slot * 18, 132));
        }
    }

    @Override
    public void clicked(int slotIndex, int button, ContainerInput input, Player player) {
        if (this.stillValid(player)) {
            super.clicked(slotIndex, button, input, player);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        if (!this.stillValid(player) || slotIndex < 0 || slotIndex >= this.slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasItem() || !slot.mayPickup(player)) {
            return ItemStack.EMPTY;
        }

        ItemStack moving = slot.getItem();
        ItemStack original = moving.copy();
        boolean moved = slotIndex < SOULSPACE_SLOT_COUNT
                ? this.moveItemStackTo(moving, SOULSPACE_SLOT_COUNT, this.slots.size(), true)
                : this.moveItemStackTo(moving, 0, SOULSPACE_SLOT_COUNT, false);
        if (!moved) {
            return ItemStack.EMPTY;
        }

        if (moving.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY, original);
        } else {
            slot.setChanged();
        }
        slot.onTake(player, moving);
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return player == this.owner && player.isAlive()
                && player.getData(ModAttachments.CORE).hasSoulspace();
    }
}
