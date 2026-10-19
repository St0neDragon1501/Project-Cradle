package net.ok.project_cradle.spells;

import net.ok.project_cradle.capability.madra.MadraType;

/** Distinct general-purpose spells, with windup owned by the common casting service. */
public enum AdditionalTechnique {
    SHIELD("shield", MadraType.Element.FORCE, 30.0, 160, 0),
    FIREBALL("fireball", MadraType.Element.FIRE, 28.0, 60, 24),
    ICE_LANCE("ice_lance", MadraType.Element.WATER, 18.0, 40, 12),
    CHAIN_LIGHTNING("chain_lightning", MadraType.Element.LIGHT, 32.0, 100, 20),
    BLINK("blink", MadraType.Element.SHADOW, 20.0, 80, 0),
    HEALING_PULSE("healing_pulse", MadraType.Element.LIFE, 25.0, 140, 20),
    COUNTERSPELL("counterspell", MadraType.Element.PURE, 20.0, 80, 0),
    GRAVITY_WELL("gravity_well", MadraType.Element.FORCE, 35.0, 160, 24);

    private final String id;
    private final MadraType.Element aspect;
    private final double madraCost;
    private final int cooldownTicks;
    private final int castTimeTicks;

    AdditionalTechnique(String id, MadraType.Element aspect, double madraCost,
            int cooldownTicks, int castTimeTicks) {
        this.id = id;
        this.aspect = aspect;
        this.madraCost = madraCost;
        this.cooldownTicks = cooldownTicks;
        this.castTimeTicks = castTimeTicks;
    }

    public String id() { return this.id; }
    public MadraType.Element aspect() { return this.aspect; }
    public double madraCost() { return this.madraCost; }
    public double soulfireCost() { return 0.0; }
    public int cooldownTicks() { return this.cooldownTicks; }
    public int castTimeTicks() { return this.castTimeTicks; }
    public String descriptionTranslationKey() { return "tooltip.project_cradle." + this.id; }
}
