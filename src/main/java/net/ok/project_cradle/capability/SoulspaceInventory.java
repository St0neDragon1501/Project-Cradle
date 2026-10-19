package net.ok.project_cradle.capability;

import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

/** A soulspace holds one item, independently of equipment and the ordinary inventory. */
public final class SoulspaceInventory extends SimpleContainer implements ValueIOSerializable {
    public static final int SLOT_COUNT = 1;

    public SoulspaceInventory() {
        super(SLOT_COUNT);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean canAddItem(ItemStack stack) {
        return !stack.isEmpty() && this.getItem(0).isEmpty();
    }

    @Override
    public ItemStack addItem(ItemStack stack) {
        ItemStack remainder = stack.copy();
        if (this.canAddItem(remainder)) {
            this.setItem(0, remainder.split(1));
        }
        return remainder;
    }

    @Override
    public void serialize(ValueOutput output) {
        ContainerHelper.saveAllItems(output, this.getItems());
    }

    @Override
    public void deserialize(ValueInput input) {
        this.clearContent();
        ContainerHelper.loadAllItems(input, this.getItems());
        this.getItem(0).limitSize(1);
    }

    public void copyFrom(SoulspaceInventory source) {
        this.setItem(0, source.getItem(0).copy());
    }
}
