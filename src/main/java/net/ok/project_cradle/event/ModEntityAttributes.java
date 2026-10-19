package net.ok.project_cradle.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.entities.ModEntities;
import net.ok.project_cradle.entities.RemnantEntity;

/** Attributes must exist before a saved, summoned, or egg-spawned custom mob is constructed. */
@EventBusSubscriber(modid = ProjectCradle.MODID)
public final class ModEntityAttributes {
    private ModEntityAttributes() { }

    @SubscribeEvent
    public static void createAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.REMNANT.get(), RemnantEntity.createAttributes().build());
    }
}
