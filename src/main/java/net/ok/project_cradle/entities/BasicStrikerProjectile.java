package net.ok.project_cradle.entities;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.ok.project_cradle.capability.madra.MadraType;
import net.ok.project_cradle.items.ModItems;

public class BasicStrikerProjectile extends ThrowableItemProjectile {
    private static final EntityDataAccessor<Integer> DATA_MADRA_ELEMENT = SynchedEntityData.defineId(
            BasicStrikerProjectile.class,
            EntityDataSerializers.INT
    );

    private static final double MAX_TRAVEL_DISTANCE = 5.0;
    private double distanceTravelled;

    public BasicStrikerProjectile(EntityType<? extends BasicStrikerProjectile> type, Level level) {
        super(type, level);
    }

    public BasicStrikerProjectile(Level level, LivingEntity owner, ItemStack itemStack) {
        super(ModEntities.BASIC_STRIKER_PROJECTILE.get(), owner, level, itemStack);
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

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_MADRA_ELEMENT, MadraType.Element.FIRE.ordinal());
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide()) {
            Vec3 movement = this.getDeltaMovement();
            double remainingDistance = MAX_TRAVEL_DISTANCE - this.distanceTravelled;
            double movementLength = movement.length();
            if (movementLength > remainingDistance && movementLength > 0.0) {
                this.setDeltaMovement(movement.scale(remainingDistance / movementLength));
            }
        }

        if (this.level().isClientSide()) {
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
            if (this.distanceTravelled >= MAX_TRAVEL_DISTANCE) {
                this.disperse();
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        Entity target = hitResult.getEntity();
        target.hurt(this.damageSources().indirectMagic(this, this.getOwner()), 6.0F);

        Vec3 direction = this.getDeltaMovement().normalize();
        target.push(direction.x * 0.3, 0.12, direction.z * 0.3);
        target.hurtMarked = true;
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        this.disperse();
    }

    private void disperse() {
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
        return switch (this.getMadraElement()) {
            case FIRE -> ParticleTypes.FLAME;
            case WATER -> ParticleTypes.BUBBLE;
            case AIR -> ParticleTypes.CLOUD;
            case EARTH -> new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState());
        };
    }

    private ParticleOptions getSecondaryTrailParticle() {
        return switch (this.getMadraElement()) {
            case FIRE -> ParticleTypes.SMOKE;
            case WATER -> ParticleTypes.SPLASH;
            case AIR -> ParticleTypes.SWEEP_ATTACK;
            case EARTH -> ParticleTypes.POOF;
        };
    }

    private ParticleOptions getBurstParticle() {
        return switch (this.getMadraElement()) {
            case FIRE -> ParticleTypes.FLAME;
            case WATER -> ParticleTypes.SPLASH;
            case AIR -> ParticleTypes.CLOUD;
            case EARTH -> new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState());
        };
    }

    private ParticleOptions getSecondaryBurstParticle() {
        return switch (this.getMadraElement()) {
            case FIRE -> ParticleTypes.LARGE_SMOKE;
            case WATER -> ParticleTypes.BUBBLE;
            case AIR -> ParticleTypes.SWEEP_ATTACK;
            case EARTH -> ParticleTypes.POOF;
        };
    }

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
