package net.ok.project_cradle.capability;

import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public final class QuickDropInventory extends SimpleContainer implements ValueIOSerializable {
    public static final int SLOT_COUNT = 5;

    public QuickDropInventory() {
        super(SLOT_COUNT);
    }

    @Override
    public void serialize(ValueOutput output) {
        ContainerHelper.saveAllItems(output, this.getItems());
    }

    @Override
    public void deserialize(ValueInput input) {
        this.clearContent();
        ContainerHelper.loadAllItems(input, this.getItems());
    }

    public void copyFrom(QuickDropInventory source) {
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            this.setItem(slot, source.getItem(slot).copy());
        }
    }
}
