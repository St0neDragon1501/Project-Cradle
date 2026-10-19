package net.ok.project_cradle.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** Original spirit meshes. All animation is supplied as pose transforms by RemnantRenderer. */
final class RemnantModel {
    final ModelPart torso;
    final ModelPart beastTorso;
    final ModelPart treeTrunk;
    final ModelPart head;
    final ModelPart beastHead;
    final ModelPart treeCrown;
    final ModelPart eyes;
    final ModelPart beastEyes;
    final ModelPart treeEyes;
    final ModelPart leftArm;
    final ModelPart rightArm;
    final ModelPart armChannels;
    final ModelPart channels;
    final ModelPart secondaryChannels;
    final ModelPart treeChannels;
    final ModelPart core;
    final ModelPart coreRing;
    final ModelPart mote;
    final ModelPart[] ribbons = new ModelPart[5];

    RemnantModel() {
        ModelPart root = createGeometry().bakeRoot();
        this.torso = root.getChild("torso");
        this.beastTorso = root.getChild("beast_torso");
        this.treeTrunk = root.getChild("tree_trunk");
        this.head = root.getChild("head");
        this.beastHead = root.getChild("beast_head");
        this.treeCrown = root.getChild("tree_crown");
        this.eyes = root.getChild("eyes");
        this.beastEyes = root.getChild("beast_eyes");
        this.treeEyes = root.getChild("tree_eyes");
        this.leftArm = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");
        this.armChannels = root.getChild("arm_channels");
        this.channels = root.getChild("channels");
        this.secondaryChannels = root.getChild("secondary_channels");
        this.treeChannels = root.getChild("tree_channels");
        this.core = root.getChild("core");
        this.coreRing = root.getChild("core_ring");
        this.mote = root.getChild("mote");
        for (int i = 0; i < this.ribbons.length; i++) this.ribbons[i] = root.getChild("ribbon_" + i);
    }

    private static CubeListBuilder cubes() { return CubeListBuilder.create().texOffs(0, 0); }

    private static PartDefinition part(PartDefinition root, String name, CubeListBuilder cubes) {
        return root.addOrReplaceChild(name, cubes, PartPose.ZERO);
    }

    private static LayerDefinition createGeometry() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        part(root, "torso", cubes()
                .addBox(-4.0F, 14.0F, -2.15F, 8.0F, 7.5F, 4.3F)
                .addBox(-3.1F, 11.9F, -1.65F, 6.2F, 3.2F, 3.3F)
                .addBox(-4.7F, 20.2F, -1.7F, 9.4F, 1.9F, 3.4F)
                .addBox(-1.0F, 21.7F, -1.0F, 2.0F, 1.6F, 2.0F));
        part(root, "beast_torso", cubes()
                .addBox(-4.5F, 15.0F, -2.4F, 9.0F, 6.5F, 4.8F)
                .addBox(-3.0F, 11.7F, -1.6F, 6.0F, 4.1F, 3.2F)
                .addBox(-5.4F, 20.0F, -1.8F, 10.8F, 2.0F, 3.6F));
        PartDefinition trunk = part(root, "tree_trunk", cubes()
                .addBox(-2.8F, 11.4F, -2.0F, 5.6F, 12.0F, 4.0F)
                .addBox(-2.1F, 22.2F, -1.6F, 4.2F, 3.3F, 3.2F));
        for (int side : new int[] {-1, 1}) {
            trunk.addOrReplaceChild("root_" + side, cubes()
                    .addBox(-1.0F, -5.0F, -1.0F, 2.0F, 6.5F, 2.0F),
                    PartPose.offsetAndRotation(side * 1.8F, 12.4F, 0, 0, 0, side * 0.4F));
        }
        // These heads use coordinates local to the neck pivot at y=23 model units.
        PartDefinition head = part(root, "head", cubes()
                .addBox(-3.2F, 0.0F, -2.7F, 6.4F, 5.9F, 5.4F)
                .addBox(-2.5F, -0.6F, -2.15F, 5.0F, 1.1F, 4.3F));
        head.addOrReplaceChild("crest_left", cubes().addBox(-0.65F, 0, -0.55F, 1.3F, 3.3F, 1.1F),
                PartPose.offsetAndRotation(-2.6F, 4.7F, -0.2F, 0, 0, 0.25F));
        head.addOrReplaceChild("crest_right", cubes().addBox(-0.65F, 0, -0.55F, 1.3F, 2.6F, 1.1F),
                PartPose.offsetAndRotation(2.6F, 4.7F, -0.2F, 0, 0, -0.25F));
        PartDefinition beast = part(root, "beast_head", cubes()
                .addBox(-3.5F, -0.2F, -2.8F, 7.0F, 5.6F, 5.7F)
                .addBox(-2.45F, 0.1F, 2.0F, 4.9F, 2.65F, 3.5F)
                .addBox(-1.45F, -0.6F, 2.7F, 2.9F, 0.9F, 2.35F));
        for (int side : new int[] {-1, 1}) {
            PartDefinition horn = beast.addOrReplaceChild("horn_" + side, cubes()
                    .addBox(-0.8F, 0, -0.8F, 1.6F, 4.2F, 1.6F),
                    PartPose.offsetAndRotation(side * 2.6F, 4.2F, -1.7F, -0.23F, 0, -side * 0.24F));
            horn.addOrReplaceChild("tip", cubes().addBox(-0.42F, 0, -0.42F, 0.84F, 2.3F, 0.84F),
                    PartPose.offsetAndRotation(0, 3.7F, 0, -0.3F, 0, -side * 0.17F));
        }
        PartDefinition crown = part(root, "tree_crown", cubes());
        for (int side : new int[] {-1, 1}) {
            PartDefinition branch = crown.addOrReplaceChild("branch_" + side, cubes()
                    .addBox(-0.8F, 0, -0.8F, 1.6F, 9.4F, 1.6F),
                    PartPose.offsetAndRotation(side * 1.5F, -0.8F, 0, 0.12F, 0, -side * 0.65F));
            branch.addOrReplaceChild("fork", cubes().addBox(-0.5F, 0, -0.5F, 1.0F, 4.7F, 1.0F),
                    PartPose.offsetAndRotation(0, 4.5F, 0, -0.25F, 0, side * 0.65F));
            branch.addOrReplaceChild("bud", cubes()
                    .addBox(-1.4F, 7.3F, -1.2F, 2.8F, 2.2F, 2.4F), PartPose.ZERO);
        }
        crown.addOrReplaceChild("spire", cubes()
                .addBox(-0.7F, -0.4F, -0.7F, 1.4F, 7.8F, 1.4F)
                .addBox(-1.45F, 5.3F, -1.3F, 2.9F, 2.5F, 2.6F),
                PartPose.offsetAndRotation(0.0F, 0, -0.7F, -0.12F, 0, 0.1F));
        part(root, "eyes", cubes()
                .addBox(-2.4F, 2.45F, 2.71F, 1.55F, 0.65F, 0.18F)
                .addBox(0.85F, 2.45F, 2.71F, 1.55F, 0.65F, 0.18F)
                .addBox(-0.25F, 0.8F, 2.72F, 0.5F, 0.8F, 0.16F));
        part(root, "beast_eyes", cubes()
                .addBox(-3.22F, 2.65F, 2.93F, 1.8F, 0.55F, 0.18F)
                .addBox(1.42F, 2.65F, 2.93F, 1.8F, 0.55F, 0.18F)
                .addBox(-0.75F, 1.3F, 5.5F, 1.5F, 0.8F, 0.18F));
        part(root, "tree_eyes", cubes()
                .addBox(-1.95F, -1.8F, 2.02F, 1.3F, 0.55F, 0.18F)
                .addBox(0.65F, -1.8F, 2.02F, 1.3F, 0.55F, 0.18F));
        for (String side : new String[] {"left", "right"}) {
            float sign = side.equals("left") ? -1.0F : 1.0F;
            PartDefinition arm = part(root, side + "_arm", cubes()
                    .addBox(-1.15F, -5.9F, -1.15F, 2.3F, 6.0F, 2.3F)
                    .addBox(-0.9F + sign * 0.1F, -9.2F, -0.8F, 1.8F, 3.7F, 1.6F));
            arm.addOrReplaceChild("hand", cubes()
                    .addBox(-1.2F, -1.9F, -0.6F, 2.4F, 1.9F, 1.2F)
                    .addBox(-1.15F, -3.2F, -0.55F, 0.55F, 1.5F, 0.6F)
                    .addBox(-0.25F, -3.5F, -0.55F, 0.55F, 1.8F, 0.6F)
                    .addBox(0.65F, -3.0F, -0.55F, 0.55F, 1.3F, 0.6F),
                    PartPose.offsetAndRotation(sign * 0.25F, -8.4F, 0.2F, -0.15F, 0, -sign * 0.12F));
        }
        part(root, "arm_channels", cubes()
                .addBox(-0.18F, -5.7F, 1.17F, 0.36F, 5.4F, 0.16F)
                .addBox(-0.17F, -8.8F, 0.83F, 0.34F, 2.5F, 0.16F));
        part(root, "channels", cubes()
                .addBox(-0.22F, 14.3F, 2.18F, 0.44F, 7.3F, 0.18F)
                .addBox(-3.5F, 20.2F, 2.18F, 3.35F, 0.35F, 0.18F)
                .addBox(-3.5F, 17.0F, 2.18F, 2.55F, 0.35F, 0.18F)
                .addBox(-3.55F, 17.0F, 2.18F, 0.35F, 3.5F, 0.18F));
        part(root, "secondary_channels", cubes()
                .addBox(0.15F, 20.2F, 2.18F, 3.35F, 0.35F, 0.18F)
                .addBox(0.95F, 17.0F, 2.18F, 2.55F, 0.35F, 0.18F)
                .addBox(3.2F, 17.0F, 2.18F, 0.35F, 3.5F, 0.18F));
        part(root, "tree_channels", cubes()
                .addBox(-0.2F, 12.0F, 2.03F, 0.4F, 11.0F, 0.17F)
                .addBox(-1.9F, 17.2F, 2.03F, 3.8F, 0.35F, 0.17F)
                .addBox(-1.95F, 17.2F, 2.03F, 0.35F, 4.2F, 0.17F)
                .addBox(1.6F, 17.2F, 2.03F, 0.35F, 4.2F, 0.17F));
        part(root, "core", cubes().addBox(-1.25F, -1.25F, -1.25F, 2.5F, 2.5F, 2.5F));
        PartDefinition ring = part(root, "core_ring", cubes());
        for (int i = 0; i < 12; i++) {
            float angle = i * (float) Math.PI / 6.0F;
            ring.addOrReplaceChild("edge_" + i, cubes().addBox(-0.55F, -0.15F, -0.15F, 1.1F, 0.3F, 0.3F),
                    PartPose.offsetAndRotation((float) Math.cos(angle) * 2.65F,
                            (float) Math.sin(angle) * 2.65F, 0, 0, 0, angle + (float) Math.PI / 2));
        }
        part(root, "mote", cubes().addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F));
        for (int i = 0; i < 5; i++) {
            float side = (i - 2) / 2.0F;
            float x = (float) Math.sin(i * 1.9) * 1.25F;
            float z = (float) Math.cos(i * 1.9) * 1.2F;
            PartDefinition ribbon = part(root, "ribbon_" + i, cubes()
                    .addBox(x - 1.25F, -3.9F, z - 0.65F, 2.5F, 4.2F, 1.3F)
                    .addBox(x - 0.9F + side * 0.5F, -7.7F, z - 0.45F + 0.6F, 1.8F, 4.0F, 0.9F)
                    .addBox(x - 0.55F + side, -10.5F - (i % 2), z - 0.3F + 1.3F, 1.1F, 3.2F, 0.6F)
                    .addBox(x - 0.25F + side * 1.6F, -12.9F - (i % 2), z - 0.15F + 1.9F, 0.5F, 2.8F, 0.3F));
        }
        return LayerDefinition.create(mesh, 16, 16);
    }
}
