package net.ok.project_cradle.client;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.ok.project_cradle.capability.core.GoldsignStyle;

/** Small, immutable cosmetic meshes. Animated attachment transforms belong to the render layer. */
public final class GoldsignModel {
    public enum Anchor { HEAD, BODY, LEFT_ARM, RIGHT_ARM }
    public enum Motion { NONE, LEFT_WING, RIGHT_WING, FLOATING, FOX_TAIL }
    public record Piece(ModelPart part, Anchor anchor, int color, boolean emissive, Motion motion) { }

    private final Map<GoldsignStyle, List<Piece>> pieces = new EnumMap<>(GoldsignStyle.class);

    public GoldsignModel(ModelPart root) {
        add(root, GoldsignStyle.METAL_HAIR, "hair_shadow", Anchor.HEAD, 0xFF414C60, false, Motion.NONE);
        add(root, GoldsignStyle.METAL_HAIR, "hair_metal", Anchor.HEAD, 0xFFCDDCE6, false, Motion.NONE);
        add(root, GoldsignStyle.METAL_HAIR, "hair_glints", Anchor.HEAD, 0xFFF0FAFF, false, Motion.NONE);
        add(root, GoldsignStyle.METAL_BRACER, "bracer", Anchor.LEFT_ARM, 0xFF899DAE, false, Motion.NONE);
        add(root, GoldsignStyle.METAL_BRACER, "bracer_bands", Anchor.LEFT_ARM, 0xFFE7CE82, false, Motion.NONE);
        add(root, GoldsignStyle.METAL_BRACER, "bracer_gem", Anchor.LEFT_ARM, 0xFF78D6F2, true, Motion.NONE);
        for (String side : List.of("left", "right")) {
            Anchor anchor = side.equals("left") ? Anchor.LEFT_ARM : Anchor.RIGHT_ARM;
            add(root, GoldsignStyle.WEBBED_FINGERS, side + "_fingers", anchor, 0xFF5CABA2, false, Motion.NONE);
            add(root, GoldsignStyle.WEBBED_FINGERS, side + "_webbing", anchor, 0xFF9DDBC5, false, Motion.NONE);
            add(root, GoldsignStyle.WEBBED_FINGERS, side + "_finger_tips", anchor, 0xFFD6E8C0, false, Motion.NONE);
            Motion motion = side.equals("left") ? Motion.LEFT_WING : Motion.RIGHT_WING;
            add(root, GoldsignStyle.WINGS, side + "_wing_bones", Anchor.BODY, 0xFF2D6C63, false, motion);
            add(root, GoldsignStyle.WINGS, side + "_wing_feathers", Anchor.BODY, 0xFF55BBA0, false, motion);
            add(root, GoldsignStyle.WINGS, side + "_wing_edges", Anchor.BODY, 0xFFB6EFD2, false, motion);
        }
        add(root, GoldsignStyle.ARM_SERPENT, "serpent_scales", Anchor.LEFT_ARM, 0xFF498C61, false, Motion.NONE);
        add(root, GoldsignStyle.ARM_SERPENT, "serpent_belly", Anchor.LEFT_ARM, 0xFFCEBA70, false, Motion.NONE);
        add(root, GoldsignStyle.ARM_SERPENT, "serpent_eyes", Anchor.LEFT_ARM, 0xFFFFC14F, true, Motion.NONE);
        add(root, GoldsignStyle.ARM_SERPENT, "serpent_fangs", Anchor.LEFT_ARM, 0xFFF4EAD2, false, Motion.NONE);
        add(root, GoldsignStyle.BLACKFLAME_EYES, "eye_sclera", Anchor.HEAD, 0xFF100E17, false, Motion.NONE);
        add(root, GoldsignStyle.BLACKFLAME_EYES, "eye_iris", Anchor.HEAD, 0xFFFF2C36, true, Motion.NONE);
        add(root, GoldsignStyle.CLOUD_SWORDS, "cloud_blades", Anchor.BODY, 0xFFB8DCEB, false, Motion.FLOATING);
        add(root, GoldsignStyle.CLOUD_SWORDS, "cloud_blade_edges", Anchor.BODY, 0xFFF1FBFF, true, Motion.FLOATING);
        add(root, GoldsignStyle.CLOUD_SWORDS, "cloud_hilts", Anchor.BODY, 0xFF738CAE, false, Motion.FLOATING);
        add(root, GoldsignStyle.CLOUD_SWORDS, "clouds", Anchor.BODY, 0xFFDDE9F0, false, Motion.FLOATING);
        add(root, GoldsignStyle.FOX_TAIL, "fox_tail_shadow", Anchor.BODY, 0xFFD5BFB0, false, Motion.FOX_TAIL);
        add(root, GoldsignStyle.FOX_TAIL, "fox_tail_fur", Anchor.BODY, 0xFFF1E5D8, false, Motion.FOX_TAIL);
        add(root, GoldsignStyle.FOX_TAIL, "fox_tail_tip", Anchor.BODY, 0xFFFFFAF2, false, Motion.FOX_TAIL);
        add(root, GoldsignStyle.FOX_TAIL, "fox_tail_tufts", Anchor.BODY, 0xFFFCF1E5, false, Motion.FOX_TAIL);
        this.pieces.replaceAll((style, list) -> List.copyOf(list));
    }

    private void add(ModelPart root, GoldsignStyle style, String name, Anchor anchor,
            int color, boolean emissive, Motion motion) {
        this.pieces.computeIfAbsent(style, ignored -> new ArrayList<>())
                .add(new Piece(root.getChild(name), anchor, color, emissive, motion));
    }

    public List<Piece> pieces(GoldsignStyle style) {
        return this.pieces.getOrDefault(style, List.of());
    }

    public static LayerDefinition createLayer(boolean slim) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        hair(root);
        bracer(root, slim);
        hands(root, slim);
        serpent(root, slim);
        wings(root);
        eyes(root);
        cloudSwords(root);
        foxTail(root);
        return LayerDefinition.create(mesh, 16, 16);
    }

    private static CubeListBuilder cubes() { return CubeListBuilder.create().texOffs(0, 0); }

    private static PartDefinition part(PartDefinition root, String name, CubeListBuilder cubes) {
        return root.addOrReplaceChild(name, cubes, PartPose.ZERO);
    }

    private static void hair(PartDefinition root) {
        part(root, "hair_shadow", cubes()
                .addBox(-4.2F, -8.25F, -3.9F, 8.4F, 1.2F, 8.2F)
                .addBox(-4.15F, -7.2F, 2.9F, 8.3F, 7.4F, 1.3F));
        PartDefinition metal = part(root, "hair_metal", cubes());
        for (int i = 0; i < 7; i++) {
            float x = -3.8F + i * 1.13F;
            metal.addOrReplaceChild("crown_" + i, cubes().addBox(0, -2.2F, -0.5F, 0.8F, 3.3F, 7.7F),
                    PartPose.offsetAndRotation(x, -8.0F, -3.7F, -0.06F, 0, (i - 3) * 0.08F));
        }
        for (int side : new int[] {-1, 1}) {
            for (int lock = 0; lock < 3; lock++) {
                metal.addOrReplaceChild("lock_" + side + "_" + lock,
                        cubes().addBox(-0.45F, 0, -0.5F, 0.9F, 7.5F + lock, 1.2F),
                        PartPose.offsetAndRotation(side * 4.3F, -6.1F, -1.2F + lock * 1.9F,
                                0.08F, 0, -side * 0.08F));
            }
        }
        part(root, "hair_glints", cubes()
                .addBox(-3.9F, -8.5F, -3.55F, 1.0F, 0.3F, 6.5F)
                .addBox(-0.5F, -8.6F, -3.55F, 0.7F, 0.3F, 7.0F)
                .addBox(2.6F, -8.5F, -3.55F, 0.7F, 0.3F, 6.5F));
    }

    private static void bracer(PartDefinition root, boolean slim) {
        float width = slim ? 3 : 4;
        float x = -1.65F;
        // Four separate faces leave the forearm inside the cuff rather than replacing the arm.
        part(root, "bracer", cubes()
                .addBox(x, 5.0F, -2.65F, width + 1.3F, 4.6F, 0.55F)
                .addBox(x, 5.0F, 2.1F, width + 1.3F, 4.6F, 0.55F)
                .addBox(x, 5.0F, -2.1F, 0.55F, 4.6F, 4.2F)
                .addBox(width - 0.9F, 5.0F, -2.1F, 0.55F, 4.6F, 4.2F));
        CubeListBuilder bands = cubes();
        for (float y : new float[] {4.7F, 9.2F}) {
            bands.addBox(x - 0.1F, y, -2.85F, width + 1.5F, 0.7F, 0.65F)
                    .addBox(x - 0.1F, y, 2.2F, width + 1.5F, 0.7F, 0.65F)
                    .addBox(x - 0.1F, y, -2.2F, 0.65F, 0.7F, 4.4F)
                    .addBox(width - 0.9F, y, -2.2F, 0.65F, 0.7F, 4.4F);
        }
        part(root, "bracer_bands", bands);
        PartDefinition gem = part(root, "bracer_gem", cubes());
        gem.addOrReplaceChild("stone", cubes().addBox(-0.7F, -0.7F, -0.3F, 1.4F, 1.4F, 0.6F),
                PartPose.offsetAndRotation(width / 2 - 1, 7.3F, -2.9F, 0, 0, 0.7854F));
    }

    private static void hands(PartDefinition root, boolean slim) {
        for (String side : List.of("left", "right")) {
            float x = side.equals("left") ? -1.35F : slim ? -2.35F : -3.35F;
            float width = slim ? 3.7F : 4.7F;
            CubeListBuilder fingers = cubes().addBox(x, 8.1F, -2.35F, width, 2.2F, 0.6F);
            CubeListBuilder web = cubes();
            CubeListBuilder tips = cubes();
            for (int i = 0; i < 4; i++) {
                float fingerX = x + i * (width - 0.8F) / 3;
                float length = i == 1 || i == 2 ? 3.3F : 2.6F;
                fingers.addBox(fingerX, 9.5F, -2.4F, 0.7F, length, 0.85F);
                tips.addBox(fingerX, 9.1F + length, -2.45F, 0.7F, 0.55F, 0.95F);
                if (i < 3) web.addBox(fingerX + 0.45F, 9.6F, -2.2F,
                        (width - 0.8F) / 3 - 0.2F, 2.1F, 0.25F);
            }
            part(root, side + "_fingers", fingers);
            part(root, side + "_webbing", web);
            part(root, side + "_finger_tips", tips);
        }
    }

    private static void serpent(PartDefinition root, boolean slim) {
        float centerX = slim ? 0.5F : 1.0F;
        float radiusX = slim ? 2.35F : 2.85F;
        CubeListBuilder scales = cubes();
        CubeListBuilder belly = cubes();
        for (int i = 0; i < 29; i++) {
            double angle = i * Math.PI / 7;
            float x = centerX + (float) Math.cos(angle) * radiusX;
            float z = (float) Math.sin(angle) * 2.8F;
            float y = 1.3F + i * 0.26F;
            scales.addBox(x - 0.65F, y, z - 0.65F, 1.3F, 1.0F, 1.3F);
            belly.addBox(x - 0.4F, y + 0.7F, z - 0.4F, 0.8F, 0.3F, 0.8F);
        }
        scales.addBox(centerX - 1.3F, 0.2F, -3.9F, 2.6F, 1.5F, 2.8F)
                .addBox(centerX - 0.9F, 1.4F, -4.35F, 1.8F, 0.5F, 2.0F);
        part(root, "serpent_scales", scales);
        part(root, "serpent_belly", belly);
        part(root, "serpent_eyes", cubes()
                .addBox(centerX - 1.35F, 0.6F, -3.8F, 0.25F, 0.45F, 0.65F)
                .addBox(centerX + 1.1F, 0.6F, -3.8F, 0.25F, 0.45F, 0.65F));
        part(root, "serpent_fangs", cubes()
                .addBox(centerX - 0.65F, 1.7F, -4.1F, 0.3F, 0.7F, 0.3F)
                .addBox(centerX + 0.35F, 1.7F, -4.1F, 0.3F, 0.7F, 0.3F));
    }

    private static void wings(PartDefinition root) {
        for (String side : List.of("left", "right")) {
            int sign = side.equals("left") ? 1 : -1;
            PartDefinition bones = part(root, side + "_wing_bones", cubes());
            PartDefinition feathers = part(root, side + "_wing_feathers", cubes());
            PartDefinition edges = part(root, side + "_wing_edges", cubes());
            bones.addOrReplaceChild("shoulder", cubes().addBox(sign > 0 ? -0.7F : -8.5F, -0.5F, -0.7F, 9.2F, 1.4F, 1.4F),
                    PartPose.offsetAndRotation(sign * 2.5F, 2.5F, 3.6F, 0, sign * -0.3F, sign * -0.45F));
            for (int i = 0; i < 7; i++) {
                float x = sign * (5.2F + i * 1.7F);
                float y = 1.8F + i * 0.25F;
                float length = 11.5F - i * 0.75F;
                float angle = sign * (0.24F + i * 0.045F);
                feathers.addOrReplaceChild("feather_" + i,
                        cubes().addBox(-1.25F, 0, -0.45F, 2.5F, length, 0.9F),
                        PartPose.offsetAndRotation(x, y, 4.0F + i * 0.12F, 0.1F, 0, angle));
                edges.addOrReplaceChild("edge_" + i,
                        cubes().addBox(-0.6F, length - 2.4F, -0.53F, 1.2F, 2.5F, 1.06F),
                        PartPose.offsetAndRotation(x, y, 4.0F + i * 0.12F, 0.1F, 0, angle));
            }
        }
    }

    private static void eyes(PartDefinition root) {
        part(root, "eye_sclera", cubes()
                .addBox(-3.1F, -4.3F, -4.04F, 2.2F, 1.25F, 0.10F)
                .addBox(0.9F, -4.3F, -4.04F, 2.2F, 1.25F, 0.10F));
        part(root, "eye_iris", cubes()
                .addBox(-2.25F, -4.15F, -4.12F, 0.65F, 0.95F, 0.10F)
                .addBox(1.6F, -4.15F, -4.12F, 0.65F, 0.95F, 0.10F));
    }

    private static void foxTail(PartDefinition root) {
        PartDefinition shadow = part(root, "fox_tail_shadow", cubes());
        PartDefinition fur = part(root, "fox_tail_fur", cubes());
        PartDefinition tip = part(root, "fox_tail_tip", cubes());
        PartDefinition tufts = part(root, "fox_tail_tufts", cubes());
        // Body-local coordinates: +Z is behind the artist and -Y curls the tail upward.
        // The narrow root clears a chestplate; overlapping sections widen into a brush,
        // then taper into a pale tip without touching the player's leg animation.
        float[][] sections = {
                {10.2F, 3.0F, 2.6F, 2.4F, 4.0F, 0.18F},
                {9.5F, 6.2F, 4.3F, 4.1F, 4.7F, 0.25F},
                {8.0F, 9.5F, 6.1F, 5.5F, 4.8F, 0.42F},
                {6.0F, 12.7F, 5.7F, 5.2F, 4.3F, 0.62F},
                {4.2F, 15.2F, 4.3F, 4.2F, 3.3F, 0.72F},
                {2.6F, 17.0F, 2.6F, 2.8F, 2.5F, 0.76F}
        };
        for (int i = 0; i < sections.length; i++) {
            float[] section = sections[i];
            float width = section[2];
            float height = section[3];
            float length = section[4];
            PartPose pose = PartPose.offsetAndRotation(0, section[0], section[1], section[5], 0, 0);
            PartDefinition outer = i >= 4 ? tip : shadow;
            outer.addOrReplaceChild("section_" + i, cubes()
                    .addBox(-width / 2, -height / 2, 0, width, height, length), pose);
            if (i < 4) {
                fur.addOrReplaceChild("section_" + i, cubes()
                        .addBox(-width / 2 - 0.04F, -height / 2 - 0.05F, -0.02F,
                                width + 0.08F, height * 0.76F, length + 0.04F), pose);
            }
            if (i > 0 && i < 5) {
                for (int side : new int[] {-1, 1}) {
                    PartDefinition tuft = tufts.addOrReplaceChild("tuft_" + i + "_" + side, cubes(), pose);
                    tuft.addOrReplaceChild("outer", cubes()
                            .addBox(-0.55F, -0.65F, -0.35F, 1.1F, 1.3F, 2.4F),
                            PartPose.offsetAndRotation(side * (width / 2 - 0.3F), -height * 0.13F,
                                    length * 0.45F, 0.0F, side * 0.3F, side * -0.16F));
                }
            }
        }
    }

    private static void cloudSwords(PartDefinition root) {
        PartDefinition blades = part(root, "cloud_blades", cubes());
        PartDefinition edges = part(root, "cloud_blade_edges", cubes());
        PartDefinition hilts = part(root, "cloud_hilts", cubes());
        PartDefinition clouds = part(root, "clouds", cubes());
        for (int i = 0; i < 3; i++) {
            float x = (i - 1) * 6.0F;
            float y = i == 1 ? -7.0F : -3.8F;
            float z = i == 1 ? 7.8F : 6.5F;
            float tilt = (i - 1) * 0.4F;
            PartPose pose = PartPose.offsetAndRotation(x, y, z, -0.1F, 0, tilt);
            blades.addOrReplaceChild("blade_" + i, cubes()
                    .addBox(-0.9F, -11.0F, -0.4F, 1.8F, 11.0F, 0.8F)
                    .addBox(-0.5F, -13.0F, -0.3F, 1.0F, 2.0F, 0.6F), pose);
            edges.addOrReplaceChild("edge_" + i, cubes()
                    .addBox(-1.05F, -10.8F, -0.48F, 0.25F, 10.8F, 0.96F)
                    .addBox(0.8F, -10.8F, -0.48F, 0.25F, 10.8F, 0.96F)
                    .addBox(-0.2F, -13.6F, -0.25F, 0.4F, 0.8F, 0.5F), pose);
            hilts.addOrReplaceChild("hilt_" + i, cubes()
                    .addBox(-2.4F, 0, -0.65F, 4.8F, 0.9F, 1.3F)
                    .addBox(-0.45F, 0.8F, -0.45F, 0.9F, 3.6F, 0.9F)
                    .addBox(-0.8F, 4.0F, -0.7F, 1.6F, 0.9F, 1.4F), pose);
            clouds.addOrReplaceChild("cloud_" + i, cubes()
                    .addBox(-3.4F, 1.9F, -1.4F, 6.8F, 2.0F, 2.8F)
                    .addBox(-2.1F, 0.7F, -1.5F, 3.7F, 1.9F, 3.0F)
                    .addBox(1.0F, 1.2F, -1.2F, 3.0F, 2.0F, 2.4F), pose);
        }
    }
}
