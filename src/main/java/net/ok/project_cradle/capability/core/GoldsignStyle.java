package net.ok.project_cradle.capability.core;

import java.util.List;
import java.util.Locale;
import net.ok.project_cradle.capability.madra.MadraType;

/** Cosmetic Goldsign forms, independent of the aspect that supplies a Goldsign's benefits. */
public enum GoldsignStyle {
    AUTO("auto"),
    METAL_HAIR("metal_hair"),
    METAL_BRACER("metal_bracer"),
    WEBBED_FINGERS("webbed_fingers"),
    ARM_SERPENT("arm_serpent"),
    WINGS("wings"),
    BLACKFLAME_EYES("blackflame_eyes"),
    CLOUD_SWORDS("cloud_swords"),
    FOX_TAIL("fox_tail");

    private final String id;

    GoldsignStyle(String id) {
        this.id = id;
    }

    public String id() {
        return this.id;
    }

    public String translationKey() {
        return "goldsign_style.project_cradle." + this.id;
    }

    /** Unknown save values retain the automatic appearance. Commands use byId to reject unknown input. */
    public static GoldsignStyle fromId(String id) {
        GoldsignStyle style = byId(id);
        return style == null ? AUTO : style;
    }

    public static GoldsignStyle byId(String id) {
        if (id == null) return null;
        String normalized = id.trim().toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        return switch (normalized) {
            case "auto" -> AUTO;
            case "metal_hair", "jai" -> METAL_HAIR;
            case "metal_bracer", "cassias" -> METAL_BRACER;
            case "webbed_fingers", "fisher", "fishers" -> WEBBED_FINGERS;
            case "arm_serpent", "sandviper", "sandpiper" -> ARM_SERPENT;
            case "wings", "naru" -> WINGS;
            case "blackflame_eyes", "blackflame" -> BLACKFLAME_EYES;
            case "cloud_swords", "cloud_hammer" -> CLOUD_SWORDS;
            case "fox_tail", "fox", "tail", "white_fox" -> FOX_TAIL;
            default -> null;
        };
    }

    public static List<String> commandIds() {
        return List.of("auto", "metal_hair", "metal_bracer", "webbed_fingers", "arm_serpent", "wings",
                "blackflame_eyes", "cloud_swords", "jai", "cassias", "fisher", "sandviper", "sandpiper",
                "naru", "blackflame", "cloud_hammer", "fox_tail", "fox", "tail", "white_fox");
    }

    /** Resolve on the server so observers do not need access to another player's private madra types. */
    public static GoldsignStyle resolve(Core core, MadraType madraType) {
        if (core == null || core.getGoldsign().isBlank()) return AUTO;
        if (core.getGoldsignStyle() != AUTO) return core.getGoldsignStyle();
        List<MadraType.Element> aspects = madraType == null ? List.of() : madraType.getTypes();
        if (aspects.contains(MadraType.Element.FIRE) && aspects.contains(MadraType.Element.DESTRUCTION)) {
            return BLACKFLAME_EYES;
        }
        if (aspects.contains(MadraType.Element.AIR) && aspects.contains(MadraType.Element.FORCE)) {
            return CLOUD_SWORDS;
        }
        if (aspects.contains(MadraType.Element.SWORD) && aspects.contains(MadraType.Element.LIGHT)) {
            return METAL_HAIR;
        }
        if (aspects.contains(MadraType.Element.DREAM)) {
            return FOX_TAIL;
        }
        MadraType.Element primary = aspects.isEmpty() ? null : aspects.get(0);
        if (primary == null) primary = MadraType.Element.byId(core.getGoldsign());
        if (primary == null) return METAL_BRACER;
        return switch (primary) {
            case AIR -> WINGS;
            case FORCE, WATER -> WEBBED_FINGERS;
            case LIFE, BLOOD, SHADOW, HUNGER -> ARM_SERPENT;
            case LIGHT -> METAL_HAIR;
            case SWORD, EARTH, PURE -> METAL_BRACER;
            case FIRE, DESTRUCTION, DEATH -> BLACKFLAME_EYES;
            case DREAM -> FOX_TAIL;
        };
    }
}
