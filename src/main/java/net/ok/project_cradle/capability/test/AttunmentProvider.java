package net.ok.project_cradle.capability.test;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.capabilities.EntityCapability;

public class AttunmentProvider {

    public static final EntityCapability<Shaper, Void> SHAPER =
            EntityCapability.createVoid(
            		Identifier.fromNamespaceAndPath(
            		        "project_cradle",
            		        "shaper"
            		),
                    Shaper.class
            );

    private final Shaper shaper;

    public AttunmentProvider() {
        this.shaper = new Shaper();
    }

    public Shaper getShaper() {
        return shaper;
    }
}
