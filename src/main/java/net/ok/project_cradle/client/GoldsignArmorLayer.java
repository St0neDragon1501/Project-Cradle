package net.ok.project_cradle.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.renderstate.AvatarRenderStateModifier;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.core.GoldsignStyle;

/** Physical Goldsigns attach to the animated player model independently of equipment. */
@EventBusSubscriber(modid = ProjectCradle.MODID, value = Dist.CLIENT)
public final class GoldsignArmorLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    private static final ContextKey<GoldsignStyle> GOLDSIGN = new ContextKey<>(
            Identifier.fromNamespaceAndPath(ProjectCradle.MODID, "goldsign_appearance"));
    static final ModelLayerLocation NORMAL = layer("normal");
    static final ModelLayerLocation SLIM = layer("slim");
    // A neutral vanilla material lets the geometry's individual metallic, jade, and spirit colors show.
    private static final Identifier MATERIAL = Identifier.withDefaultNamespace("textures/block/white_concrete.png");
    private final GoldsignModel goldsignModel;

    private static ModelLayerLocation layer(String skin) {
        return new ModelLayerLocation(Identifier.fromNamespaceAndPath(ProjectCradle.MODID, "goldsign"), skin);
    }

    private GoldsignArmorLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent,
            EntityModelSet models, boolean slim) {
        super(parent);
        this.goldsignModel = new GoldsignModel(models.bakeLayer(slim ? SLIM : NORMAL));
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(NORMAL, () -> GoldsignModel.createLayer(false));
        event.registerLayerDefinition(SLIM, () -> GoldsignModel.createLayer(true));
    }

    @SubscribeEvent
    public static void addPlayerLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerModelType skin : event.getSkins()) {
            AvatarRenderer<AbstractClientPlayer> renderer = event.getPlayerRenderer(skin);
            if (renderer != null) {
                renderer.addLayer(new GoldsignArmorLayer(renderer, event.getEntityModels(), skin == PlayerModelType.SLIM));
            }
        }
    }

    @SubscribeEvent
    public static void registerRenderStateModifier(RegisterRenderStateModifiersEvent event) {
        event.registerAvatarEntityModifier(new AvatarRenderStateModifier() {
            @Override
            public <T extends Avatar & ClientAvatarEntity> void accept(T avatar, AvatarRenderState state) {
                GoldsignStyle style = GoldsignStyle.AUTO;
                if (avatar instanceof Player player) {
                    var core = player.getData(ModAttachments.CORE);
                    if (!core.getGoldsign().isBlank()) style = core.getResolvedGoldsignStyle();
                }
                state.setRenderData(GOLDSIGN, style);
            }
        });
    }

    @Override
    public void submit(PoseStack poses, SubmitNodeCollector collector, int lightCoords,
            AvatarRenderState state, float yRot, float xRot) {
        GoldsignStyle style = state.getRenderData(GOLDSIGN);
        if (state.isInvisible || state.isSpectator || style == null || style == GoldsignStyle.AUTO) return;

        PlayerModel player = this.getParentModel();
        int overlay = LivingEntityRenderer.getOverlayCoords(state, 0.0F);
        for (GoldsignModel.Piece piece : this.goldsignModel.pieces(style)) {
            poses.pushPose();
            player.root().translateAndRotate(poses);
            ModelPart anchor = switch (piece.anchor()) {
                case HEAD -> player.head;
                case BODY -> player.body;
                case LEFT_ARM -> player.leftArm;
                case RIGHT_ARM -> player.rightArm;
            };
            anchor.translateAndRotate(poses);
            animate(poses, piece.motion(), state.ageInTicks);
            collector.order(1).submitModelPart(piece.part(), poses, RenderTypes.entityCutout(MATERIAL),
                    piece.emissive() ? LightCoordsUtil.FULL_BRIGHT : lightCoords,
                    overlay, null, piece.color(), null, state.outlineColor);
            poses.popPose();
        }
    }

    private static void animate(PoseStack poses, GoldsignModel.Motion motion, float age) {
        float breath = (float) Math.sin(age * 0.08F);
        switch (motion) {
            case LEFT_WING, RIGHT_WING -> {
                float side = motion == GoldsignModel.Motion.LEFT_WING ? 1.0F : -1.0F;
                // Pivot just behind the shoulder; only the cosmetic wing moves.
                poses.translate(side * 0.18F, 0.15F, 0.22F);
                poses.mulPose(Axis.YP.rotation(side * breath * 0.09F));
                poses.translate(-side * 0.18F, -0.15F, -0.22F);
            }
            case FLOATING -> {
                poses.translate(0.0F, breath * 0.035F, 0.0F);
                poses.mulPose(Axis.ZP.rotation((float) Math.sin(age * 0.045F) * 0.025F));
            }
            case FOX_TAIL -> {
                // All fur layers sway about the same lower-back root, in model units / 16.
                poses.translate(0.0F, 10.2F / 16.0F, 3.0F / 16.0F);
                poses.mulPose(Axis.YP.rotation((float) Math.sin(age * 0.065F) * 0.11F
                        + (float) Math.sin(age * 0.027F) * 0.045F));
                poses.mulPose(Axis.XP.rotation((float) Math.sin(age * 0.045F) * 0.025F));
                poses.translate(0.0F, -10.2F / 16.0F, -3.0F / 16.0F);
            }
            case NONE -> { }
        }
    }
}
