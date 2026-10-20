package net.ok.project_cradle.server.progression;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.core.Core;
import net.ok.project_cradle.capability.core.IronBodyType;
import net.ok.project_cradle.capability.core.GoldRoute;
import net.ok.project_cradle.capability.core.LordRevelation;
import net.ok.project_cradle.capability.core.ProgressionRules;
import net.ok.project_cradle.capability.core.ProgressionHints;
import net.ok.project_cradle.capability.core.advancement.AdvancementLevel;
import net.ok.project_cradle.capability.madra.MadraType;
import net.ok.project_cradle.capability.madra.Madra;
import net.ok.project_cradle.capability.madra.MaxMadra;
import net.ok.project_cradle.items.ModItems;
import net.ok.project_cradle.server.world.AuraService;

/** Server-side rules for unlocking and advancing a sacred artist's core. */
public final class ProgressionService {
    private ProgressionService() {
    }

    public static boolean unlockFoundation(ServerPlayer player) {
        Core core = player.getData(ModAttachments.CORE);
        if (core.hasReceivedTutorial()) return false;
        if (core.getAdvancement() == AdvancementLevel.UNFOUNDED) core.setAdvancement(AdvancementLevel.FOUNDATION);
        core.setTutorialReceived(true);
        award(player, "unlock_madra", "unlocked");
        if (player.getStats().getValue(Stats.ITEM_CRAFTED.get(ModItems.TATTERED_MANUAL.get())) == 0) {
            giveOrDrop(player, new ItemStack(ModItems.TATTERED_MANUAL.get()));
        }
        player.sendSystemMessage(Component.translatable("message.project_cradle.madra_unlocked"));
        sync(player);
        return true;
    }

    /** Advances only stages that the specification ties to core capacity. */
    public static boolean evaluateCapacity(ServerPlayer player) {
        Core core = player.getData(ModAttachments.CORE);
        AdvancementLevel current = core.getAdvancement();
        AdvancementLevel next = current.capacityAdvancement();
        if (next == null || player.getData(ModAttachments.MAXMADRA).getLevel() < current.requiredMaxMadra()) return false;
        if (next == AdvancementLevel.JADE && !core.hasJadeSpiral()) return false;
        if (next == AdvancementLevel.LOWGOLD) {
            if (core.getGoldCycling() < ProgressionRules.NATURAL_GOLD_CYCLES) return false;
            core.setGoldRoute(GoldRoute.NATURAL);
            core.setGoldsign("");
        }
        return advance(player, next);
    }

    /** Activates the user's cycling technique and gathers aura when their stage permits it. */
    public static void cycle(ServerPlayer player) {
        if (!player.isAlive() || player.isSpectator()) return;
        long now = player.level().getGameTime();
        long previous = player.getPersistentData().getLongOr("project_cradle.last_cycle", -10L);
        if (now >= previous && now - previous < 10L) return;
        player.getPersistentData().putLong("project_cradle.last_cycle", now);
        Core core = player.getData(ModAttachments.CORE);
        if (core.getAdvancement() == AdvancementLevel.UNFOUNDED) core.setAdvancement(AdvancementLevel.FOUNDATION);
        if ((core.hasSage() || core.isMonarch()) && !player.isShiftKeyDown() && issueSageCommand(player)) return;
        core.trainCycling();
        MaxMadra maximum = player.getData(ModAttachments.MAXMADRA);
        Madra madra = player.getData(ModAttachments.MADRA);
        MadraType type = player.getData(ModAttachments.MADRA_TYPE);
        boolean pure = type.getTypes().size() == 1 && type.getTypes().get(0) == MadraType.Element.PURE;
        AuraService.Reading reading = AuraService.detect(player);
        if (core.getAdvancement().order() < AdvancementLevel.COPPER.order()) {
            // Foundation training cycles the artist's own pure madra, without sensing environmental aura.
            double focus = reading.meditationMultiplier();
            maximum.addMaxMadra(2.0 * focus);
            madra.addMadra(20.0 * focus, maximum.getLevel());
        } else {
            double jadeBonus = core.hasJadeSpiral() ? 2.0 : 1.0;
            double location = pure ? reading.meditationMultiplier() : reading.locationMultiplier(type);
            if (core.getAdvancement().order() <= AdvancementLevel.ARCHLORD.order()) maximum.addMaxMadra(jadeBonus * location);
            // Internal recovery remains possible when nearby objects have been depleted.
            madra.addMadra(pure ? 20.0 * jadeBonus * location : 3.0 + reading.ambientYield(type), maximum.getLevel());
            if (!pure) AuraService.harvestBest(player, reading);
            if (player.isShiftKeyDown() && core.getAdvancement().order() >= AdvancementLevel.TRUEGOLD.order()) {
                if (!core.hasSoulspace()) openSoulspace(player);
                if (core.hasSoulspace() && reading.isBalanced()) {
                    core.addSoulfire(0.1);
                    player.sendOverlayMessage(Component.translatable("message.project_cradle.soulfire_woven"));
                }
            }
        }
        evaluateCapacity(player);
        player.syncData(ModAttachments.MADRA);
        player.syncData(ModAttachments.MAXMADRA);
        sync(player);
    }

    private static boolean issueSageCommand(ServerPlayer player) {
        return IconAuthorityService.issue(player);
    }

    public static boolean completeIronBody(ServerPlayer player, IronBodyType type) {
        Core core = player.getData(ModAttachments.CORE);
        if (core.getAdvancement() != AdvancementLevel.COPPER || type == IronBodyType.NONE) return false;
        if (player.getData(ModAttachments.MAXMADRA).getLevel() < ProgressionRules.IRON_MADRA) {
            player.sendSystemMessage(Component.translatable("message.project_cradle.iron_needs_capacity", ProgressionRules.IRON_MADRA));
            return false;
        }
        if (type.isPerfect() && core.getIronPreparation() < ProgressionRules.IRON_PREPARATION_CYCLES) {
            player.sendSystemMessage(Component.translatable("message.project_cradle.iron_needs_preparation", core.getIronPreparation(), ProgressionRules.IRON_PREPARATION_CYCLES));
            return false;
        }
        core.setIronBodyType(type);
        if (type == IronBodyType.BASIC) player.sendSystemMessage(Component.translatable("message.project_cradle.basic_iron_limit"));
        return advance(player, AdvancementLevel.IRON);
    }

    public static boolean absorbRemnant(ServerPlayer player, MadraType.Element aspect) {
        Core core = player.getData(ModAttachments.CORE);
        if (core.getAdvancement() != AdvancementLevel.JADE || !core.hasJadeSpiral()) return false;
        if (!player.getData(ModAttachments.MADRA_TYPE).getTypes().contains(aspect)) {
            player.sendSystemMessage(Component.translatable("message.project_cradle.remnant_incompatible"));
            return false;
        }
        core.setGoldRoute(GoldRoute.REMNANT);
        core.setGoldsign(aspect.id());
        player.getData(ModAttachments.MAXMADRA).setALevel(Math.max(2_000.0, player.getData(ModAttachments.MAXMADRA).getLevel()));
        player.syncData(ModAttachments.MAXMADRA);
        boolean advanced = advance(player, AdvancementLevel.LOWGOLD);
        GoldsignAppearanceService.refresh(player);
        return advanced;
    }

    public static boolean alignIcon(ServerPlayer player, String concept) {
        Core core = player.getData(ModAttachments.CORE);
        if (core.getAdvancement().order() < AdvancementLevel.UNDERLORD.order()) {
            player.sendSystemMessage(Component.translatable("message.project_cradle.icon_requires_underlord"));
            return false;
        }
        if (core.getWillpower() < ProgressionRules.ICON_WILLPOWER) {
            player.sendSystemMessage(Component.translatable("message.project_cradle.icon_requires_willpower", core.getWillpower(), ProgressionRules.ICON_WILLPOWER));
            return false;
        }
        if (!core.addIcon(concept)) {
            player.sendSystemMessage(Component.translatable("message.project_cradle.icon_already_aligned", ProgressionHints.iconName(concept)));
            return false;
        }
        boolean wasMonarch = core.isMonarch();
        core.setSage(true);
        player.sendSystemMessage(Component.translatable("message.project_cradle.sage_awakened", ProgressionHints.iconName(concept)));
        award(player, "reach_sage", "reached");
        finishMonarchPath(player, core, wasMonarch);
        sync(player);
        return true;
    }

    public static void addSoulfire(ServerPlayer player, double amount) {
        Core core = player.getData(ModAttachments.CORE);
        if (core.getAdvancement().order() < AdvancementLevel.TRUEGOLD.order() || !core.hasSoulspace()) {
            player.sendSystemMessage(Component.translatable("message.project_cradle.soulfire_needs_soulspace"));
            return;
        }
        core.addSoulfire(amount);
        sync(player);
    }

    public static boolean advance(ServerPlayer player, AdvancementLevel next) {
        Core core = player.getData(ModAttachments.CORE);
        if (next == null || next.order() <= core.getAdvancement().order()) {
            return false;
        }
        core.setAdvancement(next);
        applyPermanentBenefits(player, core);
        GoldsignAppearanceService.refresh(player);
        player.sendSystemMessage(Component.translatable("message.project_cradle.advancement_reached", Component.translatable("advancement.project_cradle." + next.id())));
        award(player, "reach_" + next.id(), "reached");
        sync(player);
        return true;
    }

    /** Re-applies persistent attributes after login or respawn. */
    public static void applyPermanentBenefits(ServerPlayer player, Core core) {
        AdvancementLevel level = core.getAdvancement();
        if (core.hasIronBody() || level.order() >= AdvancementLevel.IRON.order()) {
            addModifier(player, "iron_body_health", Attributes.MAX_HEALTH, 4.0);
            addModifier(player, "iron_body_armor", Attributes.ARMOR, 2.0);
            addModifier(player, "iron_body_damage", Attributes.ATTACK_DAMAGE, 1.0);
        }
        if (level.order() >= AdvancementLevel.IRON.order()) IronBodyBenefits.applyPermanent(player, core.getIronBodyType(), level);
        if (level.order() >= AdvancementLevel.UNDERLORD.order()) {
            addModifier(player, "underlord_health", Attributes.MAX_HEALTH, 6.0);
        }
        if (level.order() >= AdvancementLevel.OVERLORD.order()) {
            addModifier(player, "overlord_health", Attributes.MAX_HEALTH, 4.0);
        }
        if (level.order() >= AdvancementLevel.ARCHLORD.order()) {
            addModifier(player, "archlord_health", Attributes.MAX_HEALTH, 6.0);
        }
        if (core.hasHerald() || core.isMonarch()) {
            addModifier(player, "herald_armor", Attributes.ARMOR, 4.0);
            addModifier(player, "herald_damage", Attributes.ATTACK_DAMAGE, 3.0);
            addModifier(player, "herald_knockback", Attributes.KNOCKBACK_RESISTANCE, 0.5);
        }
        if (level.order() >= AdvancementLevel.LOWGOLD.order()) {
            applyGoldsign(player, core.getGoldsign());
        }
    }

    /** Reapplies combat passives whose vanilla representation is a status effect. */
    public static void applyTimedBenefits(ServerPlayer player) {
        Core core = player.getData(ModAttachments.CORE);
        if (core.getAdvancement().order() >= AdvancementLevel.IRON.order()) {
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 240, 0, true, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 240, 0, true, false, false));
        }
        if (core.getAdvancement().order() >= AdvancementLevel.IRON.order()) IronBodyBenefits.applyTimed(player, core.getIronBodyType(), core.getAdvancement());
        if (core.hasHerald() || core.isMonarch()) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 240, 1, true, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 240, 2, true, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 240, 2, true, false, false));
        }
    }

    private static void addModifier(ServerPlayer player, String path, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, double amount) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        Identifier id = Identifier.fromNamespaceAndPath(ProjectCradle.MODID, path);
        if (instance.getModifier(id) == null) {
            instance.addPermanentModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    private static void applyGoldsign(ServerPlayer player, String sign) {
        switch (sign) {
            case "fire" -> addModifier(player, "goldsign_fire", Attributes.ATTACK_SPEED, 0.15);
            case "water" -> addModifier(player, "goldsign_water", Attributes.MOVEMENT_SPEED, 0.10);
            case "air" -> addModifier(player, "goldsign_air", Attributes.MOVEMENT_SPEED, 0.15);
            case "earth" -> addModifier(player, "goldsign_earth", Attributes.ARMOR, 2.0);
            case "pure" -> addModifier(player, "goldsign_pure", Attributes.MAX_HEALTH, 2.0);
            case "life" -> addModifier(player, "goldsign_life", Attributes.MAX_HEALTH, 4.0);
            case "death" -> addModifier(player, "goldsign_death", Attributes.ATTACK_DAMAGE, 1.5);
            case "light" -> addModifier(player, "goldsign_light", Attributes.MOVEMENT_SPEED, 0.10);
            case "shadow" -> addModifier(player, "goldsign_shadow", Attributes.KNOCKBACK_RESISTANCE, 0.15);
            case "blood" -> addModifier(player, "goldsign_blood", Attributes.ATTACK_DAMAGE, 1.5);
            case "dream" -> addModifier(player, "goldsign_dream", Attributes.FOLLOW_RANGE, 8.0);
            case "destruction" -> addModifier(player, "goldsign_destruction", Attributes.ATTACK_DAMAGE, 2.0);
            case "force" -> addModifier(player, "goldsign_force", Attributes.KNOCKBACK_RESISTANCE, 0.20);
            case "sword" -> addModifier(player, "goldsign_sword", Attributes.ATTACK_SPEED, 0.20);
            case "hunger" -> addModifier(player, "goldsign_hunger", Attributes.ATTACK_DAMAGE, 2.0);
            default -> {
            }
        }
    }


    public static boolean openSoulspace(ServerPlayer player) {
        Core core = player.getData(ModAttachments.CORE);
        if (core.hasSoulspace()) return true;
        if (core.getAdvancement().order() < AdvancementLevel.TRUEGOLD.order()
                || player.getData(ModAttachments.MAXMADRA).getLevel() < AdvancementLevel.TRUEGOLD.requiredMaxMadra()) {
            player.sendSystemMessage(Component.translatable("message.project_cradle.soulspace_needs_peak_truegold"));
            return false;
        }
        if (!core.getIronBodyType().isPerfect() || !core.hasJadeSpiral() || !core.hasMasteredRemnant()) {
            player.sendSystemMessage(Component.translatable("message.project_cradle.lord_needs_foundation"));
            return false;
        }
        core.setSoulspace(true);
        player.sendSystemMessage(Component.translatable("message.project_cradle.soulspace_opened"));
        sync(player);
        return true;
    }

    public static boolean recordRevelation(ServerPlayer player, String statement) {
        Core core = player.getData(ModAttachments.CORE);
        LordRevelation revelation = LordRevelation.forStage(core.getAdvancement());
        if (revelation == null) {
            player.sendSystemMessage(Component.translatable("message.project_cradle.no_lord_revelation"));
            return false;
        }
        statement = statement.trim();
        if (statement.length() < 8 || statement.length() > 160 || statement.chars().filter(Character::isLetter).count() < 4) {
            player.sendSystemMessage(Component.translatable("message.project_cradle.revelation_needs_statement"));
            return false;
        }
        if (!openSoulspace(player) || !core.getIronBodyType().isPerfect() || !core.hasJadeSpiral() || !core.hasMasteredRemnant()) {
            player.sendSystemMessage(Component.translatable("message.project_cradle.lord_needs_foundation"));
            return false;
        }
        AdvancementLevel current = core.getAdvancement();
        double cost = current.requiredSoulfire();
        if (player.getData(ModAttachments.MAXMADRA).getLevel() < current.requiredMaxMadra() || !core.hasSoulfire(cost)) {
            player.sendSystemMessage(Component.translatable("message.project_cradle.revelation_needs_power", current.requiredMaxMadra(), cost));
            return false;
        }
        if (!core.trySpendSoulfire(cost)) return false;
        core.recordRevelation(revelation, statement);
        return advance(player, revelation.target());
    }

    public static boolean completeHeraldFusion(ServerPlayer player) {
        Core core = player.getData(ModAttachments.CORE);
        if (core.getAdvancement() != AdvancementLevel.ARCHLORD || core.hasHerald()
                || core.getWillpower() < ProgressionRules.HERALD_WILLPOWER
                || player.getData(ModAttachments.MAXMADRA).getLevel() < ProgressionRules.PEAK_ARCHLORD_MADRA) return false;
        boolean wasMonarch = core.isMonarch();
        core.setHerald(true);
        applyPermanentBenefits(player, core);
        player.sendSystemMessage(Component.translatable("message.project_cradle.herald_awakened"));
        award(player, "reach_herald", "reached");
        finishMonarchPath(player, core, wasMonarch);
        sync(player);
        return true;
    }

    private static void giveOrDrop(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    private static void finishMonarchPath(ServerPlayer player, Core core, boolean wasMonarch) {
        if (!wasMonarch && core.isMonarch()) {
            core.setAdvancement(AdvancementLevel.MONARCH);
            applyPermanentBenefits(player, core);
            player.sendSystemMessage(Component.translatable("message.project_cradle.monarch_awakened"));
            award(player, "reach_monarch", "reached");
        }
    }

    private static void award(ServerPlayer player, String advancementPath, String criterion) {
        var advancement = player.level().getServer().getAdvancements().get(
                Identifier.fromNamespaceAndPath(ProjectCradle.MODID, advancementPath)
        );
        if (advancement != null) {
            player.getAdvancements().award(advancement, criterion);
        }
    }

    private static void sync(ServerPlayer player) {
        player.syncData(ModAttachments.CORE);
    }
}
