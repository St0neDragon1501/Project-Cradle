package net.ok.project_cradle.spells;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.ok.project_cradle.capability.madra.MadraType;
import net.ok.project_cradle.items.AdditionalTechniqueItem;
import net.ok.project_cradle.items.BasicStrikerItem;
import net.ok.project_cradle.items.EnforcerTechniqueItem;
import net.ok.project_cradle.items.ElementalTechniqueItem;
import net.ok.project_cradle.items.ForgerTechniqueItem;
import net.ok.project_cradle.items.PathTechniqueItem;
import net.ok.project_cradle.items.RulerTechniqueItem;

/** Shared display and preparation metadata; the spell remains responsible for payment. */
public record TechniqueCastProfile(int castTimeTicks, double madraCost, double soulfireCost,
        int cooldownTicks, MadraType.Element aspect) {
    /** A null aspect uses the caster's primary aspect. Unsupported items return null. */
    public static TechniqueCastProfile fromItemStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        if (stack.getItem() instanceof AdditionalTechniqueItem item) {
            AdditionalTechnique technique = item.getTechnique();
            return new TechniqueCastProfile(technique.castTimeTicks(), technique.madraCost(), 0.0,
                    technique.cooldownTicks(), technique.aspect());
        }
        if (stack.getItem() instanceof ElementalTechniqueItem item) {
            ElementalTechnique technique = item.getTechnique();
            return new TechniqueCastProfile(technique.castTimeTicks(), technique.madraCost(), 0.0,
                    technique.cooldownTicks(), technique.aspect());
        }
        if (stack.getItem() instanceof PathTechniqueItem item) {
            PathTechnique technique = item.getTechnique();
            return new TechniqueCastProfile(pathCastTime(technique), technique.madraCost(),
                    technique.soulfireCost(), technique.cooldownTicks(), technique.aspect());
        }
        if (stack.getItem() instanceof BasicStrikerItem) return new TechniqueCastProfile(6, 10.0, 0.0, 10, null);
        if (stack.getItem() instanceof ForgerTechniqueItem) return new TechniqueCastProfile(12, 18.0, 0.0, 140, null);
        if (stack.getItem() instanceof EnforcerTechniqueItem) return new TechniqueCastProfile(0, 22.0, 0.0, 180, null);
        if (stack.getItem() instanceof RulerTechniqueItem) return new TechniqueCastProfile(16, 26.0, 0.0, 80, null);
        return null;
    }

    /** Stored metadata stays at base cost; resolve against current progression at the point of use. */
    public double madraCost(Player player) {
        return TechniqueScaling.madraCost(player, this.madraCost);
    }

    private static int pathCastTime(PathTechnique technique) {
        return switch (technique) {
            case BURNING_CLOAK, FLOWING_SWORD, FLOWING_STARLIGHT, STARS_EDGE,
                    SERPENTS_SHADOW, STONE_ANCHOR, FORGED_WINGS -> 0;
            case VOID_DRAGONS_DANCE, DRAGON_DESCENDS, FINAL_SWORD -> 24;
            case FOX_DREAM, FOX_MIRROR, ENDLESS_SWORD -> 20;
            case VENOM_MIST, WIND_PRESSURE, GRASPING_SKY, OATHSIGN -> 16;
            case DRAGONS_BREATH, VENOM_BLAST, FORGED_NEEDLES, FOX_TAIL -> 14;
            case INVISIBLE_FLAME, PHANTOM_LIGHTS, STAR_LANCE -> 12;
            default -> 10;
        };
    }
}
