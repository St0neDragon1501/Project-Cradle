package net.ok.project_cradle.spells;

import net.ok.project_cradle.capability.madra.MadraType;

/** Registered techniques drawn from the paths represented in Project Cradle. */
public enum PathTechnique {
    DRAGONS_BREATH("dragons_breath", MadraType.Element.DESTRUCTION, 22.0, 30),
    BURNING_CLOAK("burning_cloak", MadraType.Element.FIRE, 18.0, 120),
    VOID_DRAGONS_DANCE("void_dragons_dance", MadraType.Element.DESTRUCTION, 36.0, 140),
    DRAGON_DESCENDS("dragon_descends", MadraType.Element.DESTRUCTION, 45.0, 180, 1.0),

    FOX_FIRE("fox_fire", MadraType.Element.DREAM, 8.0, 50),
    INVISIBLE_FLAME("invisible_flame", MadraType.Element.FIRE, 10.0, 50),
    PHANTOM_LIGHTS("phantom_lights", MadraType.Element.LIGHT, 12.0, 70),
    FOX_TAIL("fox_tail", MadraType.Element.DREAM, 15.0, 100),
    FOX_DREAM("fox_dream", MadraType.Element.DREAM, 22.0, 100),
    FOX_MIRROR("fox_mirror", MadraType.Element.DREAM, 25.0, 140),

    RIPPLING_SWORD("rippling_sword", MadraType.Element.SWORD, 12.0, 25),
    FLOWING_SWORD("flowing_sword", MadraType.Element.SWORD, 16.0, 100),
    ENDLESS_SWORD("endless_sword", MadraType.Element.SWORD, 28.0, 100),
    HIDDEN_BLADE("hidden_blade", MadraType.Element.SWORD, 14.0, 50),
    FINAL_SWORD("final_sword", MadraType.Element.SWORD, 42.0, 180),

    VENOM_NEEDLES("venom_needles", MadraType.Element.BLOOD, 12.0, 35),
    VENOM_BLAST("venom_blast", MadraType.Element.BLOOD, 20.0, 70),
    VENOM_MIST("venom_mist", MadraType.Element.BLOOD, 24.0, 100),

    STAR_LANCE("star_lance", MadraType.Element.LIGHT, 14.0, 30),
    FLOWING_STARLIGHT("flowing_starlight", MadraType.Element.LIGHT, 16.0, 100),
    STARS_EDGE("stars_edge", MadraType.Element.LIGHT, 14.0, 80),
    SERPENTS_SHADOW("serpents_shadow", MadraType.Element.LIGHT, 20.0, 100),
    FORGED_NEEDLES("forged_needles", MadraType.Element.LIGHT, 18.0, 65),

    OATHSIGN("oathsign", MadraType.Element.FORCE, 25.0, 140),
    STONE_ANCHOR("stone_anchor", MadraType.Element.EARTH, 12.0, 100),

    CUTTING_WIND("cutting_wind", MadraType.Element.AIR, 12.0, 25),
    GRASPING_SKY("grasping_sky", MadraType.Element.AIR, 16.0, 100),
    WIND_PRESSURE("wind_pressure", MadraType.Element.AIR, 25.0, 110),
    FORGED_WINGS("forged_wings", MadraType.Element.AIR, 24.0, 140);

    private final String id;
    private final MadraType.Element aspect;
    private final double madraCost;
    private final int cooldownTicks;
    private final double soulfireCost;

    PathTechnique(String id, MadraType.Element aspect, double madraCost, int cooldownTicks) {
        this(id, aspect, madraCost, cooldownTicks, 0.0);
    }

    PathTechnique(String id, MadraType.Element aspect, double madraCost, int cooldownTicks, double soulfireCost) {
        this.id = id;
        this.aspect = aspect;
        this.madraCost = madraCost;
        this.cooldownTicks = cooldownTicks;
        this.soulfireCost = soulfireCost;
    }

    public String id() {
        return this.id;
    }

    public MadraType.Element aspect() {
        return this.aspect;
    }

    public double madraCost() {
        return this.madraCost;
    }

    public int cooldownTicks() {
        return this.cooldownTicks;
    }

    public double soulfireCost() {
        return this.soulfireCost;
    }
}
