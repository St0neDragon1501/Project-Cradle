package net.ok.project_cradle.client;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.entities.ModEntities;
import net.ok.project_cradle.keymapping.ModKeyMappings;
import net.ok.project_cradle.menus.ModMenus;

@EventBusSubscriber(modid = ProjectCradle.MODID, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.registerCategory(ModKeyMappings.CATEGORY);
        event.register(ModKeyMappings.PRESS_CYCLING.get());
        ModKeyMappings.QUICK_DROP_BINDINGS.forEach(event::register);
    }

    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.QUICK_DROP.get(), QuickDropScreen::new);
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
                ModEntities.BASIC_STRIKER_PROJECTILE.get(),
                context -> new ThrownItemRenderer<>(context, 0.65F, true)
        );
    }
}
