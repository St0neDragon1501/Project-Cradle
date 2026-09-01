package net.ok.project_cradle.capability.madra;

import com.mojang.serialization.Codec;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public class MaxMadra implements ValueIOSerializable {

    private Double aLevel = 1000.0;

    public MaxMadra() {
    }

    public Double getLevel() {
        return aLevel;
    }

    public void setALevel(Double aLevel) {
        this.aLevel = aLevel;
    }
    
    public void addMaxMadra(double amount) {
        this.aLevel += amount;
    }

    public void copyFromMaxMadra(MaxMadra source) {
        this.aLevel = source.aLevel;
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putDouble("aLevel", aLevel);
    }

    @Override
    public void deserialize(ValueInput input) {
        aLevel = input.read("aLevel", Codec.DOUBLE).orElse(1.0);
    }
}
