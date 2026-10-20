package net.ok.project_cradle.server.world;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.SacredBeastAppearance;
import net.ok.project_cradle.capability.core.advancement.AdvancementLevel;
import net.ok.project_cradle.entities.RemnantEntity;
import net.ok.project_cradle.server.progression.RemnantFusionService;
import net.ok.project_cradle.server.progression.SpiritBondService;

/** Sacred variants keep their vanilla bodies, movement, equipment, and ordinary AI. */
public final class SacredBeastService {
    public static final String SACRED_TAG = "project_cradle.sacred_beast";

    private SacredBeastService() { }

    public static boolean canBecomeSacred(Mob mob) {
        Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        return id != null && id.getNamespace().equals("minecraft") && !(mob instanceof RemnantEntity)
                && !mob.entityTags().contains("project_cradle.remnant")
                && !RemnantFusionService.isOwnRemnant(mob)
                && !mob.entityTags().contains(SpiritBondService.WILD_SPIRIT_TAG)
                && !mob.entityTags().contains("project_cradle.dreadbeast")
                && !mob.entityTags().contains("project_cradle.dreadgod")
                && !mob.entityTags().contains("project_cradle.spirit_destroyed")
                && !mob.getPersistentData().getBooleanOr("project_cradle.spirit_destroyed", false);
    }

    public static boolean awaken(Mob mob) {
        if (!(mob.level() instanceof ServerLevel) || !mob.isAlive() || !canBecomeSacred(mob)
                || mob.getData(ModAttachments.SACRED_BEAST_AGE).isSacred()) return false;
        var profile = SacredBeastProfiles.forMob(mob);
        var spirit = mob.getData(ModAttachments.SACRED_BEAST_AGE);
        spirit.setSacred(true);
        spirit.markSpawnChecked();
        spirit.setTechniques(profile.techniques());
        mob.getData(ModAttachments.CORE).setAdvancement(profile.rank());
        mob.getData(ModAttachments.MADRA_TYPE).setTypes(profile.aspects());
        double capacity = capacity(profile.rank());
        mob.getData(ModAttachments.MAXMADRA).setALevel(capacity);
        mob.getData(ModAttachments.MADRA).setALevel(capacity);
        applyAttributes(mob);
        mob.setHealth(mob.getMaxHealth());
        identify(mob);
        refreshAppearance(mob);
        return true;
    }

    /** Fill missing legacy fields and restore modifiers without healing a saved creature. */
    public static void restore(Mob mob) {
        var spirit = mob.getData(ModAttachments.SACRED_BEAST_AGE);
        if (!spirit.isSacred() || !canBecomeSacred(mob)) return;
        spirit.markSpawnChecked();
        var profile = SacredBeastProfiles.forMob(mob);
        if (spirit.getTechniques().isEmpty()) spirit.setTechniques(profile.techniques());
        var core = mob.getData(ModAttachments.CORE);
        if (core.getAdvancement().order() < AdvancementLevel.LOWGOLD.order()) core.setAdvancement(profile.rank());
        double maximum = mob.getData(ModAttachments.MAXMADRA).getLevel();
        if (maximum < 60.0) {
            maximum = capacity(core.getAdvancement());
            mob.getData(ModAttachments.MAXMADRA).setALevel(maximum);
            mob.getData(ModAttachments.MADRA).setALevel(maximum);
        }
        float health = mob.getHealth();
        applyAttributes(mob);
        mob.setHealth(Math.min(health, mob.getMaxHealth()));
        identify(mob);
        refreshAppearance(mob);
    }

    /** A conversion retains the same spirit rather than rolling a new rare variant. */
    public static void copySpirit(Mob from, Mob to) {
        var source = from.getData(ModAttachments.SACRED_BEAST_AGE);
        var target = to.getData(ModAttachments.SACRED_BEAST_AGE);
        target.markSpawnChecked();
        target.setSacred(source.isSacred());
        target.setTechniques(source.getTechniques());
        to.setData(ModAttachments.SACRED_BEAST_APPEARANCE, from.getData(ModAttachments.SACRED_BEAST_APPEARANCE));
        if (!source.isSacred()) return;
        to.getData(ModAttachments.CORE).setAdvancement(from.getData(ModAttachments.CORE).getAdvancement());
        to.getData(ModAttachments.CORE).setBondedPartner(from.getData(ModAttachments.CORE).getBondedPartner());
        to.getData(ModAttachments.MADRA_TYPE).setTypes(from.getData(ModAttachments.MADRA_TYPE).getTypes());
        to.getData(ModAttachments.MAXMADRA).setALevel(from.getData(ModAttachments.MAXMADRA).getLevel());
        to.getData(ModAttachments.MADRA).setALevel(from.getData(ModAttachments.MADRA).getALevel());
        to.addTag(SACRED_TAG);
        if (sacredName(from).equals(from.getCustomName())) to.setCustomName(sacredName(to));
    }

    /** A dividing body has already left its one spirit; its offspring are ordinary mobs. */
    public static void ordinarySplitChild(Mob parent, Mob child) {
        var state = child.getData(ModAttachments.SACRED_BEAST_AGE);
        state.markSpawnChecked();
        state.setSacred(false);
        state.setTechniques(List.of());
        child.setData(ModAttachments.SACRED_BEAST_APPEARANCE, SacredBeastAppearance.NONE);
        child.syncData(ModAttachments.SACRED_BEAST_APPEARANCE);
        child.removeTag(SACRED_TAG);
        if (parent.getData(ModAttachments.SACRED_BEAST_AGE).isSacred()) {
            child.getData(ModAttachments.CORE).setAdvancement(AdvancementLevel.FOUNDATION);
            child.getData(ModAttachments.CORE).setBondedPartner("");
            child.getData(ModAttachments.MADRA_TYPE).setTypes(List.of(net.ok.project_cradle.capability.madra.MadraType.Element.PURE));
            child.getData(ModAttachments.MAXMADRA).setALevel(100.0);
            child.getData(ModAttachments.MADRA).setALevel(100.0);
        }
        if (sacredName(parent).equals(child.getCustomName())) {
            child.setCustomName(null);
            child.setCustomNameVisible(false);
        }
        for (var type : List.of(Attributes.MAX_HEALTH, Attributes.ATTACK_DAMAGE, Attributes.MOVEMENT_SPEED,
                Attributes.KNOCKBACK_RESISTANCE)) {
            AttributeInstance attribute = child.getAttribute(type);
            if (attribute != null) attribute.removeModifier(modifierId(type == Attributes.MAX_HEALTH ? "health"
                    : type == Attributes.ATTACK_DAMAGE ? "damage" : type == Attributes.MOVEMENT_SPEED ? "speed" : "knockback"));
        }
        child.setHealth(Math.min(child.getHealth(), child.getMaxHealth()));
        AttributeInstance scale = child.getAttribute(Attributes.SCALE);
        if (scale != null) scale.removeModifier(modifierId("scale"));
    }

    private static double capacity(AdvancementLevel rank) {
        return 2_000.0 + Math.max(0, rank.order() - AdvancementLevel.LOWGOLD.order()) * 1_200.0;
    }

    private static void identify(Mob mob) {
        mob.addTag(SACRED_TAG);
        if (mob.getCustomName() == null || mob.getCustomName().equals(Component.translatable("entity.project_cradle.sacred_beast"))) {
            mob.setCustomName(sacredName(mob));
            mob.setCustomNameVisible(true);
        }
    }

    private static Component sacredName(Mob mob) {
        return Component.translatable("entity.project_cradle.sacred_variant", mob.getType().getDescription())
                .withStyle(ChatFormatting.GOLD);
    }

    private static Identifier modifierId(String name) {
        return Identifier.fromNamespaceAndPath(ProjectCradle.MODID, "sacred_beast_" + name);
    }

    private static void applyAttributes(Mob mob) {
        AttributeInstance health = mob.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) modifier(health, "health", Math.max(8.0, health.getBaseValue()));
        AttributeInstance attack = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) modifier(attack, "damage", Math.max(2.0, attack.getBaseValue() * 0.35));
        AttributeInstance speed = mob.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) modifier(speed, "speed", speed.getBaseValue() * 0.1);
        AttributeInstance knockback = mob.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (knockback != null) modifier(knockback, "knockback", 0.25);
        // Dragon multipart bounds and phase movement are managed separately by vanilla.
        AttributeInstance scale = mob.getAttribute(Attributes.SCALE);
        if (scale != null && !(mob instanceof EnderDragon)) {
            scale.removeModifier(modifierId("scale"));
            scale.addPermanentModifier(new AttributeModifier(modifierId("scale"), 0.15,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    private static void refreshAppearance(Mob mob) {
        SacredBeastAppearance previous = mob.getData(ModAttachments.SACRED_BEAST_APPEARANCE);
        int variant = previous.sacred() ? previous.variant() : mob.getRandom().nextInt(4);
        SacredBeastAppearance appearance = SacredBeastAppearance.of(mob.getData(ModAttachments.MADRA_TYPE).getTypes(), variant);
        if (!appearance.equals(previous)) mob.setData(ModAttachments.SACRED_BEAST_APPEARANCE, appearance);
        // Also refresh tracking clients after restoring an old sacred beast or converting its body.
        mob.syncData(ModAttachments.SACRED_BEAST_APPEARANCE);
    }

    private static void modifier(AttributeInstance attribute, String id, double amount) {
        attribute.removeModifier(modifierId(id));
        attribute.addPermanentModifier(new AttributeModifier(modifierId(id), amount, AttributeModifier.Operation.ADD_VALUE));
    }
}
