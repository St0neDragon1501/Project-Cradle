package net.ok.project_cradle;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.entities.ModEntities;
import net.ok.project_cradle.items.ModItems;
import net.ok.project_cradle.items.ModCreativeTabs;
import net.ok.project_cradle.menus.ModMenus;

@Mod(ProjectCradle.MODID)
public final class ProjectCradle {
    public static final String MODID = "project_cradle";

    public ProjectCradle(IEventBus modEventBus, ModContainer modContainer) {
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        ModEntities.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModMenus.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}
