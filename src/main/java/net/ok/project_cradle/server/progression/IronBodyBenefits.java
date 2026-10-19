package net.ok.project_cradle.server.progression;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.capability.core.IronBodyType;
import net.ok.project_cradle.capability.core.advancement.AdvancementLevel;

/** Practical body specializations in addition to the common benefits of Iron advancement. */
public final class IronBodyBenefits {
    private IronBodyBenefits() {
    }

    public static void applyPermanent(ServerPlayer player, IronBodyType type, AdvancementLevel advancement) {
        boolean iron = type != IronBodyType.NONE && advancement.order() >= AdvancementLevel.IRON.order();
        boolean perfect = iron && type.isPerfect();
        int growth = Math.max(0, advancement.order() - AdvancementLevel.IRON.order());

        updateModifier(player, "iron_quality_armor", Attributes.ARMOR, perfect ? 1.0 : 0.0);
        updateModifier(player, "iron_quality_speed", Attributes.MOVEMENT_SPEED, perfect ? 0.005 : 0.0);

        boolean raindrop = iron && type == IronBodyType.RAINDROP;
        updateModifier(player, "iron_raindrop_movement", Attributes.MOVEMENT_SPEED,
                raindrop ? 0.01 + growth * 0.001 : 0.0);
        updateModifier(player, "iron_raindrop_attack_speed", Attributes.ATTACK_SPEED,
                raindrop ? 0.4 + growth * 0.05 : 0.0);

        updateModifier(player, "iron_steelforged_damage", Attributes.ATTACK_DAMAGE,
                iron && type == IronBodyType.STEELFORGED ? 1.0 + growth * 0.75 : 0.0);

        boolean puppeteer = iron && type == IronBodyType.PUPPETEER;
        updateModifier(player, "iron_puppeteer_knockback", Attributes.KNOCKBACK_RESISTANCE,
                puppeteer ? 0.25 : 0.0);
        updateModifier(player, "iron_puppeteer_step_height", Attributes.STEP_HEIGHT,
                puppeteer ? 0.25 : 0.0);
        updateModifier(player, "iron_puppeteer_safe_fall", Attributes.SAFE_FALL_DISTANCE,
                puppeteer ? 2.0 : 0.0);
    }

    public static void applyTimed(ServerPlayer player, IronBodyType type, AdvancementLevel advancement) {
        if (advancement.order() < AdvancementLevel.IRON.order() || type != IronBodyType.BLOODFORGED) {
            return;
        }

        // Periodic purging limits poison exposure without making the artist wholly immune.
        player.removeEffect(MobEffects.POISON);
        int regeneration = advancement.order() >= AdvancementLevel.UNDERLORD.order() ? 1 : 0;
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 240,
                regeneration, true, false, false));
    }

    /** Replace our own modifier when its rank-scaled value changes, and clear obsolete body bonuses. */
    private static void updateModifier(ServerPlayer player, String path, Holder<Attribute> attribute, double amount) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }

        Identifier id = Identifier.fromNamespaceAndPath(ProjectCradle.MODID, path);
        AttributeModifier current = instance.getModifier(id);
        if (current != null && current.amount() == amount
                && current.operation() == AttributeModifier.Operation.ADD_VALUE) {
            return;
        }
        if (current != null) {
            instance.removeModifier(id);
        }
        if (amount != 0.0) {
            instance.addPermanentModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
        }
    }
}
