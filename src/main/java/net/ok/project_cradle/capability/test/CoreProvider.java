package net.ok.project_cradle.capability.test;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.capabilities.EntityCapability;

public class CoreProvider {

    public static final EntityCapability<Core, Void> CORE =
            EntityCapability.createVoid(
            		Identifier.fromNamespaceAndPath(
            		        "project_cradle",
            		        "core"
            		),
                    Core.class
            );

    private final Core core;

    public CoreProvider() {
        this.core = new Core();
    }

    public Core getCore() {
        return core;
    }
}
