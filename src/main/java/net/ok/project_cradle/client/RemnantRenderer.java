package net.ok.project_cradle.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
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
import net.ok.project_cradle.capability.madra.MadraType;
import net.ok.project_cradle.entities.RemnantEntity;
import net.ok.project_cradle.entities.RemnantForm;

/** A translucent spirit surrounding a bright core, with its former shape suggested by ghostly madra. */
public final class RemnantRenderer extends EntityRenderer<RemnantEntity, RemnantRenderer.State> {
    private static final Identifier MATERIAL = Identifier.withDefaultNamespace("textures/block/white_concrete.png");
    private final RemnantModel model = new RemnantModel();

    public RemnantRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(RemnantEntity entity, State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.yaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
        state.pitch = Mth.clamp(Mth.lerp(partialTick, entity.xRotO, entity.getXRot()), -30.0F, 30.0F);
        state.primary = color(entity.getMadraElement());
        state.secondary = entity.hasSecondaryAspect() ? color(entity.getSecondaryMadraElement()) : tint(state.primary, 0.55F);
        state.form = entity.getRemnantForm();
        state.charging = entity.isCharging();
        state.scale = 0.95F + Mth.clamp(entity.getSourceRank().order() - 3, 0, 8) * 0.015F;
        state.fade = entity.deathTime > 0 ? Mth.clamp(1.0F - (entity.deathTime + partialTick) / 20.0F, 0.0F, 1.0F) : 1.0F;
    }

    @Override
    public void submit(State state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.isInvisible || state.fade <= 0.0F) return;
        float age = state.ageInTicks;
        float breath = (float) Math.sin(age * 0.085F);
        float pulse = 0.92F + 0.08F * (float) Math.sin(age * 0.17F);
        int shell = tint(state.primary, 0.78F);
        poses.pushPose();
        poses.translate(0, 0.04F + breath * 0.045F, 0);
        poses.mulPose(Axis.YP.rotationDegrees(-state.yaw));
        poses.scale(state.scale, state.scale, state.scale);

        // Draw the luminous interior before the translucent shell so the core remains visible through it.
        poses.pushPose();
        poses.translate(0, 18.5F / 16.0F, 0);
        poses.mulPose(Axis.YP.rotation(age * 0.035F));
        poses.mulPose(Axis.ZP.rotation((float) Math.PI / 4.0F));
        poses.scale(pulse, pulse, pulse);
        poses.pushPose();
        poses.scale(0.55F, 0.55F, 0.55F);
        draw(this.model.core, poses, collector, state, 0xF8FFFF, 1.0F, 0);
        poses.popPose();
        draw(this.model.core, poses, collector, state, state.primary, 0.7F, 0);
        poses.popPose();
        poses.pushPose();
        poses.translate(0, 18.5F / 16.0F, 1.0F / 16.0F);
        poses.mulPose(Axis.ZP.rotation(-age * 0.025F));
        draw(this.model.coreRing, poses, collector, state, state.secondary, 0.86F * pulse, 0);
        poses.popPose();

        ModelPart body = switch (state.form) {
            case HUMANOID -> this.model.torso;
            case BEAST -> this.model.beastTorso;
            case TREE -> this.model.treeTrunk;
        };
        draw(body, poses, collector, state, shell, 0.38F + breath * 0.025F, 1);
        if (state.form == RemnantForm.TREE) {
            draw(this.model.treeChannels, poses, collector, state, state.primary, 0.72F * pulse, 2);
        } else {
            poses.pushPose();
            if (state.form == RemnantForm.BEAST) poses.translate(0, 0, 0.35F / 16.0F);
            draw(this.model.channels, poses, collector, state, state.primary, 0.72F * pulse, 2);
            draw(this.model.secondaryChannels, poses, collector, state, state.secondary, 0.72F * pulse, 2);
            poses.popPose();
        }

        poses.pushPose();
        poses.translate(0, 23.0F / 16.0F, 0);
        poses.mulPose(Axis.XP.rotationDegrees(state.pitch * 0.45F));
        ModelPart head = switch (state.form) {
            case HUMANOID -> this.model.head;
            case BEAST -> this.model.beastHead;
            case TREE -> this.model.treeCrown;
        };
        ModelPart eyes = switch (state.form) {
            case HUMANOID -> this.model.eyes;
            case BEAST -> this.model.beastEyes;
            case TREE -> this.model.treeEyes;
        };
        draw(head, poses, collector, state, shell, 0.43F, 1);
        draw(eyes, poses, collector, state, tint(state.secondary, 0.45F), 0.96F, 2);
        poses.popPose();

        if (state.form != RemnantForm.TREE) {
            arm(poses, collector, state, true, shell);
            arm(poses, collector, state, false, shell);
        }
        for (int i = 0; i < this.model.ribbons.length; i++) {
            float sway = (float) Math.sin(age * 0.09F + i * 1.3F);
            poses.pushPose();
            poses.translate((i - 2) * 0.075F, 13.6F / 16.0F, 0);
            poses.mulPose(Axis.XP.rotation(sway * 0.13F + 0.07F));
            poses.mulPose(Axis.ZP.rotation((float) Math.sin(age * 0.065F + i) * 0.11F));
            draw(this.model.ribbons[i], poses, collector, state, i % 2 == 0 ? shell : tint(state.secondary, 0.65F),
                    0.25F + 0.025F * i, 1);
            poses.popPose();
        }
        for (int i = 0; i < 3; i++) {
            float phase = age * 0.035F + i * (float) Math.PI * 2.0F / 3.0F;
            poses.pushPose();
            poses.translate(Math.cos(phase) * 0.52F, 0.88F + Math.sin(phase * 1.5F) * 0.28F,
                    Math.sin(phase) * 0.34F);
            poses.mulPose(Axis.YP.rotation(phase * 2));
            poses.mulPose(Axis.ZP.rotation(phase));
            draw(this.model.mote, poses, collector, state, i == 1 ? state.secondary : state.primary, 0.76F, 2);
            poses.popPose();
        }
        poses.popPose();
        super.submit(state, poses, collector, camera);
    }

    private void arm(PoseStack poses, SubmitNodeCollector collector, State state, boolean left, int shell) {
        float side = left ? -1.0F : 1.0F;
        poses.pushPose();
        poses.translate(side * 5.25F / 16.0F, 21.0F / 16.0F, 0);
        poses.mulPose(Axis.ZP.rotation(side * (0.12F + (float) Math.sin(state.ageInTicks * 0.07F) * 0.055F)));
        poses.mulPose(Axis.XP.rotation(state.charging ? -1.05F : -0.08F));
        draw(left ? this.model.leftArm : this.model.rightArm, poses, collector, state, shell, 0.4F, 1);
        draw(this.model.armChannels, poses, collector, state, left ? state.primary : state.secondary, 0.78F, 2);
        poses.popPose();
    }

    private static void draw(ModelPart part, PoseStack poses, SubmitNodeCollector collector,
            State state, int rgb, float alpha, int order) {
        int color = (Mth.clamp(Math.round(alpha * state.fade * 255.0F), 0, 255) << 24) | rgb;
        collector.order(order).submitModelPart(part, poses, RenderTypes.entityTranslucentEmissive(MATERIAL),
                LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, null, color, null, state.outlineColor);
    }

    private static int tint(int rgb, float white) {
        int red = (rgb >> 16) & 255;
        int green = (rgb >> 8) & 255;
        int blue = rgb & 255;
        return ((red + Math.round((255 - red) * white)) << 16)
                | ((green + Math.round((255 - green) * white)) << 8)
                | (blue + Math.round((255 - blue) * white));
    }

    private static int color(MadraType.Element aspect) {
        return switch (aspect) {
            case FIRE -> 0xFF713C;
            case WATER -> 0x55BFFF;
            case AIR -> 0xB5F0E0;
            case EARTH -> 0xBA9D5A;
            case PURE -> 0xA0E9FF;
            case LIFE -> 0x70E58B;
            case DEATH -> 0x5EB2A3;
            case LIGHT -> 0xFFE8A0;
            case SHADOW -> 0x8D77BB;
            case BLOOD -> 0xD63D54;
            case DREAM -> 0xD991F1;
            case DESTRUCTION -> 0xE64440;
            case FORCE -> 0x91D4DC;
            case SWORD -> 0xD8E6EF;
            case HUNGER -> 0xB9BDCA;
        };
    }

    public static final class State extends EntityRenderState {
        private float yaw;
        private float pitch;
        private int primary = 0xA0E9FF;
        private int secondary = 0xDAF6FF;
        private RemnantForm form = RemnantForm.HUMANOID;
        private boolean charging;
        private float scale = 0.95F;
        private float fade = 1.0F;
    }
}
