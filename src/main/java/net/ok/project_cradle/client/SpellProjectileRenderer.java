package net.ok.project_cradle.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.ok.project_cradle.capability.madra.MadraType;
import net.ok.project_cradle.entities.BasicStrikerProjectile;
import net.ok.project_cradle.entities.SpellBoltProjectile;
import net.ok.project_cradle.spells.AdditionalTechnique;

/** Animated spell geometry shared by Striker bolts, Path projectiles, fireballs, and ice lances. */
public final class SpellProjectileRenderer<T extends BasicStrikerProjectile>
        extends EntityRenderer<T, SpellProjectileRenderer.State> {
    private static final Identifier MATERIAL = Identifier.withDefaultNamespace("textures/block/white_concrete.png");
    private final ModelPart orb;
    private final ModelPart halo;
    private final ModelPart blade;
    private final ModelPart needle;
    private final ModelPart lance;

    public SpellProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        root.addOrReplaceChild("orb", boxes().addBox(-2, -2, -2, 4, 4, 4), PartPose.ZERO);
        CubeListBuilder ring = boxes();
        for (int i = 0; i < 16; i++) {
            double angle = i * Math.PI / 8;
            ring.addBox((float) Math.cos(angle) * 4 - 0.4F,
                    (float) Math.sin(angle) * 4 - 0.4F, -0.4F, 0.8F, 0.8F, 0.8F);
        }
        root.addOrReplaceChild("halo", ring, PartPose.ZERO);
        root.addOrReplaceChild("blade", boxes()
                .addBox(-0.8F, -0.3F, -5, 1.6F, 0.6F, 12)
                .addBox(-0.4F, -0.25F, 7, 0.8F, 0.5F, 2)
                .addBox(-3, -0.5F, -5.5F, 6, 1, 1)
                .addBox(-0.45F, -0.4F, -9, 0.9F, 0.8F, 3.5F), PartPose.ZERO);
        root.addOrReplaceChild("needle", boxes()
                .addBox(-0.25F, -0.25F, -6, 0.5F, 0.5F, 12)
                .addBox(-0.7F, -0.15F, -5, 1.4F, 0.3F, 3), PartPose.ZERO);
        root.addOrReplaceChild("lance", boxes()
                .addBox(-0.8F, -0.8F, -6, 1.6F, 1.6F, 10)
                .addBox(-0.55F, -0.55F, 4, 1.1F, 1.1F, 4)
                .addBox(-0.25F, -0.25F, 8, 0.5F, 0.5F, 3)
                .addBox(-1.7F, -0.25F, -5, 3.4F, 0.5F, 4)
                .addBox(-0.25F, -1.7F, -5, 0.5F, 3.4F, 4), PartPose.ZERO);
        ModelPart baked = LayerDefinition.create(mesh, 16, 16).bakeRoot();
        this.orb = baked.getChild("orb");
        this.halo = baked.getChild("halo");
        this.blade = baked.getChild("blade");
        this.needle = baked.getChild("needle");
        this.lance = baked.getChild("lance");
        this.shadowRadius = 0;
    }

    private static CubeListBuilder boxes() { return CubeListBuilder.create().texOffs(0, 0); }

    @Override
    public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(T entity, State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.yaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
        state.pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        state.color = color(entity.getMadraElement());
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath();
        state.shape = id.contains("sword") || id.contains("blade") ? Shape.BLADE
                : id.contains("needles") ? Shape.NEEDLE
                : id.contains("lance") || id.contains("wind") ? Shape.LANCE : Shape.ORB;
        state.scale = id.contains("dragons_breath") ? 1.25F : 0.9F;
        if (entity instanceof SpellBoltProjectile bolt) {
            state.shape = bolt.getTechnique() == AdditionalTechnique.ICE_LANCE ? Shape.LANCE : Shape.ORB;
            state.scale = bolt.getVisualScale(partialTick);
        }
    }

    @Override
    public void submit(State state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.isInvisible) return;
        poses.pushPose();
        poses.mulPose(Axis.YP.rotationDegrees(-state.yaw));
        poses.mulPose(Axis.XP.rotationDegrees(state.pitch));
        poses.scale(state.scale, state.scale, state.scale);
        poses.mulPose(Axis.ZP.rotation(state.ageInTicks * 0.12F));
        ModelPart body = switch (state.shape) {
            case BLADE -> this.blade;
            case NEEDLE -> this.needle;
            case LANCE -> this.lance;
            case ORB -> this.orb;
        };
        draw(body, poses, collector, state.color, false);
        if (state.shape == Shape.ORB) {
            draw(this.halo, poses, collector, 0x99000000 | (state.color & 0xFFFFFF), true);
            poses.scale(0.65F, 0.65F, 0.65F);
            poses.mulPose(Axis.XP.rotationDegrees(45));
            draw(this.orb, poses, collector, 0xFFF6FAFF, false);
        } else if (state.shape == Shape.LANCE) {
            poses.scale(0.5F, 0.5F, 1.03F);
            draw(this.lance, poses, collector, 0xFFEFFFFF, false);
        }
        poses.popPose();
        super.submit(state, poses, collector, camera);
    }

    private static void draw(ModelPart part, PoseStack poses, SubmitNodeCollector collector, int color, boolean translucent) {
        collector.order(translucent ? 1 : 0).submitModelPart(part, poses,
                translucent ? RenderTypes.entityTranslucentEmissive(MATERIAL) : RenderTypes.entityCutout(MATERIAL),
                LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, null, color, null);
    }

    private static int color(MadraType.Element aspect) {
        return switch (aspect) {
            case FIRE -> 0xFFFF7B30;
            case WATER -> 0xFF76DAFF;
            case AIR -> 0xFFB9F4E4;
            case EARTH -> 0xFFC3A66D;
            case PURE -> 0xFFCDEBFF;
            case LIFE -> 0xFF80E888;
            case DEATH -> 0xFF7CBEB7;
            case LIGHT -> 0xFFFFECB0;
            case SHADOW -> 0xFF9566C9;
            case BLOOD -> 0xFFE85878;
            case DREAM -> 0xFFD68EFA;
            case DESTRUCTION -> 0xFFFF383E;
            case FORCE -> 0xFFBBA5FF;
            case SWORD -> 0xFFE1F4FF;
            case HUNGER -> 0xFFAAA5BC;
        };
    }

    private enum Shape { ORB, BLADE, NEEDLE, LANCE }
    public static final class State extends EntityRenderState {
        private float yaw;
        private float pitch;
        private float scale;
        private int color;
        private Shape shape = Shape.ORB;
    }
}
