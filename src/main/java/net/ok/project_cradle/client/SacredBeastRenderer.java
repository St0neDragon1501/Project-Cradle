package net.ok.project_cradle.client;

import com.google.common.reflect.TypeToken;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EnderDragonRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.SacredBeastAppearance;
import net.ok.project_cradle.client.SacredBeastModel.Shape;

/** Adds solid spirit features after a vanilla body, without changing its model, equipment, or layers. */
@EventBusSubscriber(modid = ProjectCradle.MODID, value = Dist.CLIENT)
public final class SacredBeastRenderer {
    private static final ContextKey<AppearanceState> APPEARANCE = new ContextKey<>(
            Identifier.fromNamespaceAndPath(ProjectCradle.MODID, "sacred_beast_appearance"));
    private static final SacredBeastModel MODEL = new SacredBeastModel();

    private SacredBeastRenderer() { }

    public static SacredBeastAppearance appearance(EntityRenderState state) {
        AppearanceState appearance = state.getRenderData(APPEARANCE);
        return appearance == null ? SacredBeastAppearance.NONE : appearance.appearance;
    }

    @SubscribeEvent
    public static void registerRenderStateModifier(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier(new TypeToken<EntityRenderer<Entity, EntityRenderState>>() { }, (entity, state) -> {
            state.setRenderData(APPEARANCE, null);
            if (!(entity instanceof Mob mob) || entity.isSpectator()) return;
            SacredBeastAppearance appearance = mob.getData(ModAttachments.SACRED_BEAST_APPEARANCE);
            if (!appearance.sacred()) return;
            float width = mob.getBbWidth();
            float height = mob.getBbHeight();
            if (mob.isSleeping()) {
                var standing = mob.getDimensions(Pose.STANDING);
                width = standing.width();
                height = standing.height();
            }
            state.setRenderData(APPEARANCE, new AppearanceState(appearance, shape(mob),
                    mob.getPreciseBodyRotation(state.partialTick), width, height));
        });
    }

    @SubscribeEvent
    public static void registerDragonRenderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityTypes.ENDER_DRAGON, SacredDragonRenderer::new);
    }

    @SubscribeEvent
    public static void afterLivingBody(RenderLivingEvent.Post<?, ?, ?> event) {
        LivingEntityRenderState state = event.getRenderState();
        AppearanceState appearance = state.getRenderData(APPEARANCE);
        if (appearance == null || state.isInvisible || state.deathTime >= 20.0F) return;
        PoseStack poses = event.getPoseStack();
        poses.pushPose();
        if (state.hasPose(Pose.SLEEPING)) {
            // Keep crests and mantles with a sleeping body instead of standing above the bed.
            poses.translate(0.0F, 0.18F, 0.0F);
            poses.mulPose(Axis.YP.rotationDegrees(sleepRotation(state.bedOrientation, appearance.yaw)));
            poses.mulPose(Axis.ZP.rotationDegrees(90.0F));
        } else {
            poses.mulPose(Axis.YP.rotationDegrees(180.0F - appearance.yaw));
            if (state.deathTime > 0.0F) {
                float fall = Math.min(1.0F, Mth.sqrt((state.deathTime - 1.0F) / 20.0F * 1.6F));
                poses.mulPose(Axis.ZP.rotationDegrees(-fall * 90.0F));
            } else if (state.isUpsideDown) {
                poses.translate(0.0F, appearance.height + 0.1F, 0.0F);
                poses.mulPose(Axis.ZP.rotationDegrees(180.0F));
            }
        }
        submit(state, appearance, poses, event.getSubmitNodeCollector());
        poses.popPose();
    }

    /** The Dragon's phase renderer does not pass through RenderLivingEvent. */
    static void afterDragonBody(EnderDragonRenderState state, PoseStack poses, SubmitNodeCollector collector) {
        AppearanceState appearance = state.getRenderData(APPEARANCE);
        if (appearance == null || state.isInvisible || state.deathTime > 0.0F) return;
        poses.pushPose();
        // These rotations mirror the vanilla phase transform, keeping its crown with the flying body.
        poses.mulPose(Axis.YP.rotationDegrees(-state.getHistoricalPos(7).yRot()));
        float slope = (float) (state.getHistoricalPos(5).y() - state.getHistoricalPos(10).y());
        poses.mulPose(Axis.XP.rotationDegrees(slope * 10.0F));
        poses.translate(0.0F, 0.0F, 1.0F);
        submit(state, appearance, poses, collector);
        poses.popPose();
    }

    private static void submit(EntityRenderState state, AppearanceState appearance, PoseStack poses,
            SubmitNodeCollector collector) {
        float maxWidth = appearance.shape == Shape.DRAGON ? 16.0F : 12.0F;
        float maxHeight = appearance.shape == Shape.DRAGON ? 8.0F : 24.0F;
        MODEL.submit(poses, collector, appearance.appearance, appearance.shape,
                Mth.clamp(appearance.width, 0.12F, maxWidth), Mth.clamp(appearance.height, 0.12F, maxHeight),
                state.ageInTicks, state.lightCoords, state.outlineColor);
    }

    private static float sleepRotation(Direction orientation, float yaw) {
        if (orientation == null) return 180.0F - yaw;
        return switch (orientation) {
            case SOUTH -> 90.0F;
            case WEST -> 0.0F;
            case NORTH -> 270.0F;
            case EAST -> 180.0F;
            default -> 180.0F - yaw;
        };
    }

    private static Shape shape(Mob mob) {
        Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        String species = id == null ? "" : id.getPath();
        return switch (species) {
            case "ender_dragon" -> Shape.DRAGON;
            case "slime", "magma_cube", "sulfur_cube", "ghast", "happy_ghast", "blaze" -> Shape.AMORPHOUS;
            case "chicken", "parrot", "bat", "phantom", "breeze", "allay", "vex" -> Shape.AVIAN;
            case "cod", "salmon", "tropical_fish", "pufferfish", "squid", "glow_squid", "nautilus",
                    "zombie_nautilus", "axolotl", "dolphin", "turtle", "frog", "tadpole", "guardian", "elder_guardian" -> Shape.AQUATIC;
            case "spider", "cave_spider", "bee", "silverfish", "endermite" -> Shape.ARTHROPOD;
            case "cow", "mooshroom", "pig", "sheep", "goat", "rabbit", "panda", "sniffer", "armadillo",
                    "horse", "donkey", "mule", "llama", "trader_llama", "camel", "camel_husk", "wolf",
                    "cat", "ocelot", "fox", "polar_bear", "hoglin", "zoglin", "strider", "zombie_horse",
                    "skeleton_horse", "ravager" -> Shape.HORNED;
            case "villager", "wandering_trader", "iron_golem", "snow_golem", "copper_golem", "creaking" -> Shape.HUMANOID;
            default -> switch (mob.getType().getCategory()) {
                case AXOLOTLS, WATER_CREATURE, WATER_AMBIENT, UNDERGROUND_WATER_CREATURE -> Shape.AQUATIC;
                case AMBIENT -> Shape.AVIAN;
                case MONSTER -> Shape.HUMANOID;
                default -> Shape.HORNED;
            };
        };
    }

    /** Only immutable values cross into the deferred rendering pipeline. */
    private record AppearanceState(SacredBeastAppearance appearance, Shape shape, float yaw, float width, float height) { }
}
