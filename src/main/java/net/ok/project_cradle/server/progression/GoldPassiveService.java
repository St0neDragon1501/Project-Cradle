package net.ok.project_cradle.server.progression;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.capability.madra.MadraPassives;
import net.ok.project_cradle.capability.madra.MadraType.Element;

/** Server-owned, reversible aspect benefits gained at Lowgold. */
public final class GoldPassiveService {
    private static final String SHADOW_ACTIVE = ProjectCradle.MODID + ".shadow_passive_active";

    private GoldPassiveService() {
    }

    public static void describe(ServerPlayer player) {
        boolean any = false;
        for (Element element : Element.values()) {
            if (element != Element.PURE && MadraPassives.has(player, element)) {
                if (!any) player.sendSystemMessage(Component.translatable("message.project_cradle.passives_header"));
                player.sendSystemMessage(Component.translatable("passive.project_cradle." + element.id()));
                any = true;
            }
        }
        if (!any) player.sendSystemMessage(Component.translatable("message.project_cradle.no_gold_passives"));
    }

    public static void refresh(ServerPlayer player) {
        updateModifier(player, "gold_passive_blood_health", Attributes.MAX_HEALTH,
                MadraPassives.has(player, Element.BLOOD) ? 4.0 : 0.0,
                AttributeModifier.Operation.ADD_VALUE);
        updateModifier(player, "gold_passive_earth_mining", Attributes.BLOCK_BREAK_SPEED,
                MadraPassives.has(player, Element.EARTH) ? 0.2 : 0.0,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        // Vanilla Strength I adds 3 attack damage. A separate modifier keeps Sword's
        // additional Strength-I-equivalent bonus alongside Iron's existing Strength effect.
        updateModifier(player, "gold_passive_sword_damage", Attributes.ATTACK_DAMAGE,
                MadraPassives.has(player, Element.SWORD) ? 3.0 : 0.0,
                AttributeModifier.Operation.ADD_VALUE);

        // Removing Blood lowers only the maximum; gaining it never heals the player.
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
        refreshLife(player);
    }

    /** Called after vanilla's effect updates so those updates cannot erase concealment. */
    public static void refreshShadow(ServerPlayer player) {
        boolean concealed = MadraPassives.has(player, Element.SHADOW)
                && player.level().getMaxLocalRawBrightness(player.blockPosition()) <= 7;
        var data = player.getPersistentData();
        if (concealed) {
            data.putBoolean(SHADOW_ACTIVE, true);
            player.setInvisible(true);
        } else if (data.getBooleanOr(SHADOW_ACTIVE, false)) {
            data.remove(SHADOW_ACTIVE);
            // Never remove a potion effect. Only release the raw invisibility flag
            // that this passive owned, restoring vanilla's current effect state.
            player.setInvisible(player.hasEffect(MobEffects.INVISIBILITY));
        }
    }

    private static void refreshLife(ServerPlayer player) {
        if (!MadraPassives.has(player, Element.LIFE)) {
            return;
        }
        MobEffectInstance current = player.getEffect(MobEffects.REGENERATION);
        if (current != null && (current.getAmplifier() > 0 || current.isInfiniteDuration()
                || current.getDuration() > 10)) {
            return;
        }
        // Renew only near expiry: resetting every tick would accelerate vanilla regeneration.
        // Short duration makes the passive expire naturally after an aspect change,
        // without removing regeneration from potions, Iron bodies, or other sources.
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60,
                0, true, false, false));
    }

    /** Touch only our own ID; identical refreshes never accumulate attribute bonuses. */
    private static void updateModifier(ServerPlayer player, String path, Holder<Attribute> attribute,
            double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        Identifier id = Identifier.fromNamespaceAndPath(ProjectCradle.MODID, path);
        AttributeModifier current = instance.getModifier(id);
        if (current != null && current.amount() == amount && current.operation() == operation) {
            return;
        }
        if (current != null) {
            instance.removeModifier(id);
        }
        if (amount != 0.0) {
            AttributeModifier modifier = new AttributeModifier(id, amount, operation);
            // Save the health maximum so loading health cannot clamp away Blood's two hearts
            // before player-login events restore dynamic passives.
            if (attribute.equals(Attributes.MAX_HEALTH)) instance.addPermanentModifier(modifier);
            else instance.addTransientModifier(modifier);
        }
    }
}
