package net.ok.project_cradle.server.event;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.entities.ModEntities;
import net.ok.project_cradle.entities.RemnantEntity;
import net.ok.project_cradle.entities.RemnantForm;
import net.ok.project_cradle.spells.TechniqueCatalog;
import net.ok.project_cradle.server.progression.TechniqueKnowledge;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.SacredBeastAge;
import net.ok.project_cradle.capability.core.Core;
import net.ok.project_cradle.capability.core.advancement.AdvancementLevel;
import net.ok.project_cradle.items.ModItems;
import net.ok.project_cradle.server.world.AuraService;
import net.ok.project_cradle.server.world.SacredBeastService;
import net.ok.project_cradle.server.progression.RemnantFusionService;
import net.ok.project_cradle.server.progression.SpiritBondService;
import net.ok.project_cradle.server.progression.RemnantAspectData;
import net.ok.project_cradle.capability.madra.MadraType;

/** World events for Remnants, sacred wildlife, sacred plants, and Dreadgods. */
@EventBusSubscriber(modid = ProjectCradle.MODID)
public final class SacredWorldEvents {
    private static final String REMNANT_TAG = "project_cradle.remnant";
    // One loaded Minecraft day represents a year so a century-old beast is reachable in survival.
    private static final long SACRED_BEAST_AGE_TICKS = 100L * 24_000L;
    private static final long DREADGOD_INTERVAL = 7L * 24_000L;

    private SacredWorldEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }
        if (RemnantFusionService.isOwnRemnant(event.getEntity())) {
            if (event.getSource().getEntity() instanceof ServerPlayer killer) {
                RemnantFusionService.onDefeated(event.getEntity(), killer);
            }
            return;
        }
        trySpawnRemnant(event.getEntity(), event.getSource().getEntity() instanceof LivingEntity attacker ? attacker : null);
    }

    /** Shared with deaths that bypass LivingDeathEvent, such as a creeper's own explosion. */
    public static void trySpawnRemnant(LivingEntity source, LivingEntity attacker) {
        if (!(source.level() instanceof ServerLevel level)
                || source.getPersistentData().getBooleanOr("project_cradle.remnant_formed", false)) return;
        // A developed, viable spirit leaves an echo; existing mod spirits cannot leave another one.
        Core deceased = source.getData(ModAttachments.CORE);
        boolean sacredBeast = source.getData(ModAttachments.SACRED_BEAST_AGE).isSacred();
        if (source instanceof Vex && !sacredBeast || source instanceof RemnantEntity
                || source.entityTags().contains(REMNANT_TAG)
                || source.entityTags().contains(SpiritBondService.WILD_SPIRIT_TAG)
                || source.entityTags().contains("project_cradle.dreadbeast")
                || source.entityTags().contains("project_cradle.dreadgod")
                || source.entityTags().contains("project_cradle.spirit_destroyed")
                || source.getPersistentData().getBooleanOr("project_cradle.spirit_destroyed", false)
                || source instanceof WitherBoss && source.getCustomName() != null
                        && source.getCustomName().getString().equals("Dreadgod")
                || !sacredBeast && deceased.getAdvancement().order() < AdvancementLevel.IRON.order()) {
            return;
        }

        RemnantEntity remnant = ModEntities.REMNANT.get().create(level, EntitySpawnReason.EVENT);
        if (remnant == null) return;
        remnant.inheritSpirit(source);
        // Old sacred beasts predate saved advancement; they still have a developed spirit.
        if (sacredBeast && deceased.getAdvancement().order() < AdvancementLevel.IRON.order()) {
            remnant.setSpirit(AdvancementLevel.LOWGOLD, source.getData(ModAttachments.MADRA_TYPE).getTypes(), RemnantForm.BEAST);
        }
        remnant.snapTo(source.getX(), source.getY() + 0.6, source.getZ(), source.getYRot(), 0.0F);
        remnant.setPersistenceRequired();
        remnant.addTag(REMNANT_TAG);
        if (source instanceof ServerPlayer || source.hasCustomName()) {
            remnant.setCustomName(Component.translatable("entity.project_cradle.remnant_of", source.getName()));
            remnant.setCustomNameVisible(true);
        }
        if (attacker != null && attacker != source && attacker.isAlive() && !attacker.isSpectator()) remnant.setTarget(attacker);
        if (level.addFreshEntity(remnant)) {
            source.getPersistentData().putBoolean("project_cradle.remnant_formed", true);
            remnant.spawnAnim();
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }

        Entity entity = event.getEntity();
        boolean remnant = entity instanceof RemnantEntity || entity.entityTags().contains(REMNANT_TAG)
                || RemnantFusionService.isOwnRemnant(entity);
        SacredBeastAge beastAge = entity.getData(ModAttachments.SACRED_BEAST_AGE);
        if (!remnant && !beastAge.isSacred()) {
            return;
        }

        if (remnant) {
            ItemStack binding = new ItemStack(ModItems.REMNANT_BINDING.get());
            RemnantAspectData.set(binding, entity.getData(ModAttachments.MADRA_TYPE).getTypes());
            event.getDrops().add(new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), binding));
            List<String> known = entity instanceof RemnantEntity spirit ? spirit.getKnownTechniques()
                    : TechniqueKnowledge.capture(event.getEntity());
            List<String> extra = known.stream().filter(id -> !id.equals(TechniqueCatalog.basic())).toList();
            List<String> choices = extra.isEmpty() ? known : extra;
            String id = choices.isEmpty() ? TechniqueCatalog.basic() : choices.get(level.getRandom().nextInt(choices.size()));
            ItemStack technique = TechniqueCatalog.stack(id);
            if (technique.isEmpty()) technique = new ItemStack(ModItems.BASIC_STRIKER.get());
            event.getDrops().add(new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), technique));
        }
        if (beastAge.isSacred()) {
            event.getDrops().add(new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(),
                    new ItemStack(ModItems.SACRED_HERB.get(), 2 + level.getRandom().nextInt(3))));
        }
    }

    @SubscribeEvent
    public static void onDreadgodDamage(LivingIncomingDamageEvent event) {
        Entity victim = event.getEntity();
        if (!(victim instanceof WitherBoss wither) || !(wither.level() instanceof ServerLevel level)
                || wither.getCustomName() == null
                || !wither.getCustomName().getString().equals("Dreadgod")
                || !(event.getSource().getEntity() instanceof ServerPlayer)) {
            return;
        }

        long artistsNearby = level.players().stream()
                .filter(player -> player.distanceToSqr(wither) <= 32.0 * 32.0)
                .filter(player -> player.getData(ModAttachments.CORE).getAdvancement().order()
                        >= AdvancementLevel.FOUNDATION.order())
                .count();
        float groupMultiplier = Math.min(1.0F, artistsNearby / 3.0F);
        event.setAmount(event.getAmount() * groupMultiplier);
    }

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof ServerPlayer player)
                || !isAtLeastJade(player.getData(ModAttachments.CORE))
                || !(event.getState().is(BlockTags.FLOWERS) || event.getState().is(BlockTags.LEAVES))
                || event.getLevel().getRandom().nextInt(400) != 0) {
            return;
        }
        event.getDrops().add(new ItemEntity(event.getLevel(), event.getPos().getX() + 0.5,
                event.getPos().getY() + 0.5, event.getPos().getZ() + 0.5,
                new ItemStack(ModItems.SACRED_HERB.get())));
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Animal animal) || animal.level().isClientSide() || animal.isBaby()) {
            return;
        }

        SacredBeastAge age = animal.getData(ModAttachments.SACRED_BEAST_AGE);
        if (age.isSacred()) {
            return;
        }
        age.tick();
        if (!age.wasCenturyChecked() && age.getLivedTicks() >= SACRED_BEAST_AGE_TICKS) {
            age.markCenturyChecked();
            if (animal.getRandom().nextInt(20) == 0) {
                SacredBeastService.awaken(animal);
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ServerLevel overworld = event.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return;
        }

        long gameTime = overworld.getGameTime();
        if (gameTime % 6_000L == 0L) {
            for (ServerPlayer player : overworld.players()) {
                trySpawnWildSpirit(overworld, player);
            }
        }

        if (gameTime > 0L && gameTime % DREADGOD_INTERVAL == 0L) {
            List<ServerPlayer> monarchs = overworld.players().stream()
                    .filter(player -> player.getData(ModAttachments.CORE).isMonarch())
                    .toList();
            if (!monarchs.isEmpty() && !hasLivingDreadgod(overworld)) {
                spawnDreadgod(overworld, monarchs);
            }
        }
    }

    private static void trySpawnWildSpirit(ServerLevel level, ServerPlayer player) {
        if (player.getRandom().nextInt(100) != 0) {
            return;
        }
        AuraService.Reading aura = AuraService.detect(player);
        if (!aura.isBalanced() || aura.strength() < 80) {
            return;
        }
        boolean nearbySpirit = !level.getEntitiesOfClass(Allay.class, player.getBoundingBox().inflate(48.0), allay ->
                allay.getCustomName() != null && allay.getCustomName().getString().equals("Wild Spirit")).isEmpty();
        if (nearbySpirit) {
            return;
        }

        int x = player.blockPosition().getX() + player.getRandom().nextInt(33) - 16;
        int z = player.blockPosition().getZ() + player.getRandom().nextInt(33) - 16;
        BlockPos spawnPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
        Allay spirit = EntityTypes.ALLAY.create(level, EntitySpawnReason.EVENT);
        if (spirit == null) {
            return;
        }
        spirit.snapTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
                player.getRandom().nextFloat() * 360.0F, 0.0F);
        spirit.setCustomName(Component.translatable("entity.project_cradle.wild_spirit"));
        spirit.setCustomNameVisible(true);
        spirit.setPersistenceRequired();
        spirit.addTag(SpiritBondService.WILD_SPIRIT_TAG);
        spirit.getData(ModAttachments.CORE).setAdvancement(AdvancementLevel.LOWGOLD);
        spirit.getData(ModAttachments.MADRA_TYPE).setTypes(List.of(aura.aspect()));
        spirit.getData(ModAttachments.MAXMADRA).setALevel(2_000.0);
        level.addFreshEntity(spirit);
    }

    private static boolean hasLivingDreadgod(ServerLevel level) {
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof WitherBoss wither && wither.isAlive() && wither.getCustomName() != null
                    && wither.getCustomName().getString().equals("Dreadgod")) {
                return true;
            }
        }
        return false;
    }

    private static void spawnDreadgod(ServerLevel level, List<ServerPlayer> monarchs) {
        ServerPlayer anchor = monarchs.get(level.getRandom().nextInt(monarchs.size()));
        int x = anchor.blockPosition().getX() + level.getRandom().nextInt(49) - 24;
        int z = anchor.blockPosition().getZ() + level.getRandom().nextInt(49) - 24;
        BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));

        WitherBoss dreadgod = EntityTypes.WITHER.create(level, EntitySpawnReason.EVENT);
        if (dreadgod == null) {
            return;
        }
        dreadgod.snapTo(surface.getX() + 0.5, surface.getY() + 8.0, surface.getZ() + 0.5,
                level.getRandom().nextFloat() * 360.0F, 0.0F);
        double maxHealth = 300.0 + 200.0 * (monarchs.size() - 1);
        AttributeInstance health = dreadgod.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.setBaseValue(maxHealth);
            dreadgod.setHealth((float) maxHealth);
        }
        AttributeInstance speed = dreadgod.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.setBaseValue(0.6 + 0.04 * (monarchs.size() - 1));
        }
        dreadgod.addTag("project_cradle.dreadgod");
        dreadgod.setCustomName(Component.translatable("entity.project_cradle.dreadgod"));
        dreadgod.setCustomNameVisible(true);
        dreadgod.setPersistenceRequired();
        level.addFreshEntity(dreadgod);
    }

    private static boolean isAtLeastJade(Core core) {
        return core.getAdvancement().order() >= AdvancementLevel.JADE.order();
    }

}
