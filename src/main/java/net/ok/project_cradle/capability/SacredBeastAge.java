package net.ok.project_cradle.capability;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

/** Tracks loaded adult life toward the mod's compressed sacred-beast century. */
public final class SacredBeastAge implements ValueIOSerializable {
    private long livedTicks;
    private boolean centuryChecked;
    private boolean sacred;

    public long getLivedTicks() {
        return this.livedTicks;
    }

    public void tick() {
        if (!this.sacred) {
            this.livedTicks++;
        }
    }

    public boolean isSacred() {
        return this.sacred;
    }

    public boolean wasCenturyChecked() {
        return this.centuryChecked;
    }

    public void markCenturyChecked() {
        this.centuryChecked = true;
    }

    public void setSacred(boolean sacred) {
        this.sacred = sacred;
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putLong("livedTicks", this.livedTicks);
        output.putBoolean("centuryChecked", this.centuryChecked);
        output.putBoolean("sacred", this.sacred);
    }

    @Override
    public void deserialize(ValueInput input) {
        this.livedTicks = Math.max(0L, input.getLongOr("livedTicks", 0L));
        this.centuryChecked = input.getBooleanOr("centuryChecked", false);
        this.sacred = input.getBooleanOr("sacred", false);
    }
}
