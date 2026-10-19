package net.ok.project_cradle.items;

import java.util.Arrays;
import java.util.List;



import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.spells.PathTechnique;
import net.ok.project_cradle.capability.core.IronBodyType;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;


public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ProjectCradle.MODID);

    public static final DeferredItem<Item> SOUL_CRUSHER = ITEMS.registerItem("soul_crusher",
            properties -> new SoulCrusherItem(properties.stacksTo(1)));
    
    public static final DeferredItem<Item> TATTERED_MANUAL = ITEMS.registerItem("tattered_manual",
            properties -> new TatteredManualItem(properties.stacksTo(1)));
    
    public static final DeferredItem<Item> MADRA_SCALE = ITEMS.registerItem("madra_scale",
            properties -> new Item(properties.stacksTo(1)));
    
    public static final DeferredItem<Item> MADRA_DETETION = ITEMS.registerItem("madra_dertetion",
            properties -> new MadraDetectionItem(properties.stacksTo(1)));

    public static final DeferredItem<Item> BASIC_STRIKER = ITEMS.registerItem("basic_striker",
            properties -> new BasicStrikerItem(properties.stacksTo(1)));

    public static final List<DeferredItem<Item>> PATH_TECHNIQUES = Arrays.stream(PathTechnique.values())
            .map(ModItems::registerTechnique)
            .toList();

    public static final DeferredItem<Item> FORGER_TECHNIQUE = ITEMS.registerItem("forger_technique",
            properties -> new ForgerTechniqueItem(properties.stacksTo(1)));

    public static final DeferredItem<Item> ENFORCER_TECHNIQUE = ITEMS.registerItem("enforcer_technique",
            properties -> new EnforcerTechniqueItem(properties.stacksTo(1)));

    public static final DeferredItem<Item> RULER_TECHNIQUE = ITEMS.registerItem("ruler_technique",
            properties -> new RulerTechniqueItem(properties.stacksTo(1)));

    public static final DeferredItem<Item> MADRA_TYPE_TUNER = ITEMS.registerItem("madra_type_tuner",
            properties -> new MadraTypeTunerItem(properties.stacksTo(1)));

    public static final DeferredItem<Item> FOUNDATION_CYCLING_TECHNIQUE = ITEMS.registerItem(
            "foundation_cycling_technique",
            properties -> new FoundationCyclingTechniqueItem(properties.stacksTo(1))
    );

    public static final DeferredItem<Item> IRON_BODY_ELIXIR = ITEMS.registerItem(
            "iron_body_elixir",
            properties -> new IronBodyElixirItem(properties.stacksTo(16))
    );

    public static final DeferredItem<Item> REMNANT_BINDING = ITEMS.registerItem(
            "remnant_binding",
            properties -> new RemnantBindingItem(properties.stacksTo(16))
    );

    public static final DeferredItem<Item> ICON_ECHO = ITEMS.registerItem(
            "icon_echo",
            properties -> new IconEchoItem(properties.stacksTo(16))
    );

    public static final DeferredItem<Item> SOULFIRE_PHIAL = ITEMS.registerItem(
            "soulfire_phial",
            properties -> new SoulfirePhialItem(properties.stacksTo(16))
    );

    public static final DeferredItem<Item> SACRED_HERB = ITEMS.registerItem(
            "sacred_herb",
            properties -> new SacredHerbItem(properties.stacksTo(64))
    );
  
    
    public static final DeferredItem<Item> BASIC_IRON_BODY_ELIXIR = ITEMS.registerItem("basic_iron_body_elixir",
            properties -> new IronBodyElixirItem(properties.stacksTo(16), IronBodyType.BASIC));

    public static final DeferredItem<Item> BLOODFORGED_IRON_BODY_ELIXIR = ITEMS.registerItem("bloodforged_iron_body_elixir",
            properties -> new IronBodyElixirItem(properties.stacksTo(16), IronBodyType.BLOODFORGED));

    public static final DeferredItem<Item> RAINDROP_IRON_BODY_ELIXIR = ITEMS.registerItem("raindrop_iron_body_elixir",
            properties -> new IronBodyElixirItem(properties.stacksTo(16), IronBodyType.RAINDROP));

    public static final DeferredItem<Item> STEELFORGED_IRON_BODY_ELIXIR = ITEMS.registerItem("steelforged_iron_body_elixir",
            properties -> new IronBodyElixirItem(properties.stacksTo(16), IronBodyType.STEELFORGED));

    public static final DeferredItem<Item> PUPPETEER_IRON_BODY_ELIXIR = ITEMS.registerItem("puppeteer_iron_body_elixir",
            properties -> new IronBodyElixirItem(properties.stacksTo(16), IronBodyType.PUPPETEER));

    public static final DeferredItem<Item> SACRED_CONTRACT = ITEMS.registerItem("sacred_contract",
            properties -> new SacredContractItem(properties.stacksTo(1)));

    public static final DeferredItem<Item> SPIRIT_MIRROR = ITEMS.registerItem("spirit_mirror",
            properties -> new SpiritMirrorItem(properties.stacksTo(1)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    private static DeferredItem<Item> registerTechnique(PathTechnique technique) {
        return ITEMS.registerItem(technique.id(),
                properties -> new PathTechniqueItem(properties.stacksTo(1), technique));
    }
}
