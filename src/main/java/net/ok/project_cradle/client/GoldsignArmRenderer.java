package net.ok.project_cradle.client;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.capability.ModAttachments;
import org.joml.Quaternionf;

/** Adds hand Goldsigns to the ordinary first-person arm without cancelling its rendering. */
@EventBusSubscriber(modid = ProjectCradle.MODID, value = Dist.CLIENT)
public final class GoldsignArmRenderer {
    private static GoldsignModel normal;
    private static GoldsignModel slim;
    private static final Identifier MATERIAL = Identifier.withDefaultNamespace("textures/block/white_concrete.png");

    private GoldsignArmRenderer() { }

    @SubscribeEvent
    public static void onModelsReloaded(EntityRenderersEvent.AddLayers event) {
        normal = new GoldsignModel(event.getEntityModels().bakeLayer(GoldsignArmorLayer.NORMAL));
        slim = new GoldsignModel(event.getEntityModels().bakeLayer(GoldsignArmorLayer.SLIM));
    }

    @SubscribeEvent
    public static void onRenderArm(RenderArmEvent<?> event) {
        if (!(event.getAvatar() instanceof Player player) || player.isInvisible() || player.isSpectator()) return;
        var core = player.getData(ModAttachments.CORE);
        if (core.getGoldsign().isBlank()) return;
        GoldsignModel model = event.getAvatar().getSkin().model() == PlayerModelType.SLIM ? slim : normal;
        if (model == null) return;
        GoldsignModel.Anchor arm = event.getArm() == HumanoidArm.LEFT
                ? GoldsignModel.Anchor.LEFT_ARM : GoldsignModel.Anchor.RIGHT_ARM;
        var poses = event.getPoseStack();
        var initial = event.getArmPart().getInitialPose();
        poses.pushPose();
        // Match AvatarRenderer.renderHand's reset pose and gentle roll without mutating its shared arm.
        poses.translate(initial.x() / 16.0F, initial.y() / 16.0F, initial.z() / 16.0F);
        poses.mulPose(new Quaternionf().rotationZYX(event.getArm() == HumanoidArm.LEFT ? -0.1F : 0.1F,
                initial.yRot(), initial.xRot()));
        poses.scale(initial.xScale(), initial.yScale(), initial.zScale());
        for (GoldsignModel.Piece piece : model.pieces(core.getResolvedGoldsignStyle())) {
            if (piece.anchor() != arm) continue;
            event.getSubmitNodeCollector().order(1).submitModelPart(piece.part(), poses,
                    RenderTypes.entityCutout(MATERIAL), piece.emissive() ? LightCoordsUtil.FULL_BRIGHT : event.getLightCoords(),
                    OverlayTexture.NO_OVERLAY, null, piece.color(), null);
        }
        poses.popPose();
    }
}
