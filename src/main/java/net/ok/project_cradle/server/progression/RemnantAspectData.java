package net.ok.project_cradle.server.progression;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.ok.project_cradle.capability.madra.MadraType;

/** Bindings retain their source's madra, independently of whoever holds or absorbs them. */
public final class RemnantAspectData {
    private static final String ASPECT = "project_cradle_remnant_aspect";
    private static final String SECONDARY_ASPECT = "project_cradle_remnant_secondary_aspect";

    private RemnantAspectData() {
    }

    public static void set(ItemStack stack, MadraType.Element aspect) {
        set(stack, aspect == null ? List.of() : List.of(aspect));
    }

    public static void set(ItemStack stack, List<MadraType.Element> aspects) {
        List<MadraType.Element> valid = new ArrayList<>(2);
        if (aspects != null) for (MadraType.Element aspect : aspects) {
            if (aspect != null && !valid.contains(aspect) && valid.size() < 2) valid.add(aspect);
        }
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        // Keep the original primary key so existing bindings and older readers remain compatible.
        if (valid.isEmpty()) data.remove(ASPECT);
        else data.putString(ASPECT, valid.get(0).id());
        if (valid.size() < 2) data.remove(SECONDARY_ASPECT);
        else data.putString(SECONDARY_ASPECT, valid.get(1).id());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        stack.set(DataComponents.ITEM_NAME, valid.isEmpty()
                ? Component.translatable("item.project_cradle.remnant_binding") : typedName(valid));
    }

    /** Empty means a legacy untyped binding, not a binding of the current holder's aspect. */
    public static List<MadraType.Element> getTypes(ItemStack stack) {
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        List<MadraType.Element> aspects = new ArrayList<>(2);
        MadraType.Element primary = MadraType.Element.byId(data.getStringOr(ASPECT, ""));
        MadraType.Element secondary = MadraType.Element.byId(data.getStringOr(SECONDARY_ASPECT, ""));
        if (primary != null) aspects.add(primary);
        if (secondary != null && secondary != primary) aspects.add(secondary);
        return List.copyOf(aspects);
    }

    public static MadraType.Element get(ItemStack stack, MadraType.Element legacyFallback) {
        List<MadraType.Element> aspects = getTypes(stack);
        return aspects.isEmpty() ? legacyFallback : aspects.get(0);
    }

    public static Component typedName(List<MadraType.Element> aspects) {
        return Component.translatable("item.project_cradle.remnant_binding_typed", typeNames(aspects));
    }

    public static Component typeNames(List<MadraType.Element> aspects) {
        MutableComponent names = Component.empty();
        for (int index = 0; index < aspects.size(); index++) {
            if (index > 0) names.append(" + ");
            names.append(Component.translatable(aspects.get(index).translationKey()));
        }
        return names;
    }
}
