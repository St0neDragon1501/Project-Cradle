package net.ok.project_cradle.entities;

import java.util.function.Supplier;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.ok.project_cradle.ProjectCradle;

public final class ModEntities {
    private static final DeferredRegister.Entities ENTITIES =
            DeferredRegister.createEntities(ProjectCradle.MODID);

    public static final Supplier<EntityType<BasicStrikerProjectile>> BASIC_STRIKER_PROJECTILE =
            ENTITIES.registerEntityType(
                    "basic_striker",
                    BasicStrikerProjectile::new,
                    MobCategory.MISC,
                    builder -> builder.sized(0.3F, 0.3F)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .noSave()
            );

    private ModEntities() {
    }

    public static void register(IEventBus modEventBus) {
        ENTITIES.register(modEventBus);
    }
}
