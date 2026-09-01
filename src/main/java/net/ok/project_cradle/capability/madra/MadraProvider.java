package net.ok.project_cradle.capability.madra;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.capabilities.EntityCapability;

public class MadraProvider {

    public static final EntityCapability<Madra, Void> MADRA =
            EntityCapability.createVoid(
            		Identifier.fromNamespaceAndPath(
            		        "project_cradle",
            		        "madra"
            		),
                    Madra.class
            );

    private final Madra madra;

    

    public Madra getMadra() {
        return madra;
    }
    
    public static final EntityCapability<MaxMadra, Void> MAXMADRA =
            EntityCapability.createVoid(
            		Identifier.fromNamespaceAndPath(
            		        "project_cradle",
            		        "max_madra"
            		),
                    MaxMadra.class
            );

    private final MaxMadra max_madra;

    

    public MaxMadra getMaxMadra() {
        return max_madra;
    }
    
    public MadraProvider() {
        this.madra = new Madra();
        this.max_madra = new MaxMadra();
    }
    
}
