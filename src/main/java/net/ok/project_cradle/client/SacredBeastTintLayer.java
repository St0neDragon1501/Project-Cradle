package net.ok.project_cradle.client;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.capability.SacredBeastAppearance;

/** A translucent madra wash follows each vanilla body and its existing animation. */
@EventBusSubscriber(modid = ProjectCradle.MODID, value = Dist.CLIENT)
public final class SacredBeastTintLayer<S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {
    private final LivingEntityRenderer<?, S, M> renderer;

    private SacredBeastTintLayer(LivingEntityRenderer<?, S, M> renderer) {
        super(renderer);
        this.renderer = renderer;
    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        Set<LivingEntityRenderer<?, ?, ?>> added = Collections.newSetFromMap(new IdentityHashMap<>());
        for (var type : event.getEntityTypes()) {
            var id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            if (id == null || !id.getNamespace().equals("minecraft")) continue;
            if (event.getRenderer(type) instanceof LivingEntityRenderer<?, ?, ?> living && added.add(living)) add(living);
        }
    }

    /** The layer and renderer share their model/state pair; registry wildcards erase that relation. */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void add(LivingEntityRenderer<?, ?, ?> renderer) {
        ((LivingEntityRenderer) renderer).addLayer(new SacredBeastTintLayer((LivingEntityRenderer) renderer));
    }

    @Override
    public void submit(PoseStack poses, SubmitNodeCollector collector, int lightCoords,
            S state, float yRot, float xRot) {
        SacredBeastAppearance appearance = SacredBeastRenderer.appearance(state);
        if (!appearance.sacred() || state.isInvisible || state.deathTime >= 20.0F) return;
        poses.pushPose();
        poses.scale(1.001F, 1.001F, 1.001F);
        collector.order(1).submitModel(this.getParentModel(), state, poses,
                RenderTypes.entityTranslucent(this.renderer.getTextureLocation(state)), lightCoords,
                LivingEntityRenderer.getOverlayCoords(state, 0.0F), tint(appearance), null, state.outlineColor, null);
        poses.popPose();
    }

    static int tint(SacredBeastAppearance appearance) {
        int color = switch (appearance.primaryElement()) {
            case FIRE -> 0xFF713C;
            case WATER -> 0x49BDE5;
            case AIR -> 0xA7EBD5;
            case EARTH -> 0xB49A55;
            case PURE -> 0xACEAF5;
            case LIFE -> 0x62CE73;
            case DEATH -> 0x6DAA9A;
            case LIGHT -> 0xFFE59D;
            case SHADOW -> 0x8062A8;
            case BLOOD -> 0xC93452;
            case DREAM -> 0xD596F4;
            case DESTRUCTION -> 0xED4741;
            case FORCE -> 0x81C3CD;
            case SWORD -> 0xCDDDE7;
            case HUNGER -> 0xB5B9C5;
        };
        return 0x40000000 | color;
    }
}
