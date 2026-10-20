package net.ok.project_cradle.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.dragon.EnderDragonModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EnderDragonRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/** Retains the vanilla Dragon, phase animation, crystal beam and death rendering. */
public final class SacredDragonRenderer extends EnderDragonRenderer {
    private static final Identifier SKIN = Identifier.withDefaultNamespace("textures/entity/enderdragon/dragon.png");
    private final EnderDragonModel tintModel;

    public SacredDragonRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.tintModel = new EnderDragonModel(context.bakeLayer(ModelLayers.ENDER_DRAGON));
    }

    @Override
    public void submit(EnderDragonRenderState state, PoseStack poses, SubmitNodeCollector collector,
            CameraRenderState camera) {
        super.submit(state, poses, collector, camera);
        var appearance = SacredBeastRenderer.appearance(state);
        if (appearance.sacred() && !state.isInvisible && state.deathTime <= 0.0F) {
            poses.pushPose();
            // Mirror the native model transform; the separate model shares the Dragon's phase state.
            poses.mulPose(Axis.YP.rotationDegrees(-state.getHistoricalPos(7).yRot()));
            float slope = (float) (state.getHistoricalPos(5).y() - state.getHistoricalPos(10).y());
            poses.mulPose(Axis.XP.rotationDegrees(slope * 10.0F));
            poses.translate(0.0F, 0.0F, 1.0F);
            poses.scale(-1.0F, -1.0F, 1.0F);
            poses.translate(0.0F, -1.501F, 0.0F);
            poses.scale(1.001F, 1.001F, 1.001F);
            collector.order(1).submitModel(this.tintModel, state, poses, RenderTypes.entityTranslucent(SKIN),
                    state.lightCoords, OverlayTexture.pack(0.0F, state.hasRedOverlay),
                    SacredBeastTintLayer.tint(appearance), null, state.outlineColor, null);
            poses.popPose();
        }
        SacredBeastRenderer.afterDragonBody(state, poses, collector);
    }
}
