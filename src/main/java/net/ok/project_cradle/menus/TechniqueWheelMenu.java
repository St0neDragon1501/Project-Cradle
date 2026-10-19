package net.ok.project_cradle.menus;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.QuickDropInventory;

/** Read-only view of the first four independent quick slots for the technique wheel. */
public final class TechniqueWheelMenu extends AbstractContainerMenu {
    public static final int TECHNIQUE_SLOT_COUNT = 4;
    private static final int[][] SLOT_POSITIONS = {
            {92, 40}, {132, 80}, {92, 120}, {52, 80}
    };

    public TechniqueWheelMenu(int containerId, Inventory inventory) {
        super(ModMenus.TECHNIQUE_WHEEL.get(), containerId);
        QuickDropInventory quickDrop = inventory.player.getData(ModAttachments.QUICK_DROP_INVENTORY);
        for (int index = 0; index < TECHNIQUE_SLOT_COUNT; index++) {
            int x = SLOT_POSITIONS[index][0];
            int y = SLOT_POSITIONS[index][1];
            this.addSlot(new Slot(quickDrop, index, x, y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }

                @Override
                public boolean mayPickup(Player player) {
                    return false;
                }
            });
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
