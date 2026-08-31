package net.ok.project_cradle.capability.madra;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.capabilities.EntityCapability;

public class MadraProvider {

    public static final EntityCapability<Madra, Void> CORE =
            EntityCapability.createVoid(
            		Identifier.fromNamespaceAndPath(
            		        "project_cradle",
            		        "madra"
            		),
                    Madra.class
            );

    private final Madra madra;

    public MadraProvider() {
        this.madra = new Madra();
    }

    public Madra getCore() {
        return madra;
    }
}
