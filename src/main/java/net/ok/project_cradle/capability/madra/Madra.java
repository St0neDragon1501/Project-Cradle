package net.ok.project_cradle.capability.madra;

import com.mojang.serialization.Codec;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public class Madra implements ValueIOSerializable {

    private Double aLevel = 0.0;

    public Madra() {
    }

    public Madra(Double aLevel) {
        this.aLevel = aLevel;
    }

    public Double getALevel() {
        return aLevel;
    }

    public void setALevel(Double aLevel) {
        this.aLevel = aLevel;
    }
    
    public void addMadra(double amount, double maxMadra) {
        this.aLevel = Math.min(this.aLevel + amount, maxMadra);
    }

    public boolean trySpendMadra(double amount) {
        if (!Double.isFinite(amount) || amount <= 0.0 || this.aLevel < amount) {
            return false;
        }

        this.aLevel -= amount;
        return true;
    }

    public void copyFromMadra(Madra source) {
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
