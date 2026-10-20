package net.ok.project_cradle;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue SACRED_BEAST_NATURAL_SPAWN_CHANCE = BUILDER
            .comment("Chance for a newly spawned vanilla mob to be a sacred variant, including natural spawns and world generation.",
                    "The default 0.001 is one sacred variant per 1,000 new mob spawns on average.",
                    "Existing mobs are not rerolled when their chunks load. This setting supersedes sacredBeastSpawnChance.")
            .defineInRange("sacredBeastNaturalSpawnChance", 0.001, 0.0, 1.0);

    /** Kept so an older config can be loaded without dropping its former value. */
    @Deprecated
    public static final ModConfigSpec.DoubleValue SACRED_BEAST_SPAWN_CHANCE = BUILDER
            .comment("Legacy value retained for compatibility. Use sacredBeastNaturalSpawnChance to control sacred variant rarity.")
            .defineInRange("sacredBeastSpawnChance", 0.02, 0.0, 1.0);

    public static double sacredBeastSpawnChance() {
        return SACRED_BEAST_NATURAL_SPAWN_CHANCE.get();
    }

    public static final ModConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER
            .comment("Whether to log the dirt block on common setup")
            .define("logDirtBlock", true);

    public static final ModConfigSpec.IntValue MAGIC_NUMBER = BUILDER
            .comment("A magic number")
            .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
            .comment("What you want the introduction message to be for the magic number")
            .define("magicNumberIntroduction", "The magic number is... ");

    // a list of strings that are treated as resource locations for items
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
            .comment("A list of items to log on common setup.")
            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), () -> "", Config::validateItemName);

    static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(Identifier.parse(itemName));
    }
}
