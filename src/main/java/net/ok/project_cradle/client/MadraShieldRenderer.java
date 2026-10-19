package net.ok.project_cradle.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.ok.project_cradle.entities.MadraShieldEntity;

/** Original geometric runes and a translucent wall use a neutral vanilla material. */
public final class MadraShieldRenderer extends EntityRenderer<MadraShieldEntity, MadraShieldRenderer.State> {
    private static final Identifier MATERIAL = Identifier.withDefaultNamespace("textures/block/white_concrete.png");
    private final ModelPart pane;
    private final ModelPart frame;
    private final ModelPart runes;
    private final ModelPart cracks;
    private final ModelPart durability;

    public MadraShieldRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
        ModelPart model = createGeometry().bakeRoot();
        this.pane = model.getChild("pane");
        this.frame = model.getChild("frame");
        this.runes = model.getChild("runes");
        this.cracks = model.getChild("cracks");
        this.durability = model.getChild("durability");
    }

    @Override
    public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(MadraShieldEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.normalX = entity.isNormalX();
        state.healthFraction = Mth.clamp(entity.getHealth() / Math.max(1.0F, entity.getMaxHealth()), 0.0F, 1.0F);
        state.remainingTicks = entity.getRemainingTicks();
        state.hitFlash = entity.getHitFlashTicks();
    }

    @Override
    public void submit(State state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera) {
        float fade = Math.min(1.0F, state.ageInTicks / 6.0F) * Math.min(1.0F, state.remainingTicks / 16.0F);
        float pulse = 0.93F + 0.07F * (float) Math.sin(state.ageInTicks * 0.09F);
        float flash = state.hitFlash / 6.0F;
        poses.pushPose();
        if (state.normalX) poses.mulPose(Axis.YP.rotationDegrees(90.0F));
        draw(this.pane, poses, collector, color((0.20F + flash * 0.28F) * fade, 69, 182, 218), state, 0);
        draw(this.frame, poses, collector, color(0.92F * fade, 115 + (int) (flash * 100), 222, 244), state, 1);
        draw(this.runes, poses, collector, color(0.78F * fade * pulse, 167, 244, 255), state, 2);
        if (state.healthFraction < 0.65F) {
            draw(this.cracks, poses, collector, color((1.0F - state.healthFraction) * fade, 240, 180, 151), state, 3);
        }
        poses.pushPose();
        poses.scale(state.healthFraction, 1.0F, 1.0F);
        draw(this.durability, poses, collector, color(0.95F * fade, 132, 244, 204), state, 3);
        poses.popPose();
        poses.popPose();
        super.submit(state, poses, collector, camera);
    }

    private static void draw(ModelPart part, PoseStack poses, SubmitNodeCollector collector, int color, State state, int order) {
        collector.order(order).submitModelPart(part, poses, RenderTypes.entityTranslucentEmissive(MATERIAL),
                LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, null, color, null, state.outlineColor);
    }

    private static int color(float alpha, int red, int green, int blue) {
        return (Mth.clamp((int) (alpha * 255.0F), 0, 255) << 24) | (red << 16) | (green << 8) | blue;
    }

    private static CubeListBuilder cubes() { return CubeListBuilder.create().texOffs(0, 0); }

    private static LayerDefinition createGeometry() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("pane", cubes().addBox(-23.0F, 1.0F, -0.5F, 46.0F, 46.0F, 1.0F), PartPose.ZERO);
        PartDefinition frame = root.addOrReplaceChild("frame", cubes()
                .addBox(-24.0F, 0.0F, -1.0F, 48.0F, 1.6F, 2.0F)
                .addBox(-24.0F, 46.4F, -1.0F, 48.0F, 1.6F, 2.0F)
                .addBox(-24.0F, 1.6F, -1.0F, 1.6F, 44.8F, 2.0F)
                .addBox(22.4F, 1.6F, -1.0F, 1.6F, 44.8F, 2.0F), PartPose.ZERO);
        for (int x : new int[] {-1, 1}) {
            for (int y : new int[] {0, 1}) {
                frame.addOrReplaceChild("corner_" + x + "_" + y,
                        cubes().addBox(-2.2F, -2.2F, -1.25F, 4.4F, 4.4F, 2.5F),
                        PartPose.offsetAndRotation(x * 21.5F, 2.5F + y * 43.0F, 0, 0, 0, (float) Math.PI / 4));
            }
        }
        PartDefinition runes = root.addOrReplaceChild("runes", cubes(), PartPose.ZERO);
        ring(runes, "outer", 18.0F, 12, 8.6F);
        ring(runes, "inner", 10.0F, 8, 6.4F);
        ring(runes, "core", 4.4F, 4, 5.8F);
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4.0;
            runes.addOrReplaceChild("mark_" + i, cubes()
                    .addBox(-0.4F, -1.7F, -1.4F, 0.8F, 3.4F, 0.5F)
                    .addBox(-1.7F, -0.4F, -1.4F, 3.4F, 0.8F, 0.5F),
                    PartPose.offsetAndRotation((float) Math.cos(angle) * 14.0F, 24.0F + (float) Math.sin(angle) * 14.0F,
                            0, 0, 0, (float) angle));
        }
        PartDefinition cracks = root.addOrReplaceChild("cracks", cubes(), PartPose.ZERO);
        cracks.addOrReplaceChild("crack_a", cubes().addBox(-0.25F, -9.0F, -1.2F, 0.5F, 18.0F, 0.4F), PartPose.offsetAndRotation(7, 32, 0, 0, 0, 0.45F));
        cracks.addOrReplaceChild("crack_b", cubes().addBox(-0.25F, -6.0F, -1.2F, 0.5F, 12.0F, 0.4F), PartPose.offsetAndRotation(-7, 19, 0, 0, 0, -0.6F));
        root.addOrReplaceChild("durability", cubes().addBox(-21.0F, 2.6F, -1.5F, 42.0F, 0.65F, 0.5F), PartPose.ZERO);
        return LayerDefinition.create(mesh, 16, 16);
    }

    private static void ring(PartDefinition root, String name, float radius, int count, float edgeLength) {
        for (int i = 0; i < count; i++) {
            double angle = i * Math.PI * 2.0 / count;
            root.addOrReplaceChild(name + "_" + i, cubes().addBox(-edgeLength / 2, -0.35F, -1.2F, edgeLength, 0.7F, 0.45F),
                    PartPose.offsetAndRotation((float) Math.cos(angle) * radius, 24.0F + (float) Math.sin(angle) * radius,
                            0, 0, 0, (float) (angle + Math.PI / 2)));
        }
    }

    public static final class State extends EntityRenderState {
        public boolean normalX;
        public float healthFraction = 1.0F;
        public int remainingTicks = MadraShieldEntity.LIFETIME_TICKS;
        public int hitFlash;
    }
}
