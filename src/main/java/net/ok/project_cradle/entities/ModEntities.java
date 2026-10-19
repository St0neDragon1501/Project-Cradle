package net.ok.project_cradle.entities;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.spells.PathTechnique;

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

    /** Projectile techniques get separate entity registry IDs while sharing their common physics. */
    public static final Map<PathTechnique, Supplier<EntityType<PathTechniqueProjectile>>> PATH_PROJECTILES =
            registerPathProjectiles();

    private ModEntities() {
    }

    private static Map<PathTechnique, Supplier<EntityType<PathTechniqueProjectile>>> registerPathProjectiles() {
        List<PathTechnique> projectileTechniques = List.of(
                PathTechnique.DRAGONS_BREATH,
                PathTechnique.RIPPLING_SWORD,
                PathTechnique.HIDDEN_BLADE,
                PathTechnique.VENOM_NEEDLES,
                PathTechnique.VENOM_BLAST,
                PathTechnique.STAR_LANCE,
                PathTechnique.FORGED_NEEDLES,
                PathTechnique.CUTTING_WIND
        );
        Map<PathTechnique, Supplier<EntityType<PathTechniqueProjectile>>> projectiles =
                new EnumMap<>(PathTechnique.class);
        for (PathTechnique technique : projectileTechniques) {
            Supplier<EntityType<PathTechniqueProjectile>> projectile = ENTITIES.registerEntityType(
                    "technique_" + technique.id(),
                    PathTechniqueProjectile::new,
                    MobCategory.MISC,
                    builder -> builder.sized(0.28F, 0.28F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .noSave()
            );
            projectiles.put(technique, projectile);
        }
        return Map.copyOf(projectiles);
    }

    public static void register(IEventBus modEventBus) {
        ENTITIES.register(modEventBus);
    }
}
