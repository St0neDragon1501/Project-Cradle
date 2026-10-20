package net.ok.project_cradle.spells;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.capability.madra.MadraType.Element;
import net.ok.project_cradle.items.TechniqueItem;

/** The registered techniques whose behavior can be inherited by a Remnant. */
public final class TechniqueCatalog {
    private static final String BASIC = ProjectCradle.MODID + ":basic_striker";
    private static final List<String> IDS = createIds();
    private static final Set<String> ALLOWED = Set.copyOf(IDS);

    private TechniqueCatalog() { }

    private static List<String> createIds() {
        List<String> ids = new ArrayList<>();
        ids.add(BASIC);
        ids.add(ProjectCradle.MODID + ":forger_technique");
        ids.add(ProjectCradle.MODID + ":enforcer_technique");
        ids.add(ProjectCradle.MODID + ":ruler_technique");
        for (PathTechnique technique : PathTechnique.values()) ids.add(ProjectCradle.MODID + ":" + technique.id());
        for (AdditionalTechnique technique : AdditionalTechnique.values()) ids.add(ProjectCradle.MODID + ":" + technique.id());
        for (ElementalTechnique technique : ElementalTechnique.values()) ids.add(ProjectCradle.MODID + ":" + technique.id());
        return List.copyOf(ids);
    }

    public static String basic() { return BASIC; }

    public static List<String> allIds() { return IDS; }

    /** Accept a short mod ID or a full registry ID, rejecting unsupported or missing item definitions. */
    public static String normalize(String value) {
        if (value == null || value.isBlank() || value.length() > 256) return "";
        String candidate = value.trim();
        if (candidate.indexOf(':') < 0) candidate = ProjectCradle.MODID + ":" + candidate;
        Identifier id = Identifier.tryParse(candidate);
        if (id == null || !ProjectCradle.MODID.equals(id.getNamespace()) || !ALLOWED.contains(id.toString())) return "";
        Item item = BuiltInRegistries.ITEM.getValue(id);
        return item instanceof TechniqueItem ? id.toString() : "";
    }

    public static String id(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof TechniqueItem)) return "";
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id == null ? "" : normalize(id.toString());
    }

    public static ItemStack stack(String value) {
        String normalized = normalize(value);
        if (normalized.isEmpty()) return ItemStack.EMPTY;
        Item item = BuiltInRegistries.ITEM.getValue(Identifier.tryParse(normalized));
        return item instanceof TechniqueItem ? new ItemStack(item) : ItemStack.EMPTY;
    }

    /** An ordinary spirit has an instinctive technique suited to its primary madra. */
    public static String themed(Element aspect) {
        String path = switch (aspect == null ? Element.PURE : aspect) {
            case FIRE -> "fireball";
            case WATER -> "ice_lance";
            case AIR -> "cutting_wind";
            case EARTH -> "stone_anchor";
            case PURE -> "counterspell";
            case LIFE -> "healing_pulse";
            case DEATH -> "void_dragons_dance";
            case LIGHT -> "star_lance";
            case SHADOW -> "blink";
            case BLOOD -> "venom_blast";
            case DREAM -> "fox_dream";
            case DESTRUCTION -> "dragons_breath";
            case FORCE -> "shield";
            case SWORD -> "rippling_sword";
            case HUNGER -> "ruler_technique";
        };
        return ProjectCradle.MODID + ":" + path;
    }
}
