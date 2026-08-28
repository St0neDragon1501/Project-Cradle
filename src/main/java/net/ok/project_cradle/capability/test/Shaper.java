package net.ok.project_cradle.capability.test;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import com.mojang.serialization.Codec;

public class Shaper implements ValueIOSerializable {

    private boolean shaper = false;

    public Shaper() {
    }

    public boolean getShaper() {
        return shaper;
    }

    public void setShaper(boolean shaper) {
        this.shaper = shaper;
    }

    public void copyFromShaper(Shaper source) {
        this.shaper = source.shaper;
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putBoolean("shaper", shaper);
    }

    @Override
    public void deserialize(ValueInput input) {
    	shaper = input.read("shaper", Codec.BOOL).orElse(false);
    }
}