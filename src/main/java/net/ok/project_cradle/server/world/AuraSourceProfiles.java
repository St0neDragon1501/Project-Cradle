package net.ok.project_cradle.server.world;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.ok.project_cradle.capability.madra.MadraType.Element;

/** Source strengths before harvesting cooldowns and cycling yield limits are applied. */
public final class AuraSourceProfiles {
    private static final Set<String> EARTH_BLOCKS = Set.of(
            "stone", "cobblestone", "mossy_cobblestone", "deepslate", "cobbled_deepslate",
            "granite", "diorite", "andesite", "tuff", "calcite", "dripstone_block",
            "pointed_dripstone", "gravel", "clay", "packed_mud", "mud_bricks",
            "bricks", "netherrack", "basalt", "smooth_basalt", "blackstone", "end_stone",
            "obsidian", "crying_obsidian", "soul_sand", "soul_soil", "bedrock",
            "suspicious_sand", "suspicious_gravel");
    private static final Set<String> MINERAL_BLOCKS = Set.of(
            "coal_block", "raw_iron_block", "raw_copper_block", "raw_gold_block", "iron_block",
            "gold_block", "diamond_block", "emerald_block", "lapis_block", "redstone_block",
            "quartz_block", "ancient_debris", "netherite_block", "amethyst_block", "budding_amethyst");
    private static final Set<String> PLANTS = Set.of(
            "short_grass", "tall_grass", "fern", "large_fern", "bush", "firefly_bush",
            "sugar_cane", "bamboo", "bamboo_sapling", "cactus", "cactus_flower", "lily_pad",
            "seagrass", "tall_seagrass", "kelp", "kelp_plant", "vine", "glow_lichen",
            "azalea", "flowering_azalea", "pink_petals", "wildflowers", "leaf_litter",
            "mangrove_roots", "muddy_mangrove_roots", "hanging_roots", "spore_blossom",
            "small_dripleaf", "big_dripleaf", "big_dripleaf_stem", "sweet_berry_bush",
            "brown_mushroom", "red_mushroom", "brown_mushroom_block", "red_mushroom_block",
            "mushroom_stem", "mycelium", "crimson_fungus", "warped_fungus", "nether_sprouts",
            "crimson_roots", "warped_roots", "chorus_plant", "chorus_flower", "nether_wart");
    private static final Set<String> MACHINES = Set.of(
            "piston", "sticky_piston", "piston_head", "moving_piston", "observer",
            "dispenser", "dropper", "hopper", "bell", "grindstone", "stonecutter",
            "blast_furnace", "crafter", "target", "heavy_weighted_pressure_plate",
            "light_weighted_pressure_plate");

    private AuraSourceProfiles() {
    }

    /** One object may carry several aspects; overlapping matches use its strongest source. */
    public static Map<Element, Integer> block(ServerLevel level, BlockPos pos, BlockState state) {
        EnumMap<Element, Integer> sources = new EnumMap<>(Element.class);
        if (state.isAir()) {
            if (level.dimensionType().hasSkyLight() && level.canSeeSky(pos)) {
                offer(sources, Element.AIR, pos.getY() >= level.getSeaLevel() + 48 ? 8 : 4);
            }
            return immutable(sources);
        }

        var blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        String name = blockId == null ? "" : blockId.getPath();
        boolean lit = state.hasProperty(BlockStateProperties.LIT) && state.getValue(BlockStateProperties.LIT);

        if (name.endsWith("_wool") || name.endsWith("_banner") || name.endsWith("_wall_banner")
                || name.equals("cobweb") || name.equals("note_block")) offer(sources, Element.AIR, 8);

        if (state.getFluidState().is(FluidTags.LAVA) || name.equals("lava_cauldron")) {
            offer(sources, Element.FIRE, 24);
        }
        if (state.is(BlockTags.FIRE)) offer(sources, Element.FIRE, 24);
        if (state.is(BlockTags.CAMPFIRES) && lit) offer(sources, Element.FIRE, 16);
        if (lit && (name.equals("furnace") || name.equals("blast_furnace") || name.equals("smoker"))) {
            offer(sources, Element.FIRE, 12);
        }
        if (name.equals("magma_block")) {
            offer(sources, Element.FIRE, 16);
            offer(sources, Element.DESTRUCTION, 16);
        }

        if (state.getFluidState().is(FluidTags.WATER) || name.equals("water_cauldron") || name.equals("wet_sponge")) {
            offer(sources, Element.WATER, 16);
        }
        if (state.is(BlockTags.ICE)) offer(sources, Element.WATER, name.equals("blue_ice") ? 24 : 12);
        if (state.is(BlockTags.SNOW) || name.equals("powder_snow") || name.equals("powder_snow_cauldron")) {
            offer(sources, Element.WATER, 8);
        }

        if (state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(BlockTags.BASE_STONE_NETHER)
                || state.is(BlockTags.DIRT) || state.is(BlockTags.SAND) || state.is(BlockTags.MUD)
                || state.is(BlockTags.TERRACOTTA) || state.is(BlockTags.CONCRETE)
                || EARTH_BLOCKS.contains(name)) {
            offer(sources, Element.EARTH, 4);
        }
        if (state.is(BlockTags.STONE_BRICKS) || name.contains("sandstone") || name.contains("deepslate")
                || name.startsWith("polished_") || name.endsWith("stone_bricks") || name.endsWith("tuff_bricks")) {
            offer(sources, Element.EARTH, 6);
        }
        if (name.endsWith("_ore") || MINERAL_BLOCKS.contains(name)) offer(sources, Element.EARTH, 16);
        if (state.is(BlockTags.COPPER)) offer(sources, Element.EARTH, 8);

        if (state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES) || state.is(BlockTags.FLOWERS)
                || state.is(BlockTags.CROPS) || state.is(BlockTags.MOSS_BLOCKS) || state.is(BlockTags.NYLIUM)
                || state.is(BlockTags.CAVE_VINES) || state.is(BlockTags.CORALS) || state.is(BlockTags.CORAL_BLOCKS)
                || state.is(BlockTags.BEEHIVES) || PLANTS.contains(name) || name.endsWith("_sapling")
                || name.endsWith("_propagule") || name.startsWith("potted_") || name.contains("weeping_vines")
                || name.contains("twisting_vines")) {
            if (!name.contains("dead_") && !name.contains("wither_")) offer(sources, Element.LIFE, 8);
        }
        if (state.is(BlockTags.GRASS_BLOCKS) || state.is(BlockTags.PLANKS) || state.is(BlockTags.BAMBOO_BLOCKS)
                || name.equals("farmland") || name.equals("rooted_dirt") || name.equals("honeycomb_block")) {
            offer(sources, Element.LIFE, 4);
        }

        if (name.equals("bone_block") || name.equals("wither_rose") || name.equals("dried_ghast")) {
            offer(sources, Element.DEATH, 24);
        }
        if (state.is(BlockTags.SOUL_FIRE_BASE_BLOCKS) || name.contains("skull")
                || (name.endsWith("_head") && !name.equals("piston_head"))
                || name.contains("soul_fire") || name.contains("soul_torch")
                || name.contains("soul_lantern") || name.equals("soul_campfire")) {
            offer(sources, Element.DEATH, name.contains("wither_skeleton") ? 32 : 16);
        }
        if (name.contains("dead_") || name.equals("dead_bush") || name.contains("dry_grass")) {
            offer(sources, Element.DEATH, 4);
        }

        int light = state.getLightEmission(level, pos);
        if (light > 0) offer(sources, Element.LIGHT, light >= 14 ? 16 : light >= 10 ? 12 : 4);
        if (name.equals("beacon")) offer(sources, Element.LIGHT, 32);
        if (name.equals("obsidian") || name.equals("tinted_glass") || name.equals("blackstone")) {
            offer(sources, Element.SHADOW, 16);
        }
        if (name.equals("crying_obsidian")) offer(sources, Element.SHADOW, 24);
        if (sources.containsKey(Element.EARTH) && pos.getY() < level.getSeaLevel() - 8
                && !level.canSeeSky(pos) && level.getMaxLocalRawBrightness(pos) <= 7) {
            offer(sources, Element.SHADOW, 4);
        }

        if (name.contains("redstone") || name.equals("nether_wart") || name.equals("nether_wart_block")) {
            offer(sources, Element.BLOOD, 16);
        }
        if (name.contains("crimson") || name.contains("weeping_vines")) offer(sources, Element.BLOOD, 8);

        if (name.contains("amethyst")) offer(sources, Element.DREAM, name.equals("amethyst_cluster") ? 32 : 24);
        if (state.is(BlockTags.BEDS) || state.is(BlockTags.ENCHANTMENT_POWER_PROVIDER)
                || name.equals("bookshelf") || name.equals("chiseled_bookshelf") || name.equals("note_block")) {
            offer(sources, Element.DREAM, 12);
        }
        if (name.equals("enchanting_table") || name.equals("end_portal") || name.equals("end_gateway")) {
            offer(sources, Element.DREAM, 24);
        }
        if (name.contains("pale_oak") || name.equals("creaking_heart")) offer(sources, Element.DREAM, 8);

        if (name.equals("tnt") || name.equals("respawn_anchor")) offer(sources, Element.DESTRUCTION, 32);
        if (name.equals("fire") || name.equals("soul_fire") || name.equals("lava") || name.equals("lava_cauldron")) {
            offer(sources, Element.DESTRUCTION, 8);
        }
        if (MACHINES.contains(name) || state.is(BlockTags.RAILS)) offer(sources, Element.FORCE, 16);
        if (state.is(BlockTags.ANVIL)) offer(sources, Element.FORCE, 24);
        if (name.equals("slime_block") || name.equals("honey_block")) offer(sources, Element.FORCE, 16);

        if (state.is(BlockTags.IRON_ORES) || state.is(BlockTags.COPPER_ORES) || state.is(BlockTags.COPPER)
                || name.startsWith("iron_") || name.startsWith("raw_iron") || name.startsWith("raw_copper")
                || name.endsWith("_chain") || name.equals("chain") || name.equals("grindstone")) {
            offer(sources, Element.SWORD, 12);
        }
        if (state.is(BlockTags.ANVIL) || name.equals("iron_block")) offer(sources, Element.SWORD, 24);
        if (name.equals("netherite_block") || name.equals("ancient_debris")) offer(sources, Element.SWORD, 32);
        if (state.is(BlockTags.GOLD_ORES) || name.equals("gold_block") || name.equals("raw_gold_block")
                || name.equals("diamond_block") || name.equals("diamond_ore") || name.equals("deepslate_diamond_ore")) {
            offer(sources, Element.SWORD, 8);
        }

        if (name.startsWith("sculk")) {
            offer(sources, Element.DREAM, 12);
            offer(sources, Element.DEATH, 8);
            offer(sources, Element.SHADOW, 12);
            offer(sources, Element.HUNGER, name.equals("sculk_catalyst") ? 32 : 16);
        }
        if (name.equals("soul_sand") || name.equals("wither_rose") || name.equals("ender_chest")) {
            offer(sources, Element.HUNGER, 16);
        }
        if (name.equals("composter")) offer(sources, Element.HUNGER, 8);
        return immutable(sources);
    }

    /** Ambient aura makes the same collection of objects yield differently by location. */
    public static Map<Element, Integer> environment(ServerPlayer player) {
        ServerLevel level = player.level();
        BlockPos pos = player.blockPosition();
        EnumMap<Element, Integer> sources = new EnumMap<>(Element.class);
        var biome = level.getBiome(pos);
        String name = biome.unwrapKey().map(key -> key.identifier().getPath()).orElse("");
        boolean sky = level.dimensionType().hasSkyLight() && level.canSeeSky(pos);
        boolean overworld = biome.is(BiomeTags.IS_OVERWORLD) || level.dimension() == Level.OVERWORLD;
        boolean nether = biome.is(BiomeTags.IS_NETHER) || level.dimension() == Level.NETHER;
        boolean end = biome.is(BiomeTags.IS_END) || level.dimension() == Level.END;

        if (sky) {
            add(sources, Element.AIR, pos.getY() >= level.getSeaLevel() + 96 ? 24
                    : pos.getY() >= level.getSeaLevel() + 48 ? 16 : 8);
            if (level.isBrightOutside()) add(sources, Element.LIGHT, 12);
            if (level.isDarkOutside()) {
                add(sources, Element.SHADOW, 12);
                add(sources, Element.DREAM, 8);
            }
            if (level.isRainingAt(pos)) add(sources, Element.WATER, 12);
            if (level.isThundering()) {
                add(sources, Element.AIR, 8);
                add(sources, Element.FORCE, 12);
                add(sources, Element.DESTRUCTION, 4);
            }
            if (biome.value().getBaseTemperature() >= 1.5F && level.isBrightOutside()) {
                add(sources, Element.FIRE, 8);
            }
        }
        if (biome.is(BiomeTags.IS_OCEAN)) add(sources, Element.WATER, 24);
        if (biome.is(BiomeTags.IS_RIVER)) add(sources, Element.WATER, 16);
        if (biome.is(BiomeTags.IS_BEACH)) {
            add(sources, Element.WATER, 8);
            add(sources, Element.EARTH, 4);
        }
        if (biome.is(BiomeTags.IS_FOREST) || biome.is(BiomeTags.IS_TAIGA)) add(sources, Element.LIFE, 16);
        if (biome.is(BiomeTags.IS_JUNGLE)) add(sources, Element.LIFE, 24);
        if (biome.is(BiomeTags.IS_SAVANNA) || name.contains("plains")) add(sources, Element.LIFE, 8);
        if (biome.is(BiomeTags.IS_MOUNTAIN) || biome.is(BiomeTags.IS_HILL)) {
            add(sources, Element.EARTH, 12);
            if (sky) {
                add(sources, Element.AIR, 12);
                add(sources, Element.FORCE, 4);
            }
        }
        if (biome.is(BiomeTags.IS_BADLANDS) || name.contains("desert")) add(sources, Element.EARTH, 16);
        if (name.contains("swamp")) {
            add(sources, Element.LIFE, 16);
            add(sources, Element.WATER, 12);
        }
        if (name.contains("snow") || name.contains("frozen") || name.equals("ice_spikes")) {
            add(sources, Element.WATER, 12);
        }
        if (name.equals("meadow") || name.equals("flower_forest") || name.equals("cherry_grove")) {
            add(sources, Element.LIFE, 16);
            add(sources, Element.DREAM, 8);
        }
        if (name.equals("mushroom_fields") || name.equals("lush_caves")) {
            add(sources, Element.LIFE, 20);
            add(sources, Element.DREAM, 8);
            add(sources, Element.WATER, 8);
        }
        if (name.equals("jagged_peaks") || name.equals("stony_peaks")) add(sources, Element.SWORD, 8);
        if (overworld && !sky && pos.getY() < level.getSeaLevel() - 8) {
            add(sources, Element.EARTH, pos.getY() < 0 ? 20 : 12);
            if (level.getMaxLocalRawBrightness(pos) <= 7) add(sources, Element.SHADOW, 12);
        }
        if (name.equals("dripstone_caves")) {
            add(sources, Element.EARTH, 12);
            add(sources, Element.WATER, 4);
        }
        if (name.equals("deep_dark")) {
            add(sources, Element.SHADOW, 16);
            add(sources, Element.DREAM, 12);
            add(sources, Element.DEATH, 12);
            add(sources, Element.HUNGER, 16);
        }
        if (name.equals("pale_garden")) {
            add(sources, Element.SHADOW, 16);
            add(sources, Element.DREAM, 16);
            add(sources, Element.DEATH, 8);
        }
        if (nether) {
            add(sources, Element.FIRE, 16);
            add(sources, Element.EARTH, 8);
        }
        if (name.equals("crimson_forest")) {
            add(sources, Element.BLOOD, 24);
            add(sources, Element.LIFE, 12);
        }
        if (name.equals("warped_forest")) {
            add(sources, Element.DREAM, 16);
            add(sources, Element.LIFE, 12);
        }
        if (name.equals("soul_sand_valley")) {
            add(sources, Element.DEATH, 24);
            add(sources, Element.SHADOW, 8);
            add(sources, Element.HUNGER, 8);
        }
        if (name.equals("basalt_deltas")) {
            add(sources, Element.FIRE, 8);
            add(sources, Element.EARTH, 12);
            add(sources, Element.DESTRUCTION, 12);
        }
        if (end) {
            add(sources, Element.AIR, 8);
            add(sources, Element.DREAM, 16);
            add(sources, Element.SHADOW, 12);
            add(sources, Element.HUNGER, pos.getY() < 32 ? 16 : 8);
        }
        return immutable(sources);
    }

    private static void offer(EnumMap<Element, Integer> sources, Element element, int amount) {
        if (element != Element.PURE && amount > 0) sources.merge(element, amount, Math::max);
    }

    private static void add(EnumMap<Element, Integer> sources, Element element, int amount) {
        if (element != Element.PURE && amount > 0) sources.merge(element, amount, Integer::sum);
    }

    private static Map<Element, Integer> immutable(EnumMap<Element, Integer> sources) {
        return Collections.unmodifiableMap(new EnumMap<>(sources));
    }
}
