package net.ok.project_cradle.server.world;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.capability.core.advancement.AdvancementLevel;
import net.ok.project_cradle.capability.madra.MadraType.Element;
import net.ok.project_cradle.spells.TechniqueCatalog;
import net.ok.project_cradle.spells.ElementalTechnique;

/** Species affinities for sacred versions of vanilla mobs, with a fallback for future vanilla species. */
public final class SacredBeastProfiles {
    private SacredBeastProfiles() { }

    public record Profile(List<Element> aspects, List<String> techniques, AdvancementLevel rank) {
        public Profile {
            aspects = List.copyOf(aspects);
            techniques = List.copyOf(techniques);
        }
    }

    /** Selection happens once; the assigned aspects and skills are then retained in the entity's attachments. */
    public static Profile forMob(Mob mob) {
        Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        String species = id == null ? "" : id.getPath();
        Affinity affinity = affinity(mob, species);
        AdvancementLevel rank = rank(species);
        boolean second = rank.order() >= AdvancementLevel.UNDERLORD.order() || mob.getRandom().nextInt(4) != 0;
        String extra = second ? extraTechnique(mob, affinity) : "";
        List<String> techniques = second && !TechniqueCatalog.normalize(extra).isEmpty()
                ? List.of(TechniqueCatalog.basic(), extra) : List.of(TechniqueCatalog.basic());
        return new Profile(affinity.aspects(), techniques, rank);
    }

    private static String extraTechnique(Mob mob, Affinity affinity) {
        String original = ProjectCradle.MODID + ":" + affinity.technique();
        // Keep the species' familiar skill most of the time, with one elemental alternative.
        if (mob.getRandom().nextInt(3) != 0) return original;
        Element aspect = affinity.aspects().get(mob.getRandom().nextInt(affinity.aspects().size()));
        for (ElementalTechnique technique : ElementalTechnique.values()) {
            if (technique.aspect() != aspect) continue;
            String alternative = TechniqueCatalog.normalize(ProjectCradle.MODID + ":" + technique.id());
            if (!alternative.isEmpty()) return alternative;
        }
        return original;
    }

    private static AdvancementLevel rank(String species) {
        return switch (species) {
            case "ender_dragon", "wither" -> AdvancementLevel.ARCHLORD;
            case "warden" -> AdvancementLevel.OVERLORD;
            case "elder_guardian" -> AdvancementLevel.UNDERLORD;
            case "ravager", "iron_golem", "giant" -> AdvancementLevel.TRUEGOLD;
            case "evoker", "illusioner", "piglin_brute", "shulker", "creaking" -> AdvancementLevel.HIGHGOLD;
            default -> AdvancementLevel.LOWGOLD;
        };
    }

    private static Affinity affinity(Mob mob, String species) {
        return switch (species) {
            // Herd animals, companions, and intelligent villagers retain their ordinary behavior.
            case "cow", "mooshroom", "pig", "sheep", "rabbit", "panda", "sniffer" -> of(Element.LIFE, "healing_pulse");
            case "armadillo", "turtle" -> of(Element.EARTH, "stone_anchor");
            case "goat", "camel", "donkey", "mule", "llama", "trader_llama" -> of(Element.EARTH, Element.FORCE, "shield");
            case "horse" -> of(Element.AIR, "grasping_sky");
            case "chicken", "parrot", "bat" -> of(Element.AIR, "cutting_wind");
            case "bee" -> of(Element.LIFE, Element.BLOOD, "venom_needles");
            case "wolf" -> of(Element.SWORD, "rippling_sword");
            case "cat", "ocelot" -> of(Element.SHADOW, "blink");
            case "fox" -> of(Element.DREAM, Element.LIGHT, "fox_dream");
            case "polar_bear" -> of(Element.WATER, Element.FORCE, "ice_lance");
            case "villager", "wandering_trader" -> of(Element.PURE, "shield");
            case "allay" -> of(Element.PURE, Element.LIFE, "healing_pulse");
            case "copper_golem" -> of(Element.FORCE, Element.EARTH, "shield");
            case "iron_golem" -> of(Element.SWORD, Element.FORCE, "endless_sword");
            case "snow_golem" -> of(Element.WATER, "ice_lance");

            // Aquatic species keep their swimming AI, with water and coastal affinities.
            case "cod", "salmon", "tropical_fish", "tadpole", "nautilus" -> of(Element.WATER, "ice_lance");
            case "axolotl", "dolphin", "frog" -> of(Element.WATER, Element.LIFE, "healing_pulse");
            case "pufferfish" -> of(Element.WATER, Element.BLOOD, "venom_blast");
            case "squid" -> of(Element.WATER, Element.SHADOW, "blink");
            case "glow_squid" -> of(Element.WATER, Element.LIGHT, "star_lance");
            case "guardian", "elder_guardian" -> of(Element.WATER, Element.LIGHT, "star_lance");
            case "drowned", "zombie_nautilus" -> of(Element.WATER, Element.DEATH, "ice_lance");

            // Undead and wilderness monsters receive skills suited to their existing attacks.
            case "zombie", "zombie_villager", "zombie_horse", "giant" -> of(Element.DEATH, "enforcer_technique");
            case "husk", "camel_husk", "parched" -> of(Element.EARTH, Element.DEATH, "stone_anchor");
            case "skeleton", "skeleton_horse" -> of(Element.DEATH, Element.SWORD, "forged_needles");
            case "stray" -> of(Element.WATER, Element.DEATH, "ice_lance");
            case "bogged" -> of(Element.BLOOD, Element.DEATH, "venom_needles");
            case "spider", "cave_spider", "silverfish" -> of(Element.BLOOD, Element.SHADOW, "venom_mist");
            case "creeper" -> of(Element.DESTRUCTION, "fireball");
            case "slime" -> of(Element.FORCE, "shield");
            case "sulfur_cube" -> of(Element.EARTH, Element.FIRE, "fireball");
            case "witch" -> of(Element.BLOOD, Element.DREAM, "venom_blast");
            case "pillager", "vindicator" -> of(Element.SWORD, "rippling_sword");
            case "evoker" -> of(Element.FORCE, Element.DEATH, "gravity_well");
            case "illusioner" -> of(Element.DREAM, Element.LIGHT, "phantom_lights");
            case "vex" -> of(Element.PURE, Element.SWORD, "hidden_blade");
            case "ravager" -> of(Element.FORCE, Element.EARTH, "wind_pressure");
            case "phantom" -> of(Element.AIR, Element.SHADOW, "cutting_wind");
            case "breeze" -> of(Element.AIR, Element.FORCE, "wind_pressure");
            case "creaking" -> of(Element.LIFE, Element.SHADOW, "stone_anchor");
            case "warden" -> of(Element.DEATH, Element.FORCE, "gravity_well");

            // Nether and End species retain their vanilla immunities and movement.
            case "blaze", "ghast", "magma_cube" -> of(Element.FIRE, Element.DESTRUCTION, "fireball");
            case "strider" -> of(Element.FIRE, "burning_cloak");
            case "happy_ghast" -> of(Element.AIR, "forged_wings");
            case "piglin", "piglin_brute" -> of(Element.SWORD, Element.FIRE, "flowing_sword");
            case "zombified_piglin", "zoglin" -> of(Element.DEATH, Element.BLOOD, "venom_blast");
            case "hoglin" -> of(Element.FORCE, Element.BLOOD, "enforcer_technique");
            case "wither_skeleton" -> of(Element.DEATH, Element.SWORD, "endless_sword");
            case "wither" -> of(Element.DEATH, Element.DESTRUCTION, "void_dragons_dance");
            case "enderman", "endermite" -> of(Element.SHADOW, "blink");
            case "shulker" -> of(Element.FORCE, "gravity_well");
            case "ender_dragon" -> of(Element.DESTRUCTION, Element.SHADOW, "dragons_breath");
            default -> fallback(mob);
        };
    }

    private static Affinity fallback(Mob mob) {
        return switch (mob.getType().getCategory()) {
            case AXOLOTLS, WATER_CREATURE, WATER_AMBIENT, UNDERGROUND_WATER_CREATURE -> of(Element.WATER, "ice_lance");
            case AMBIENT -> of(Element.AIR, "cutting_wind");
            case MONSTER -> of(Element.FORCE, "enforcer_technique");
            default -> of(Element.LIFE, "healing_pulse");
        };
    }

    private static Affinity of(Element primary, String technique) {
        return new Affinity(List.of(primary), technique);
    }

    private static Affinity of(Element primary, Element secondary, String technique) {
        return new Affinity(List.of(primary, secondary), technique);
    }

    private record Affinity(List<Element> aspects, String technique) { }
}
