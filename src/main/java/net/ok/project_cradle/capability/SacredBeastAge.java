package net.ok.project_cradle.capability;

import java.util.LinkedHashSet;
import java.util.List;
import com.mojang.serialization.Codec;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.ok.project_cradle.spells.TechniqueCatalog;

/** Persistent sacred status, spawn selection, techniques and the legacy century of adult life. */
public final class SacredBeastAge implements ValueIOSerializable {
    private long livedTicks;
    private boolean centuryChecked;
    private boolean sacred;
    private boolean spawnChecked;
    private List<String> techniques = List.of();

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

    public boolean wasSpawnChecked() {
        return this.spawnChecked;
    }

    public void markSpawnChecked() {
        this.spawnChecked = true;
    }

    public List<String> getTechniques() {
        return this.techniques;
    }

    /** Empty legacy data stays empty until the server assigns a suitable species profile. */
    public void setTechniques(List<String> techniques) {
        var valid = new LinkedHashSet<String>();
        if (techniques != null) for (String technique : techniques) {
            String id = TechniqueCatalog.normalize(technique);
            if (!id.isEmpty() && valid.size() < 2) valid.add(id);
        }
        this.techniques = List.copyOf(valid);
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putLong("livedTicks", this.livedTicks);
        output.putBoolean("centuryChecked", this.centuryChecked);
        output.putBoolean("sacred", this.sacred);
        output.putBoolean("spawnChecked", this.spawnChecked);
        output.store("techniques", Codec.STRING.listOf(), this.techniques);
    }

    @Override
    public void deserialize(ValueInput input) {
        this.livedTicks = Math.max(0L, input.getLongOr("livedTicks", 0L));
        this.centuryChecked = input.getBooleanOr("centuryChecked", false);
        this.sacred = input.getBooleanOr("sacred", false);
        this.spawnChecked = input.getBooleanOr("spawnChecked", false);
        this.setTechniques(input.read("techniques", Codec.STRING.listOf()).orElse(List.of()));
    }
}
