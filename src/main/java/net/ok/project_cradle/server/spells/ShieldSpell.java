package net.ok.project_cradle.server.spells;

import net.minecraft.server.level.ServerPlayer;
import net.ok.project_cradle.entities.MadraShieldEntity;

/** Shield placement; the shared cast service supplies costs and cooldowns. */
public final class ShieldSpell {
    private ShieldSpell() { }

    public static boolean cast(ServerPlayer player) {
        return MadraShieldEntity.spawn(player);
    }
}
