package net.ok.project_cradle.server.spells;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.ok.project_cradle.entities.RemnantEntity;

/** Remnant entry points retained while sacred creatures share the same server combat implementation. */
public final class RemnantTechniqueService {
    private RemnantTechniqueService() { }

    public static void tick(RemnantEntity caster) { MobTechniqueService.tick(caster); }
    public static boolean cancel(RemnantEntity caster) { return MobTechniqueService.cancel(caster); }
    public static boolean canHarm(RemnantEntity caster, LivingEntity target) { return MobTechniqueService.canHarm(caster, target); }
    public static boolean hurt(RemnantEntity caster, Entity direct, LivingEntity target, float damage) {
        return MobTechniqueService.hurt(caster, direct, target, damage);
    }
    public static double power(RemnantEntity caster) { return MobTechniqueService.power(caster); }
    public static double range(RemnantEntity caster, double base) { return MobTechniqueService.range(caster, base); }
    public static boolean isCharmed(Mob mob) { return MobTechniqueService.isCharmed(mob); }
}
