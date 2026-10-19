package net.ok.project_cradle.entities;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** A named Path projectile with its own registry identity and item appearance. */
public final class PathTechniqueProjectile extends BasicStrikerProjectile {
    public PathTechniqueProjectile(EntityType<? extends BasicStrikerProjectile> type, Level level) {
        super(type, level);
    }

    public PathTechniqueProjectile(EntityType<? extends BasicStrikerProjectile> type, Level level,
            LivingEntity owner, ItemStack visualStack) {
        super(type, level, owner, visualStack);
    }
}
