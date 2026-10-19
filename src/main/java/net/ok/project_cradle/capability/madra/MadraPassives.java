package net.ok.project_cradle.capability.madra;

import net.minecraft.world.entity.player.Player;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.core.Core;
import net.ok.project_cradle.capability.core.advancement.AdvancementLevel;

/** Shared eligibility rules for aspect passives, independent of a Goldsign's appearance. */
public final class MadraPassives {
    private MadraPassives() {
    }

    public static boolean has(Player player, MadraType.Element aspect) {
        if (player == null || aspect == null || !player.isAlive() || player.isSpectator()) {
            return false;
        }
        Core core = player.getData(ModAttachments.CORE);
        return core.isMonarch() || (core.getAdvancement().order() >= AdvancementLevel.LOWGOLD.order()
                && player.getData(ModAttachments.MADRA_TYPE).getTypes().contains(aspect));
    }
}
