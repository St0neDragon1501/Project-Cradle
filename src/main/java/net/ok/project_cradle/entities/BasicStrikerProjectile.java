package net.ok.project_cradle.entities;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.DustParticleOptions;
import net.ok.project_cradle.server.spells.TechniqueVisualService;
import net.ok.project_cradle.server.spells.MobTechniqueService;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.ok.project_cradle.capability.madra.MadraType;
import net.ok.project_cradle.items.ModItems;
import net.ok.project_cradle.server.spells.MadraAspectEffects;
import net.ok.project_cradle.server.spells.TechniqueCombatService;
import net.ok.project_cradle.server.spells.ProjectileTerrainPassives;

public class BasicStrikerProjectile extends ThrowableItemProjectile {
    private static final EntityDataAccessor<Integer> DATA_MADRA_ELEMENT = SynchedEntityData.defineId(
            BasicStrikerProjectile.class,
            EntityDataSerializers.INT
    );
    private static final EntityDataAccessor<Float> DATA_SPELL_DAMAGE = SynchedEntityData.defineId(
            BasicStrikerProjectile.class,
            EntityDataSerializers.FLOAT
    );

    private double maxTravelDistance = 5.0;
    private double distanceTravelled;
    private boolean burnOnHit;

    public BasicStrikerProjectile(EntityType<? extends BasicStrikerProjectile> type, Level level) {
        super(type, level);
    }

    public BasicStrikerProjectile(Level level, LivingEntity owner, ItemStack itemStack) {
        this(ModEntities.BASIC_STRIKER_PROJECTILE.get(), level, owner, itemStack);
    }

    public BasicStrikerProjectile(EntityType<? extends BasicStrikerProjectile> type, Level level,
            LivingEntity owner, ItemStack itemStack) {
        super(type, owner, level, itemStack);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.BASIC_STRIKER.get();
    }

    public void setMadraElement(MadraType.Element element) {
        this.getEntityData().set(DATA_MADRA_ELEMENT, element.ordinal());
    }

    public MadraType.Element getMadraElement() {
        return MadraType.Element.byOrdinal(this.getEntityData().get(DATA_MADRA_ELEMENT));
    }

    public void setSpellDamage(float damage) {
        this.getEntityData().set(DATA_SPELL_DAMAGE, Math.max(0.0F, damage));
    }

    public void setMaxTravelDistance(double distance) {
        if (Double.isFinite(distance) && distance > 0.0) {
            this.maxTravelDistance = distance;
        }
    }

    public void setBurnOnHit(boolean burnOnHit) {
        this.burnOnHit = burnOnHit;
    }

    protected float getSpellDamage() {
        return this.getEntityData().get(DATA_SPELL_DAMAGE);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_MADRA_ELEMENT, MadraType.Element.FIRE.ordinal());
        builder.define(DATA_SPELL_DAMAGE, 6.0F);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide()) {
            // Water can slow a bolt before it reaches its travel limit.
            if (this.tickCount >= 200) {
                this.disperse();
                return;
            }
            Vec3 movement = this.getDeltaMovement();
            double remainingDistance = this.maxTravelDistance - this.distanceTravelled;
            double movementLength = movement.length();
            if (movementLength > remainingDistance && movementLength > 0.0) {
                this.setDeltaMovement(movement.scale(remainingDistance / movementLength));
            }
        }

        if (this.level().isClientSide() && !this.isInvisible()) {
            this.level().addParticle(
                    this.getTrailParticle(),
                    this.getRandomX(0.18),
                    this.getRandomY(0.18),
                    this.getRandomZ(0.18),
                    0.0,
                    0.0,
                    0.0
            );
            if (this.tickCount % 2 == 0) {
                this.level().addParticle(
                        this.getSecondaryTrailParticle(),
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        0.0,
                        0.0,
                        0.0
                );
            }
        }

        Vec3 previousPosition = this.position();
        super.tick();
        if (!this.level().isClientSide() && this.isAlive()) {
            this.distanceTravelled += previousPosition.distanceTo(this.position());
            if (this.distanceTravelled >= this.maxTravelDistance) {
                this.disperse();
            }
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        if (!super.canHitEntity(target)) return false;
        return !(this.getOwner() instanceof Mob caster) || !MobTechniqueService.isCaster(caster)
                || !(target instanceof LivingEntity living) || MobTechniqueService.canHarm(caster, living);
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        if (this.level().isClientSide()) {
            return;
        }

        Entity target = hitResult.getEntity();
        if (this.getOwner() instanceof Mob caster && MobTechniqueService.isCaster(caster)
                && target instanceof LivingEntity living && !MobTechniqueService.canHarm(caster, living)) return;
        float damage = this.getSpellDamage() + MadraAspectEffects.bonusDamage(this.getMadraElement());
        boolean landed = this.getOwner() instanceof ServerPlayer caster
                ? TechniqueCombatService.hurt(caster, this, target, damage)
                : target.hurtServer((ServerLevel) this.level(), this.damageSources().indirectMagic(this, this.getOwner()), damage);
        if (!landed) return;
        MadraAspectEffects.apply(this, target);
        if (this.burnOnHit && target instanceof LivingEntity livingTarget) {
            livingTarget.igniteForSeconds(5.0F);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        super.onHitBlock(hitResult);
        ProjectileTerrainPassives.onHit(this, hitResult);
    }

    @Override
    protected void onHit(HitResult hitResult) {
        if (this.level().isClientSide()) {
            return;
        }

        super.onHit(hitResult);
        this.disperse();
    }

    protected void disperse() {
        if (!this.level().isClientSide()) {
            this.level().broadcastEntityEvent(this, (byte) 3);
            this.discard();
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            this.emitBurst(this.getBurstParticle(), 12, 0.08);
            this.emitBurst(this.getSecondaryBurstParticle(), 8, 0.12);
        } else {
            super.handleEntityEvent(id);
        }
    }

    private ParticleOptions getTrailParticle() {
        return new DustParticleOptions(TechniqueVisualService.color(this.getMadraElement()), 0.65F);
    }

    private ParticleOptions getSecondaryTrailParticle() {
        return switch (this.getMadraElement()) {
            case FIRE, DESTRUCTION -> ParticleTypes.SMALL_FLAME;
            case WATER -> ParticleTypes.SNOWFLAKE;
            case SHADOW, DEATH, HUNGER -> ParticleTypes.SMOKE;
            default -> ParticleTypes.END_ROD;
        };
    }

    private ParticleOptions getBurstParticle() { return this.getTrailParticle(); }
    private ParticleOptions getSecondaryBurstParticle() { return this.getSecondaryTrailParticle(); }

    private void emitBurst(ParticleOptions particle, int count, double spread) {
        for (int i = 0; i < count; i++) {
            this.level().addParticle(
                    particle,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    this.random.nextGaussian() * spread,
                    this.random.nextGaussian() * spread,
                    this.random.nextGaussian() * spread
            );
        }
    }
}
