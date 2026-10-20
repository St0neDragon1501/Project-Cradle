package net.ok.project_cradle.spells;

import net.ok.project_cradle.capability.madra.MadraType;

/** The elemental technique manuals share preparation and progression scaling with every other spell. */
public enum ElementalTechnique {
    FLAME_RING("flame_ring", MadraType.Element.FIRE, 24.0, 100, 12, 5.0),
    FROST_NOVA("frost_nova", MadraType.Element.WATER, 26.0, 120, 16, 5.0),
    GALE_STEP("gale_step", MadraType.Element.AIR, 16.0, 60, 0, 7.0),
    STONE_AEGIS("stone_aegis", MadraType.Element.EARTH, 28.0, 220, 16, 0.0),
    SPIRIT_SEVER("spirit_sever", MadraType.Element.PURE, 24.0, 100, 12, 14.0),
    BLOOM_FIELD("bloom_field", MadraType.Element.LIFE, 32.0, 200, 20, 5.0),
    WITHERING_TOUCH("withering_touch", MadraType.Element.DEATH, 22.0, 90, 12, 6.0),
    SOLAR_BEAM("solar_beam", MadraType.Element.LIGHT, 30.0, 100, 20, 24.0),
    NIGHT_VEIL("night_veil", MadraType.Element.SHADOW, 20.0, 180, 0, 0.0),
    CRIMSON_SIPHON("crimson_siphon", MadraType.Element.BLOOD, 26.0, 120, 16, 14.0),
    DREAM_SNARE("dream_snare", MadraType.Element.DREAM, 24.0, 120, 16, 12.0),
    RUIN_BURST("ruin_burst", MadraType.Element.DESTRUCTION, 34.0, 140, 24, 18.0),
    REPULSION_WAVE("repulsion_wave", MadraType.Element.FORCE, 22.0, 100, 12, 6.0),
    BLADE_TEMPEST("blade_tempest", MadraType.Element.SWORD, 32.0, 180, 16, 5.0),
    MADRA_LEECH("madra_leech", MadraType.Element.HUNGER, 24.0, 120, 20, 12.0);

    private final String id;
    private final MadraType.Element aspect;
    private final double madraCost;
    private final int cooldownTicks;
    private final int castTimeTicks;
    private final double baseRange;

    ElementalTechnique(String id, MadraType.Element aspect, double madraCost,
            int cooldownTicks, int castTimeTicks, double baseRange) {
        this.id = id;
        this.aspect = aspect;
        this.madraCost = madraCost;
        this.cooldownTicks = cooldownTicks;
        this.castTimeTicks = castTimeTicks;
        this.baseRange = baseRange;
    }

    public String id() { return this.id; }
    public MadraType.Element aspect() { return this.aspect; }
    public double madraCost() { return this.madraCost; }
    public int cooldownTicks() { return this.cooldownTicks; }
    public int castTimeTicks() { return this.castTimeTicks; }
    public double baseRange() { return this.baseRange; }
    public String descriptionTranslationKey() { return "tooltip.project_cradle." + this.id; }
}
