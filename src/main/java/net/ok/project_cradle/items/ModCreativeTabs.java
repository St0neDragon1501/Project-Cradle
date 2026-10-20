package net.ok.project_cradle.items;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.ok.project_cradle.ProjectCradle;

/** The dedicated creative inventory tab for Project Cradle content. */
public final class ModCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ProjectCradle.MODID);

    public static final Supplier<CreativeModeTab> PROJECT_CRADLE =
            CREATIVE_TABS.register("project_cradle", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.project_cradle.project_cradle"))
                    .icon(() -> new ItemStack(ModItems.FOUNDATION_CYCLING_TECHNIQUE.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.FOUNDATION_CYCLING_TECHNIQUE);
                        output.accept(ModItems.TATTERED_MANUAL);
                        output.accept(ModItems.MADRA_SCALE);
                        output.accept(ModItems.MADRA_DETETION);
                        output.accept(ModItems.MADRA_TYPE_TUNER);
                        output.accept(ModItems.BASIC_STRIKER);
                        ModItems.PATH_TECHNIQUES.forEach(output::accept);
                        ModItems.ADDITIONAL_TECHNIQUES.forEach(output::accept);
                        ModItems.ELEMENTAL_TECHNIQUES.forEach(output::accept);
                        output.accept(ModItems.FORGER_TECHNIQUE);
                        output.accept(ModItems.ENFORCER_TECHNIQUE);
                        output.accept(ModItems.RULER_TECHNIQUE);
                        output.accept(ModItems.IRON_BODY_ELIXIR);
                        output.accept(ModItems.BASIC_IRON_BODY_ELIXIR);
                        output.accept(ModItems.BLOODFORGED_IRON_BODY_ELIXIR);
                        output.accept(ModItems.RAINDROP_IRON_BODY_ELIXIR);
                        output.accept(ModItems.STEELFORGED_IRON_BODY_ELIXIR);
                        output.accept(ModItems.PUPPETEER_IRON_BODY_ELIXIR);
                        output.accept(ModItems.SACRED_CONTRACT);
                        output.accept(ModItems.SPIRIT_MIRROR);
                        output.accept(ModItems.REMNANT_BINDING);
                        output.accept(ModItems.REMNANT_SPAWN_EGG);
                        output.accept(ModItems.ICON_ECHO);
                        output.accept(ModItems.SOULFIRE_PHIAL);
                        output.accept(ModItems.SACRED_HERB);
                    })
                    .build());

    private ModCreativeTabs() {
    }

    public static void register(IEventBus modEventBus) {
        CREATIVE_TABS.register(modEventBus);
    }
}
