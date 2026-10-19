package net.ok.project_cradle.server.progression;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.ok.project_cradle.capability.madra.MadraType;

/** Remnant bindings retain their source's aspect instead of changing to match whoever holds them. */
public final class RemnantAspectData {
    private static final String ASPECT = "project_cradle_remnant_aspect";
    private RemnantAspectData() {
    }
    public static void set(ItemStack stack, MadraType.Element aspect) {
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        data.putString(ASPECT, aspect.id());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
    }
    public static MadraType.Element get(ItemStack stack, MadraType.Element legacyFallback) {
        String id = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getStringOr(ASPECT, legacyFallback.id());
        MadraType.Element element = MadraType.Element.byId(id);
        return element == null ? legacyFallback : element;
    }
}
