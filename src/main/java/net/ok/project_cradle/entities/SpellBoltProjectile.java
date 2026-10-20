package net.ok.project_cradle.entities;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.ok.project_cradle.server.spells.AdditionalTechniqueSpell;
import net.ok.project_cradle.server.spells.TechniqueCombatService;
import net.ok.project_cradle.server.spells.MobTechniqueService;
import net.ok.project_cradle.spells.AdditionalTechnique;
import net.ok.project_cradle.spells.TechniqueScaling;

/** A growing explosive fire orb or a fast lance that pierces up to four creatures. */
public final class SpellBoltProjectile extends BasicStrikerProjectile {
    private static final EntityDataAccessor<Integer> DATA_TECHNIQUE = SynchedEntityData.defineId(
            SpellBoltProjectile.class, EntityDataSerializers.INT);
    private final Set<UUID> pierced = new HashSet<>();

    public SpellBoltProjectile(EntityType<? extends BasicStrikerProjectile> type, Level level) {
        super(type, level);
        this.setMaxTravelDistance(30.0);
    }

    public SpellBoltProjectile(Level level, LivingEntity owner, ItemStack stack) {
        super(ModEntities.SPELL_BOLT.get(), level, owner, stack);
        this.setMaxTravelDistance(30.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TECHNIQUE, AdditionalTechnique.FIREBALL.ordinal());
    }

    public void setTechnique(AdditionalTechnique technique) {
        AdditionalTechnique mode = technique == AdditionalTechnique.ICE_LANCE
                ? AdditionalTechnique.ICE_LANCE : AdditionalTechnique.FIREBALL;
        this.getEntityData().set(DATA_TECHNIQUE, mode.ordinal());
        this.setMadraElement(mode.aspect());
        this.setMaxTravelDistance(mode == AdditionalTechnique.ICE_LANCE ? 30.0 : 24.0);
    }

    public AdditionalTechnique getTechnique() {
        return this.getEntityData().get(DATA_TECHNIQUE) == AdditionalTechnique.ICE_LANCE.ordinal()
                ? AdditionalTechnique.ICE_LANCE : AdditionalTechnique.FIREBALL;
    }

    /** The renderer grows the fireball while its area and impact power grow with it. */
    public float getVisualScale(float partialTick) {
        return this.getTechnique() == AdditionalTechnique.FIREBALL
                ? 0.7F + Math.min(1.1F, (this.tickCount + partialTick) * 0.045F) : 0.9F;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isAlive() && this.level().isClientSide()
                && this.getTechnique() == AdditionalTechnique.ICE_LANCE) {
            this.level().addParticle(ParticleTypes.SNOWFLAKE, this.getRandomX(0.16),
                    this.getRandomY(0.16), this.getRandomZ(0.16), 0.0, 0.0, 0.0);
        }
        if (!this.level().isClientSide() && this.tickCount > 200) this.disperse();
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        if (this.pierced.contains(target.getUUID()) || !super.canHitEntity(target)) return false;
        if (target instanceof MadraShieldEntity shield) return shield.isPickable();
        if (!(target instanceof LivingEntity living)) return false;
        if (this.getOwner() instanceof ServerPlayer caster) return AdditionalTechniqueSpell.isHostileTarget(caster, living);
        return this.getOwner() instanceof Mob caster && MobTechniqueService.isCaster(caster) && MobTechniqueService.canHarm(caster, living);
    }

    @Override
    protected void onHit(HitResult hit) {
        if (this.level().isClientSide()) return;
        if (this.getTechnique() == AdditionalTechnique.FIREBALL) {
            if (hit instanceof BlockHitResult block) super.onHitBlock(block);
            this.explode();
        } else if (hit instanceof EntityHitResult entity) {
            this.onHitEntity(entity);
        } else {
            super.onHit(hit);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        if (!(hit.getEntity() instanceof LivingEntity target)
                || !(this.getOwner() instanceof ServerPlayer
                        || this.getOwner() instanceof Mob mob && MobTechniqueService.isCaster(mob))
                || !this.pierced.add(target.getUUID())) return;
        boolean hurt = this.getOwner() instanceof ServerPlayer caster
                ? TechniqueCombatService.hurt(caster, this, target, this.getSpellDamage())
                : MobTechniqueService.hurt((Mob) this.getOwner(), this, target, this.getSpellDamage());
        if (hurt) {
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 2, true, true, true));
            ((ServerLevel) this.level()).sendParticles(ParticleTypes.SNOWFLAKE,
                    target.getX(), target.getY() + 0.8, target.getZ(), 12, 0.3, 0.6, 0.3, 0.03);
        }
        if (this.pierced.size() >= 4) this.disperse();
    }

    private void explode() {
        if (!(this.level() instanceof ServerLevel level)) return;
        double growth = Math.min(1.0, this.tickCount / 24.0);
        double baseRadius = 2.0 + growth;
        double radius = this.getOwner() instanceof ServerPlayer caster
                ? TechniqueScaling.range(caster, baseRadius)
                : this.getOwner() instanceof Mob mob && MobTechniqueService.isCaster(mob) ? MobTechniqueService.range(mob, baseRadius) : baseRadius;
        if (this.getOwner() instanceof ServerPlayer caster) {
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
                    this.getBoundingBox().inflate(radius), entity -> AdditionalTechniqueSpell.isHostileTarget(caster, entity)
                            && entity.distanceToSqr(this) <= radius * radius && this.hasLineOfSightTo(entity))) {
                float falloff = (float) Math.max(0.3, 1.0 - target.distanceTo(this) / (radius + 0.5));
                if (TechniqueCombatService.hurt(caster, this, target, (this.getSpellDamage() + (float) growth * 4.0F) * falloff)) {
                    target.igniteForSeconds(5.0F);
                }
            }
        }
        if (this.getOwner() instanceof Mob caster && MobTechniqueService.isCaster(caster)) {
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
                    this.getBoundingBox().inflate(radius), entity -> MobTechniqueService.canHarm(caster, entity)
                            && entity.distanceToSqr(this) <= radius * radius && this.hasLineOfSightTo(entity))) {
                float falloff = (float) Math.max(0.3, 1.0 - target.distanceTo(this) / (radius + 0.5));
                float growingDamage = this.getSpellDamage() + (float) (growth * 4.0 * MobTechniqueService.power(caster));
                if (MobTechniqueService.hurt(caster, this, target, growingDamage * falloff)) target.igniteForSeconds(5.0F);
            }
        }
        level.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(), 40, radius * 0.4, 0.5, radius * 0.4, 0.1);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY(), this.getZ(), 12, 0.5, 0.5, 0.5, 0.04);
        level.playSound(null, this.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.9F, 0.7F);
        this.disperse();
    }

    private boolean hasLineOfSightTo(LivingEntity target) {
        return this.level().clip(new net.minecraft.world.level.ClipContext(this.position(), target.getEyePosition(),
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("spellTechnique", this.getTechnique().ordinal());
        output.putFloat("spellDamage", this.getSpellDamage());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        int saved = input.getIntOr("spellTechnique", AdditionalTechnique.FIREBALL.ordinal());
        this.setTechnique(saved == AdditionalTechnique.ICE_LANCE.ordinal()
                ? AdditionalTechnique.ICE_LANCE : AdditionalTechnique.FIREBALL);
        this.setSpellDamage(input.getFloatOr("spellDamage", 7.0F));
    }
}
