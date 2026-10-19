package net.ok.project_cradle.server.progression;

import net.minecraft.server.level.ServerPlayer;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.core.Core;
import net.ok.project_cradle.capability.core.GoldsignStyle;

/** Server authority for Goldsign appearance, broadcast through the public Core attachment. */
public final class GoldsignAppearanceService {
    private GoldsignAppearanceService() {
    }

    public static boolean refresh(ServerPlayer player) {
        Core core = player.getData(ModAttachments.CORE);
        GoldsignStyle resolved = GoldsignStyle.resolve(core, player.getData(ModAttachments.MADRA_TYPE));
        if (core.getResolvedGoldsignStyle() == resolved) return false;
        core.setResolvedGoldsignStyle(resolved);
        player.syncData(ModAttachments.CORE);
        return true;
    }

    /** Selection changes appearance only; it cannot grant a Goldsign to an artist without one. */
    public static boolean select(ServerPlayer player, GoldsignStyle style) {
        Core core = player.getData(ModAttachments.CORE);
        if (core.getGoldsign().isBlank() || style == null) return false;
        boolean changed = core.getGoldsignStyle() != style;
        core.setGoldsignStyle(style);
        boolean refreshed = refresh(player);
        if (changed && !refreshed) player.syncData(ModAttachments.CORE);
        return true;
    }
}
