package net.ok.project_cradle.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.ok.project_cradle.capability.SacredBeastAppearance;
import net.ok.project_cradle.capability.madra.MadraType.Element;

/** Solid, colored growths fitted to a creature's silhouette without replacing its vanilla model. */
public final class SacredBeastModel {
    public enum Shape { HORNED, AVIAN, AQUATIC, ARTHROPOD, HUMANOID, AMORPHOUS, DRAGON }

    private static final Identifier MATERIAL = Identifier.withDefaultNamespace("textures/block/white_concrete.png");
    private final ModelPart horn;
    private final ModelPart branch;
    private final ModelPart crystal;
    private final ModelPart plate;
    private final ModelPart fin;
    private final ModelPart feather;
    private final ModelPart band;
    private final ModelPart jewel;

    public SacredBeastModel() {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        root.addOrReplaceChild("horn", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-1.8F, 0, -1.8F, 3.6F, 4, 3.6F)
                .addBox(-1.25F, 4, -1.25F, 2.5F, 4, 2.5F)
                .addBox(-0.75F, 8, -0.75F, 1.5F, 4, 1.5F)
                .addBox(-0.35F, 12, -0.35F, 0.7F, 2, 0.7F), PartPose.ZERO);
        root.addOrReplaceChild("branch", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-0.8F, 0, -0.8F, 1.6F, 5, 1.6F)
                .addBox(-0.4F, 5, -0.4F, 0.8F, 3, 0.8F), PartPose.ZERO);
        root.addOrReplaceChild("crystal", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-2, -2, -2, 4, 4, 4)
                .addBox(-1.4F, 2, -1.4F, 2.8F, 2, 2.8F)
                .addBox(-0.7F, 4, -0.7F, 1.4F, 2, 1.4F)
                .addBox(-1.2F, -4, -1.2F, 2.4F, 2, 2.4F), PartPose.ZERO);
        root.addOrReplaceChild("plate", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4, -1, -3, 8, 2, 6)
                .addBox(-3, 1, -2, 6, 1, 4)
                .addBox(-2, 2, -1, 4, 1, 2), PartPose.ZERO);
        root.addOrReplaceChild("fin", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-0.6F, 0, -5, 1.2F, 3, 10)
                .addBox(-0.5F, 3, -4, 1, 3, 7)
                .addBox(-0.35F, 6, -2, 0.7F, 3, 3)
                .addBox(-0.2F, 9, -1, 0.4F, 2, 1), PartPose.ZERO);
        root.addOrReplaceChild("feather", CubeListBuilder.create().texOffs(0, 0)
                .addBox(0, -0.6F, -1.5F, 4, 1.2F, 3)
                .addBox(4, -0.5F, -1.25F, 4, 1, 2.5F)
                .addBox(8, -0.35F, -0.9F, 4, 0.7F, 1.8F)
                .addBox(12, -0.2F, -0.4F, 3, 0.4F, 0.8F), PartPose.ZERO);
        root.addOrReplaceChild("band", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4, -0.5F, -3, 8, 1, 0.8F)
                .addBox(-4, -0.5F, 2.2F, 8, 1, 0.8F)
                .addBox(-4, -0.5F, -2.2F, 0.8F, 1, 4.4F)
                .addBox(3.2F, -0.5F, -2.2F, 0.8F, 1, 4.4F), PartPose.ZERO);
        root.addOrReplaceChild("jewel", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-1.1F, -1.1F, -1.1F, 2.2F, 2.2F, 2.2F), PartPose.ZERO);
        ModelPart baked = LayerDefinition.create(mesh, 16, 16).bakeRoot();
        this.horn = baked.getChild("horn");
        this.branch = baked.getChild("branch");
        this.crystal = baked.getChild("crystal");
        this.plate = baked.getChild("plate");
        this.fin = baked.getChild("fin");
        this.feather = baked.getChild("feather");
        this.band = baked.getChild("band");
        this.jewel = baked.getChild("jewel");
    }

    /** Origin at the creature's feet, positive Y up, facing negative Z; dimensions include physical scale. */
    public void submit(PoseStack poses, SubmitNodeCollector collector, SacredBeastAppearance appearance,
            Shape shape, float width, float height, float age, int light, int outline) {
        float w = Mth.clamp(width, 0.2F, shape == Shape.DRAGON ? 16.0F : 12.0F);
        float h = Mth.clamp(height, 0.2F, shape == Shape.DRAGON ? 10.0F : 24.0F);
        float growth = 0.88F + appearance.variant() * 0.08F;
        int primary = color(appearance.primaryElement());
        int secondary = appearance.hasSecondaryAspect() ? color(appearance.secondaryElement()) : mix(primary, 0xFFF0C5, 0.6F);
        int shell = mix(primary, 0x252B39, 0.45F);
        int edge = mix(secondary, 0xFFFFFF, 0.4F);
        Context c = new Context(poses, collector, light, outline, primary, secondary, shell, edge, growth);
        switch (shape) {
            case HORNED -> horned(c, w, h, appearance.primaryElement());
            case AVIAN -> avian(c, w, h);
            case AQUATIC -> aquatic(c, w, h);
            case ARTHROPOD -> arthropod(c, w, h);
            case HUMANOID -> humanoid(c, w, h);
            case AMORPHOUS -> amorphous(c, w, h, age);
            case DRAGON -> dragon(c, w, h);
        }
    }

    private void horned(Context c, float w, float h, Element aspect) {
        float length = Math.min(h * 0.38F, w * 0.85F) * c.growth;
        for (int side : new int[] {-1, 1}) {
            draw(c, this.horn, side * w * 0.24F, h * 0.84F, -w * 0.40F,
                    length, length, length, -0.22F, 0, side * -0.28F, c.edge, false);
            if (aspect == Element.LIFE || aspect == Element.DREAM || aspect == Element.EARTH) {
                draw(c, this.branch, side * (w * 0.24F + length * 0.13F), h * 0.84F + length * 0.4F, -w * 0.44F,
                        length, length, length, 0.15F, side * 0.4F, side * -0.95F, c.secondary, false);
            }
        }
        for (int i = 0; i < 3; i++) {
            float z = (i - 1) * w * 0.3F;
            draw(c, this.plate, 0, h * 0.79F, z, w * 0.9F, h * 0.35F, w * 0.75F,
                    0.07F * i, 0, 0, c.shell, false);
            draw(c, this.crystal, 0, h * 0.83F, z, w * 0.32F, h * 0.35F * c.growth, w * 0.32F,
                    0.2F, 0.35F, 0, i == 1 ? c.secondary : c.primary, false);
        }
        gem(c, 0, h * 0.60F, -w * 0.48F, w * 0.8F, c.secondary);
    }

    private void avian(Context c, float w, float h) {
        draw(c, this.fin, 0, h * 0.85F, -w * 0.23F, w * 0.8F, h * 0.50F * c.growth, w * 0.7F,
                -0.25F, 0, 0, c.primary, false);
        for (int side : new int[] {-1, 1}) {
            for (int i = 0; i < 3; i++) {
                draw(c, this.feather, side * w * 0.30F, h * (0.56F - i * 0.045F), w * (i - 1) * 0.18F,
                        w * (0.45F - i * 0.06F), h * 0.4F, w * 0.7F,
                        0, side * (0.08F + i * 0.23F), side == 1 ? -0.18F : (float) Math.PI + 0.18F,
                        i == 0 ? c.edge : c.secondary, false);
            }
        }
        gem(c, 0, h * 0.54F, -w * 0.48F, w, c.primary);
    }

    private void aquatic(Context c, float w, float h) {
        draw(c, this.fin, 0, h * 0.75F, w * 0.05F, w, h * 0.50F * c.growth, w * 1.3F,
                -0.2F, 0, 0, c.primary, false);
        for (int side : new int[] {-1, 1}) {
            draw(c, this.fin, side * w * 0.32F, h * 0.45F, w * 0.1F, w * 0.8F, w * 0.65F, w,
                    0.25F, 0, side * -1.1F, c.edge, false);
            for (int i = 0; i < 3; i++) {
                draw(c, this.plate, side * w * 0.34F, h * 0.55F, (i - 1) * w * 0.3F,
                        w * 0.5F, h * 0.25F, w * 0.6F, 0, 0, side * 0.9F, c.shell, false);
            }
        }
        gem(c, 0, h * 0.81F, w * 0.05F, w * 0.7F, c.secondary);
    }

    private void arthropod(Context c, float w, float h) {
        for (int i = 0; i < 3; i++) {
            float z = (i - 1) * w * 0.25F;
            draw(c, this.plate, 0, h * 0.75F, z, w * 1.15F, h * 0.55F, w * 0.7F,
                    0, 0, 0, i % 2 == 0 ? c.shell : c.secondary, false);
            for (int side : new int[] {-1, 1}) {
                draw(c, this.horn, side * w * 0.37F, h * 0.58F, z,
                        w * 0.2F, w * 0.33F * c.growth, w * 0.2F, 0, 0, side * -0.95F, c.edge, false);
            }
        }
        for (int side : new int[] {-1, 1}) {
            gem(c, side * w * 0.14F, h * 0.63F, -w * 0.46F, w * 0.6F, c.primary);
        }
    }

    private void humanoid(Context c, float w, float h) {
        draw(c, this.band, 0, h * 0.94F, 0, w * 1.22F, h * 0.6F, w * 1.22F, 0, 0, 0, c.edge, false);
        for (int i = -1; i <= 1; i++) {
            draw(c, this.horn, i * w * 0.25F, h * 0.94F, -w * 0.32F,
                    w * 0.4F, h * (i == 0 ? 0.21F : 0.14F) * c.growth, w * 0.4F,
                    0.1F, 0, i * -0.3F, c.secondary, false);
        }
        for (int side : new int[] {-1, 1}) {
            draw(c, this.plate, side * w * 0.46F, h * 0.76F, 0,
                    w * 0.65F, h * 0.4F, w * 0.8F, 0, 0, side * -0.3F, c.shell, false);
            draw(c, this.crystal, side * w * 0.46F, h * 0.80F, 0,
                    w * 0.38F, h * (side == 1 ? 0.3F : 0.2F) * c.growth, w * 0.38F,
                    0.1F, 0.5F, side * -0.3F, c.primary, false);
        }
        gem(c, 0, h * 0.67F, -w * 0.42F, w * 0.9F, c.secondary);
    }

    private void amorphous(Context c, float w, float h, float age) {
        float pulse = 1 + (float) Math.sin(age * 0.075F) * 0.035F;
        draw(c, this.crystal, 0, h * 0.55F, 0, w * 1.1F * pulse, h * 1.1F * pulse, w * 1.1F * pulse,
                0.15F, age * 0.015F, 0.35F, c.secondary, true);
        for (int side : new int[] {-1, 1}) {
            draw(c, this.crystal, side * w * 0.37F, h * 0.84F, -w * 0.25F,
                    w * 0.65F, h * 0.75F * c.growth, w * 0.65F, -0.1F, side * 0.35F, side * -0.35F, c.primary, false);
            draw(c, this.plate, side * w * 0.44F, h * 0.45F, w * 0.05F,
                    w * 0.65F, h * 0.5F, w * 1.1F, 0, 0, side * 1.2F, c.shell, false);
        }
        gem(c, 0, h * 0.58F, -w * 0.51F, w * 1.7F, c.primary);
    }

    private void dragon(Context c, float w, float h) {
        // The Dragon's entity origin is at the center of its body, rather than at a walking mob's feet.
        float spine = Math.min(w * 0.075F, 1.15F);
        for (int i = 0; i < 7; i++) {
            float z = (i - 2) * w * 0.09F;
            draw(c, this.plate, 0, h * 0.15F, z, spine * 1.7F, spine, spine * 1.7F,
                    0, 0, 0, c.shell, false);
            draw(c, this.horn, 0, h * 0.17F, z, spine, spine * (1.0F - i * 0.07F) * c.growth, spine,
                    0.4F, 0, 0, i % 2 == 0 ? c.primary : c.edge, false);
        }
        for (int side : new int[] {-1, 1}) {
            draw(c, this.horn, side * w * 0.05F, h * 0.19F, -w * 0.33F,
                    spine, spine * 1.5F, spine, -0.3F, 0, side * -0.4F, c.secondary, false);
            draw(c, this.crystal, side * w * 0.28F, h * 0.08F, w * 0.10F,
                    spine * 1.4F, spine * 1.6F, spine * 1.4F, 0, side * 0.35F, side * -0.5F, c.primary, false);
        }
    }

    private void gem(Context c, float x, float y, float z, float scale, int color) {
        draw(c, this.jewel, x, y, z, scale, scale, scale, 0, 0, (float) Math.PI / 4, color, true);
    }

    private void draw(Context c, ModelPart part, float x, float y, float z, float sx, float sy, float sz,
            float rx, float ry, float rz, int rgb, boolean emissive) {
        c.poses.pushPose();
        c.poses.translate(x, y, z);
        c.poses.mulPose(Axis.YP.rotation(ry));
        c.poses.mulPose(Axis.XP.rotation(rx));
        c.poses.mulPose(Axis.ZP.rotation(rz));
        c.poses.scale(sx, sy, sz);
        c.collector.order(emissive ? 2 : 1).submitModelPart(part, c.poses,
                RenderTypes.entityCutout(MATERIAL), emissive ? LightCoordsUtil.FULL_BRIGHT : c.light,
                OverlayTexture.NO_OVERLAY, null, 0xFF000000 | rgb, null, c.outline);
        c.poses.popPose();
    }

    private static int mix(int a, int b, float amount) {
        int red = Math.round(((a >> 16) & 255) * (1 - amount) + ((b >> 16) & 255) * amount);
        int green = Math.round(((a >> 8) & 255) * (1 - amount) + ((b >> 8) & 255) * amount);
        int blue = Math.round((a & 255) * (1 - amount) + (b & 255) * amount);
        return red << 16 | green << 8 | blue;
    }

    private static int color(Element aspect) {
        return switch (aspect) {
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
    }

    private record Context(PoseStack poses, SubmitNodeCollector collector, int light, int outline,
            int primary, int secondary, int shell, int edge, float growth) { }
}
