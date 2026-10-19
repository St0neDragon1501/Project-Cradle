package net.ok.project_cradle.entities;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.spells.TechniqueScaling;

/** Stationary, solid arcane wall. A thin cardinal hitbox matches its visible plane. */
@EventBusSubscriber(modid = ProjectCradle.MODID)
public final class MadraShieldEntity extends Entity {
    public static final float WIDTH = 3.0F;
    public static final float HEIGHT = 3.0F;
    public static final float BASE_MAX_HEALTH = 30.0F;
    public static final int LIFETIME_TICKS = 200;
    private static final double HALF_THICKNESS = 0.14;
    private static final EntityDataAccessor<Float> HEALTH = SynchedEntityData.defineId(MadraShieldEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> MAX_HEALTH = SynchedEntityData.defineId(MadraShieldEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> REMAINING = SynchedEntityData.defineId(MadraShieldEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HIT_FLASH = SynchedEntityData.defineId(MadraShieldEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> OWNER = SynchedEntityData.defineId(MadraShieldEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> NORMAL_X = SynchedEntityData.defineId(MadraShieldEntity.class, EntityDataSerializers.BOOLEAN);

    public MadraShieldEntity(EntityType<? extends MadraShieldEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public MadraShieldEntity(ServerLevel level, ServerPlayer caster) {
        this(ModEntities.MADRA_SHIELD.get(), level);
        float maximum = (float) (BASE_MAX_HEALTH * TechniqueScaling.power(caster));
        this.entityData.set(MAX_HEALTH, maximum);
        this.entityData.set(HEALTH, maximum);
        this.entityData.set(REMAINING, TechniqueScaling.duration(caster, LIFETIME_TICKS));
        this.entityData.set(OWNER, caster.getUUID().toString());
        Direction facing = Direction.fromYRot(caster.getYRot());
        this.entityData.set(NORMAL_X, facing.getAxis() == Direction.Axis.X);
        this.setYRot(facing.toYRot());
        this.yRotO = this.getYRot();
    }

    /** Placement and replacement only; the spell casting service owns payment and cooldown. */
    public static boolean spawn(ServerPlayer caster) {
        if (caster == null || !caster.isAlive() || caster.isSpectator()) return false;
        ServerLevel level = caster.level();
        Direction facing = Direction.fromYRot(caster.getYRot());
        Vec3 direction = new Vec3(facing.getStepX(), 0.0, facing.getStepZ());
        Vec3 start = caster.getEyePosition();
        Vec3 end = start.add(direction.scale(2.5));
        HitResult hit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        double distance = hit.getType() == HitResult.Type.MISS ? 2.5 : start.distanceTo(hit.getLocation()) - 0.24;
        if (distance < 1.0) return false;
        MadraShieldEntity shield = new MadraShieldEntity(level, caster);
        Vec3 location = caster.position().add(direction.scale(distance)).add(0.0, 0.02, 0.0);
        shield.setPos(location);
        if (!shield.hasClearPlacement(caster) || !level.addFreshEntity(shield)) return false;
        // Unsaved shields cannot survive unloaded chunks. Search loaded levels to replace an existing one.
        java.util.List<MadraShieldEntity> previousShields = new java.util.ArrayList<>();
        for (ServerLevel loaded : level.getServer().getAllLevels()) {
            for (Entity entity : loaded.getAllEntities()) {
                if (entity instanceof MadraShieldEntity previous && previous != shield && previous.isOwnedBy(caster)) {
                    previousShields.add(previous);
                }
            }
        }
        previousShields.forEach(Entity::discard);
        level.playSound(null, shield.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.8F, 1.35F);
        return true;
    }

    private boolean hasClearPlacement(ServerPlayer caster) {
        AABB bounds = this.getBoundingBox().deflate(0.01);
        for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(bounds.minX, bounds.minY, bounds.minZ),
                BlockPos.containing(bounds.maxX, bounds.maxY, bounds.maxZ))) {
            if (this.level().isOutsideBuildHeight(pos) || !this.level().hasChunkAt(pos)
                    || !this.level().getWorldBorder().isWithinBounds(pos)) return false;
        }
        if (this.level().getBlockCollisions(this, bounds).iterator().hasNext()) return false;
        return this.level().getEntities(this, bounds, entity -> !entity.isSpectator()
                && (entity instanceof LivingEntity
                    || entity instanceof MadraShieldEntity other && !other.isOwnedBy(caster))).isEmpty();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(HEALTH, BASE_MAX_HEALTH);
        builder.define(MAX_HEALTH, BASE_MAX_HEALTH);
        builder.define(REMAINING, LIFETIME_TICKS);
        builder.define(HIT_FLASH, 0);
        builder.define(OWNER, "");
        builder.define(NORMAL_X, false);
    }

    @Override
    protected AABB makeBoundingBox(Vec3 position) {
        boolean x = this.entityData.get(NORMAL_X);
        double halfX = x ? HALF_THICKNESS : WIDTH * 0.5;
        double halfZ = x ? WIDTH * 0.5 : HALF_THICKNESS;
        return new AABB(position.x - halfX, position.y, position.z - halfZ,
                position.x + halfX, position.y + HEIGHT, position.z + halfZ);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (NORMAL_X.equals(accessor)) this.setBoundingBox(this.makeBoundingBox());
    }

    public float getHealth() { return this.entityData.get(HEALTH); }
    public float getMaxHealth() { return this.entityData.get(MAX_HEALTH); }
    public int getRemainingTicks() { return this.entityData.get(REMAINING); }
    public int getHitFlashTicks() { return this.entityData.get(HIT_FLASH); }
    public boolean isNormalX() { return this.entityData.get(NORMAL_X); }
    public String getOwnerId() { return this.entityData.get(OWNER); }
    public boolean isOwnedBy(Player player) { return player != null && player.getUUID().toString().equals(this.getOwnerId()); }

    public ServerPlayer getOwner() {
        if (!(this.level() instanceof ServerLevel level)) return null;
        try {
            return level.getServer().getPlayerList().getPlayer(UUID.fromString(this.getOwnerId()));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    @Override
    public void tick() {
        // Do not run gravity, fluid movement or portals on a fixed spell wall.
        this.setDeltaMovement(Vec3.ZERO);
        if (!(this.level() instanceof ServerLevel)) return;
        ServerPlayer owner = this.getOwner();
        if (owner == null || !owner.isAlive() || owner.level() != this.level() || owner.isSpectator()) {
            this.discard();
            return;
        }
        int life = this.getRemainingTicks() - 1;
        this.entityData.set(REMAINING, life);
        if (this.getHitFlashTicks() > 0) this.entityData.set(HIT_FLASH, this.getHitFlashTicks() - 1);
        if (life <= 0) this.discard();
    }

    @Override
    public boolean isPickable() { return !this.isRemoved() && this.getHealth() > 0.0F; }

    @Override
    public boolean canBeCollidedWith(Entity other) { return this.isPickable(); }

    @Override
    public boolean isPushable() { return false; }

    @Override
    public void push(double x, double y, double z) { }

    @Override
    public void push(Entity entity) { }

    @Override
    public void move(MoverType mover, Vec3 movement) { }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (this.isRemoved()) return false;
        // Piercing arrows may reach this as a later hit in a single swept hit list,
        // for which NeoForge does not fire another ProjectileImpactEvent.
        if (source.getDirectEntity() instanceof Projectile projectile) projectile.discard();
        if (this.isInvulnerableToBase(source) || !Float.isFinite(amount) || amount <= 0.0F) return false;
        ServerPlayer owner = this.getOwner();
        Entity attacker = source.getEntity();
        if (attacker != null && attacker.getUUID().toString().equals(this.getOwnerId())) return false;
        if (owner != null && attacker instanceof Player player && !player.canHarmPlayer(owner)) return false;
        this.entityData.set(HEALTH, Mth.clamp(this.getHealth() - amount, 0.0F, this.getMaxHealth()));
        this.entityData.set(HIT_FLASH, 6);
        Vec3 center = this.getBoundingBox().getCenter();
        level.sendParticles(ParticleTypes.END_ROD, center.x, center.y, center.z, 6, 0.45, 0.5, 0.05, 0.04);
        level.playSound(null, this.blockPosition(), SoundEvents.SHIELD_BLOCK.value(), SoundSource.PLAYERS, 0.75F, 1.25F);
        if (this.getHealth() <= 0.0F) {
            level.sendParticles(ParticleTypes.END_ROD, center.x, center.y, center.z, 36, 1.0, 1.0, 0.15, 0.15);
            level.playSound(null, this.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.9F, 1.5F);
            this.discard();
        }
        return true;
    }

    /** Vanilla projectile sweeps test the whole travel segment, so fast and piercing shots hit this thin wall. */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        Projectile projectile = event.getProjectile();
        if (!(projectile.level() instanceof ServerLevel level)
                || !(event.getRayTraceResult() instanceof EntityHitResult hit)
                || !(hit.getEntity() instanceof MadraShieldEntity shield) || !shield.isPickable()) return;
        float impact = (float) Mth.clamp(2.0 + projectile.getDeltaMovement().length() * 2.0, 3.0, 12.0);
        shield.hurtServer(level, projectile.damageSources().indirectMagic(projectile, projectile.getOwner()), impact);
        // Even friendly shots are stopped, but their caster cannot damage an allied shield.
        projectile.discard();
        event.setCanceled(true);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) { }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) { }
}
