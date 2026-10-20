package net.ok.project_cradle.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashSet;
import com.mojang.serialization.Codec;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.core.advancement.AdvancementLevel;
import net.ok.project_cradle.capability.madra.MadraType.Element;
import net.ok.project_cradle.server.progression.RemnantFusionService;
import net.ok.project_cradle.server.spells.TechniqueVisualService;
import net.ok.project_cradle.spells.TechniqueScaling;
import net.ok.project_cradle.spells.TechniqueCatalog;
import net.ok.project_cradle.server.progression.TechniqueKnowledge;
import net.ok.project_cradle.server.spells.RemnantTechniqueService;

/** A body of condensed madra, with Vex flight and charge movement beneath its own spirit appearance. */
public final class RemnantEntity extends Vex {
    private static final EntityDataAccessor<Integer> PRIMARY = SynchedEntityData.defineId(RemnantEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SECONDARY = SynchedEntityData.defineId(RemnantEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SOURCE_RANK = SynchedEntityData.defineId(RemnantEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FORM = SynchedEntityData.defineId(RemnantEntity.class, EntityDataSerializers.INT);

    private List<String> knownTechniques = List.of();

    public RemnantEntity(EntityType<? extends RemnantEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.setCanPickUpLoot(false);
        if (!level.isClientSide()) this.setSpirit(AdvancementLevel.IRON, List.of(Element.PURE), RemnantForm.HUMANOID);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Vex.createAttributes().add(Attributes.MAX_HEALTH, 12.0).add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PRIMARY, Element.PURE.ordinal());
        builder.define(SECONDARY, -1);
        builder.define(SOURCE_RANK, AdvancementLevel.IRON.ordinal());
        builder.define(FORM, RemnantForm.HUMANOID.ordinal());
    }

    public Element getMadraElement() { return Element.byOrdinal(this.entityData.get(PRIMARY)); }

    public boolean hasSecondaryAspect() {
        int ordinal = this.entityData.get(SECONDARY);
        return ordinal >= 0 && ordinal < Element.values().length && ordinal != this.entityData.get(PRIMARY);
    }

    public Element getSecondaryMadraElement() {
        return this.hasSecondaryAspect() ? Element.byOrdinal(this.entityData.get(SECONDARY)) : this.getMadraElement();
    }

    public AdvancementLevel getSourceRank() {
        int ordinal = this.entityData.get(SOURCE_RANK);
        AdvancementLevel[] ranks = AdvancementLevel.values();
        return ordinal >= AdvancementLevel.IRON.ordinal() && ordinal < ranks.length ? ranks[ordinal] : AdvancementLevel.IRON;
    }

    public RemnantForm getRemnantForm() { return RemnantForm.byOrdinal(this.entityData.get(FORM)); }

    /** Used by death formation and manifestation; the living body's equipment and identity are not copied. */
    public void inheritSpirit(LivingEntity source) {
        if (this.level().isClientSide()) return;
        this.setSpirit(source.getData(ModAttachments.CORE).getAdvancement(),
                source.getData(ModAttachments.MADRA_TYPE).getTypes(),
                source instanceof Animal ? RemnantForm.BEAST : RemnantForm.HUMANOID);
        this.setKnownTechniques(TechniqueKnowledge.capture(source));
        var core = this.getData(ModAttachments.CORE);
        core.trySpendSoulfire(core.getSoulfire());
        core.addSoulfire(source.getData(ModAttachments.CORE).getSoulfire());
        double reservoir = Mth.clamp(source.getData(ModAttachments.MAXMADRA).getLevel() * 0.65, 60.0, 1_000_000.0);
        this.getData(ModAttachments.MAXMADRA).setALevel(reservoir);
        this.getData(ModAttachments.MADRA).setALevel(reservoir);
        // Spirit strength is rank-based, but a death Remnant retains only a fraction of its body's attributes.
        AttributeInstance health = this.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.setBaseValue(Math.min(health.getBaseValue(), Math.max(8.0, source.getMaxHealth() * 0.65)));
            this.setHealth(this.getMaxHealth());
        }
        AttributeInstance sourceDamage = source.getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeInstance damage = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null && sourceDamage != null) {
            double livingPower = sourceDamage.getValue();
            if (source instanceof Player player) livingPower = Math.max(livingPower, TechniqueScaling.damage(player, 6.0F));
            damage.setBaseValue(Math.min(damage.getBaseValue(), Math.max(1.5, livingPower * 0.65)));
        }
    }

    public void setSpirit(AdvancementLevel rank, List<Element> aspects, RemnantForm form) {
        if (this.level().isClientSide()) return;
        this.setSpiritData(rank, aspects, form);
        this.setBaseStats(baseHealth(this.getSourceRank()), baseDamage(this.getSourceRank()));
        this.setHealth(this.getMaxHealth());
        this.setKnownTechniques(List.of(TechniqueCatalog.basic(), TechniqueCatalog.themed(this.getMadraElement())));
        double reservoir = 100.0 + this.getSourceRank().order() * 40.0;
        this.getData(ModAttachments.MAXMADRA).setALevel(reservoir);
        this.getData(ModAttachments.MADRA).setALevel(reservoir);
    }

    public List<String> getKnownTechniques() { return this.knownTechniques; }

    public void setKnownTechniques(List<String> ids) {
        var valid = new LinkedHashSet<String>();
        valid.add(TechniqueCatalog.basic());
        if (ids != null) for (String id : ids) {
            String normalized = TechniqueCatalog.normalize(id);
            if (!normalized.isEmpty() && valid.size() < 64) valid.add(normalized);
        }
        if (valid.size() == 1) valid.add(TechniqueCatalog.themed(this.getMadraElement()));
        this.knownTechniques = List.copyOf(valid);
    }

    private void setSpiritData(AdvancementLevel rank, List<Element> aspects, RemnantForm form) {
        AdvancementLevel sourceRank = rank == null || rank.order() < AdvancementLevel.IRON.order() ? AdvancementLevel.IRON : rank;
        List<Element> validAspects = new ArrayList<>(2);
        if (aspects != null) for (Element aspect : aspects) {
            if (aspect != null && !validAspects.contains(aspect) && validAspects.size() < 2) validAspects.add(aspect);
        }
        if (validAspects.isEmpty()) validAspects.add(Element.PURE);
        this.entityData.set(PRIMARY, validAspects.get(0).ordinal());
        this.entityData.set(SECONDARY, validAspects.size() > 1 ? validAspects.get(1).ordinal() : -1);
        this.entityData.set(SOURCE_RANK, sourceRank.ordinal());
        this.entityData.set(FORM, (form == null ? RemnantForm.HUMANOID : form).ordinal());
        this.getData(ModAttachments.CORE).setAdvancement(sourceRank);
        this.getData(ModAttachments.MADRA_TYPE).setTypes(validAspects);
        this.xpReward = Math.min(12, sourceRank.order());
    }

    private static double baseHealth(AdvancementLevel rank) { return 12.0 + Math.max(0, rank.order() - 3) * 4.0; }
    private static double baseDamage(AdvancementLevel rank) { return 3.0 + Math.max(0, rank.order() - 3) * 0.65; }

    private void setBaseStats(double health, double damage) {
        AttributeInstance maximum = this.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance attack = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (maximum != null) maximum.setBaseValue(Mth.clamp(Double.isFinite(health) ? health : 12.0,
                8.0, RemnantFusionService.isOwnRemnant(this) ? 1024.0 : 120.0));
        if (attack != null) attack.setBaseValue(Mth.clamp(Double.isFinite(damage) ? damage : 3.0, 1.5, 24.0));
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        // Its claws and bindings are madra. Vex's ordinary iron sword must never become Remnant equipment.
        for (EquipmentSlot slot : EquipmentSlot.VALUES) {
            this.setItemSlot(slot, ItemStack.EMPTY);
            this.setDropChance(slot, 0.0F);
        }
    }

    @Override
    public boolean canBreatheUnderwater() { return true; }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        return source.is(DamageTypes.DROWN) || source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.FALL)
                || super.isInvulnerableTo(level, source);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.isAlive()) RemnantTechniqueService.tick(this);
        if (!this.level().isClientSide() || !this.isAlive() || this.isInvisible()) return;
        if (this.tickCount % 5 == 0) {
            Element aspect = this.hasSecondaryAspect() && this.tickCount % 10 == 0 ? this.getSecondaryMadraElement() : this.getMadraElement();
            this.level().addParticle(new DustParticleOptions(TechniqueVisualService.color(aspect), 0.65F),
                    this.getRandomX(0.35), this.getRandomY(), this.getRandomZ(0.35), 0.0, 0.015, 0.0);
        }
        if (this.tickCount % 20 == 0) {
            this.level().addParticle(ParticleTypes.ENCHANT, this.getX(), this.getY() + 0.3, this.getZ(), 0.0, 0.0, 0.0);
        }
    }

    @Override
    public void spawnAnim() {
        if (!this.level().isClientSide()) {
            this.level().broadcastEntityEvent(this, (byte) 20);
            return;
        }
        for (int i = 0; i < 16; i++) {
            double angle = i * Math.PI / 8;
            this.level().addParticle(new DustParticleOptions(TechniqueVisualService.color(this.getMadraElement()), 0.8F),
                    this.getX() + Math.cos(angle) * 0.6, this.getY() + 0.6, this.getZ() + Math.sin(angle) * 0.6,
                    Math.cos(angle) * 0.02, 0.02, Math.sin(angle) * 0.02);
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        if (target instanceof LivingEntity living && !RemnantTechniqueService.canHarm(this, living)) return false;
        if (!super.doHurtTarget(level, target)) return false;
        if (target instanceof LivingEntity living) {
            this.applyAspect(living, this.getMadraElement());
            if (this.hasSecondaryAspect()) this.applyAspect(living, this.getSecondaryMadraElement());
        }
        return true;
    }

    private void applyAspect(LivingEntity target, Element aspect) {
        switch (aspect) {
            case FIRE -> target.igniteForSeconds(2.0F);
            case WATER, EARTH -> effect(target, MobEffects.SLOWNESS, 50);
            case PURE, DESTRUCTION -> effect(target, MobEffects.WEAKNESS, 40);
            case LIFE, HUNGER -> this.heal(0.5F);
            case DEATH -> effect(target, MobEffects.WITHER, 40);
            case LIGHT -> effect(target, MobEffects.GLOWING, 80);
            case SHADOW -> effect(target, MobEffects.DARKNESS, 40);
            case BLOOD -> effect(target, MobEffects.POISON, 40);
            case DREAM -> effect(target, MobEffects.NAUSEA, 60);
            case AIR, FORCE -> {
                double strength = aspect == Element.FORCE ? 0.55 : 0.3;
                var away = target.position().subtract(this.position()).normalize();
                target.push(away.x * strength, 0.12, away.z * strength);
                target.hurtMarked = true;
            }
            case SWORD -> { }
        }
    }

    private static void effect(LivingEntity target, net.minecraft.core.Holder<MobEffect> effect, int ticks) {
        target.addEffect(new MobEffectInstance(effect, ticks, 0, true, false, false));
    }

    @Override
    protected int getBaseExperienceReward(ServerLevel level) {
        return RemnantFusionService.isOwnRemnant(this) ? 0 : super.getBaseExperienceReward(level);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("known_techniques", Codec.STRING.listOf(), this.knownTechniques);
        output.putString("spirit_rank", this.getSourceRank().id());
        output.putString("madra_primary", this.getMadraElement().id());
        if (this.hasSecondaryAspect()) output.putString("madra_secondary", this.getSecondaryMadraElement().id());
        output.putString("spirit_form", this.getRemnantForm().id());
        output.putDouble("spirit_health", this.getAttribute(Attributes.MAX_HEALTH).getBaseValue());
        output.putDouble("spirit_damage", this.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        float savedHealth = this.getHealth();
        Element primary = Element.byId(input.getStringOr("madra_primary", "pure"));
        Element secondary = Element.byId(input.getStringOr("madra_secondary", ""));
        List<Element> aspects = secondary == null || secondary == primary
                ? List.of(primary == null ? Element.PURE : primary)
                : List.of(primary == null ? Element.PURE : primary, secondary);
        this.setSpiritData(AdvancementLevel.fromId(input.getStringOr("spirit_rank", "iron")), aspects,
                RemnantForm.byId(input.getStringOr("spirit_form", "humanoid")));
        this.setBaseStats(input.getDoubleOr("spirit_health", baseHealth(this.getSourceRank())),
                input.getDoubleOr("spirit_damage", baseDamage(this.getSourceRank())));
        this.setKnownTechniques(input.read("known_techniques", Codec.STRING.listOf()).orElseGet(() ->
                List.of(TechniqueCatalog.basic(), TechniqueCatalog.themed(this.getMadraElement()))));
        this.setHealth(Math.min(savedHealth, this.getMaxHealth()));
    }
}
