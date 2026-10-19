package net.ok.project_cradle.server.event;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.SacredBeastAge;
import net.ok.project_cradle.capability.core.Core;
import net.ok.project_cradle.capability.core.advancement.AdvancementLevel;
import net.ok.project_cradle.items.ModItems;
import net.ok.project_cradle.server.world.AuraService;
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

    @SubscribeEvent
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
        // A Remnant comes from the deceased artist's spirit, regardless of the killer or location.
        Core deceased = event.getEntity().getData(ModAttachments.CORE);
        if (event.getEntity() instanceof Vex
                || deceased.getAdvancement().order() < AdvancementLevel.IRON.order()
                || level.getRandom().nextInt(4) != 0) {
            return;
        }

        Vex remnant = EntityTypes.VEX.create(level, EntitySpawnReason.EVENT);
        if (remnant == null) {
            return;
        }
        remnant.snapTo(event.getEntity().getX(), event.getEntity().getY() + 1.0,
                event.getEntity().getZ(), event.getEntity().getYRot(), 0.0F);
        remnant.setCustomName(Component.translatable("entity.project_cradle.remnant"));
        remnant.setCustomNameVisible(true);
        remnant.setPersistenceRequired();
        remnant.addTag(REMNANT_TAG);
        remnant.getData(ModAttachments.CORE).setAdvancement(deceased.getAdvancement());
        remnant.getData(ModAttachments.MADRA_TYPE).copyFrom(event.getEntity().getData(ModAttachments.MADRA_TYPE));
        if (event.getSource().getEntity() instanceof ServerPlayer killer) {
            remnant.setTarget(killer);
        }
        level.addFreshEntity(remnant);
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }

        Entity entity = event.getEntity();
        if (RemnantFusionService.isOwnRemnant(entity)) {
            return;
        }
        boolean remnant = entity.entityTags().contains(REMNANT_TAG);
        SacredBeastAge beastAge = entity.getData(ModAttachments.SACRED_BEAST_AGE);
        if (!remnant && !beastAge.isSacred()) {
            return;
        }

        if (remnant) {
            ItemStack binding = new ItemStack(ModItems.REMNANT_BINDING.get());
            RemnantAspectData.set(binding, entity.getData(ModAttachments.MADRA_TYPE).getTypes().get(0));
            event.getDrops().add(new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), binding));
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
                age.setSacred(true);
                strengthenSacredBeast(animal);
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
        dreadgod.setCustomName(Component.translatable("entity.project_cradle.dreadgod"));
        dreadgod.setCustomNameVisible(true);
        dreadgod.setPersistenceRequired();
        level.addFreshEntity(dreadgod);
    }

    private static void strengthenSacredBeast(Animal animal) {
        animal.getData(ModAttachments.CORE).setAdvancement(AdvancementLevel.LOWGOLD);
        animal.getData(ModAttachments.MADRA_TYPE).setTypes(List.of(MadraType.Element.LIFE));
        animal.getData(ModAttachments.MAXMADRA).setALevel(2_000.0);
        addModifier(animal, "sacred_beast_health", Attributes.MAX_HEALTH, 40.0);
        addModifier(animal, "sacred_beast_damage", Attributes.ATTACK_DAMAGE, 8.0);
        addModifier(animal, "sacred_beast_knockback", Attributes.KNOCKBACK_RESISTANCE, 0.75);
        if (animal instanceof Mob mob) {
            mob.setPersistenceRequired();
        }
        if (animal.getCustomName() == null) {
            animal.setCustomName(Component.translatable("entity.project_cradle.sacred_beast"));
            animal.setCustomNameVisible(true);
        }
    }

    private static void addModifier(Entity entity, String name,
            net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, double amount) {
        if (!(entity instanceof net.minecraft.world.entity.LivingEntity living)) {
            return;
        }
        AttributeInstance instance = living.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        Identifier id = Identifier.fromNamespaceAndPath(ProjectCradle.MODID, name);
        if (instance.getModifier(id) == null) {
            instance.addPermanentModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    private static boolean isAtLeastJade(Core core) {
        return core.getAdvancement().order() >= AdvancementLevel.JADE.order();
    }

}
