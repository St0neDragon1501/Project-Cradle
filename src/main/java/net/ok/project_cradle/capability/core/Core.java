package net.ok.project_cradle.capability.core;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.ok.project_cradle.utils.Utils;

import com.mojang.serialization.Codec;

public class Core implements ValueIOSerializable {

    private String aLevel = Utils.UNFOUNDED;

    public Core() {
    }

    public String getALevel() {
        return aLevel;
    }

    public void setALevel(String aLevel) {
        this.aLevel = aLevel;
    }

    public void copyFromCore(Core source) {
        this.aLevel = source.aLevel;
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putString("aLevel", aLevel);
    }

    @Override
    public void deserialize(ValueInput input) {
    	aLevel = input.read("aLevel", Codec.STRING).orElse("oops");
    }
}