package net.ok.project_cradle.event;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.items.ModItems;

@EventBusSubscriber(modid = ProjectCradle.MODID)
public final class CreativeTabEvents {
    private CreativeTabEvents() {
    }

    @SubscribeEvent
    public static void addModItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ModItems.BASIC_STRIKER);
            ModItems.PATH_TECHNIQUES.forEach(event::accept);
            event.accept(ModItems.FORGER_TECHNIQUE);
            event.accept(ModItems.ENFORCER_TECHNIQUE);
            event.accept(ModItems.RULER_TECHNIQUE);
            event.accept(ModItems.MADRA_TYPE_TUNER);
        }

        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.TATTERED_MANUAL);
            event.accept(ModItems.MADRA_SCALE);
            event.accept(ModItems.MADRA_DETETION);
            event.accept(ModItems.FOUNDATION_CYCLING_TECHNIQUE);
            event.accept(ModItems.IRON_BODY_ELIXIR);
            event.accept(ModItems.REMNANT_BINDING);
            event.accept(ModItems.ICON_ECHO);
            event.accept(ModItems.SOULFIRE_PHIAL);
            event.accept(ModItems.SACRED_HERB);
        }
    }
}
