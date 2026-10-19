package net.ok.project_cradle.items;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.ok.project_cradle.server.spells.PathTechniqueSpell;
import net.ok.project_cradle.spells.PathTechnique;

/** Shared item definition for a named Path technique. */
public final class PathTechniqueItem extends MadraTechniqueItem {
    private final PathTechnique technique;

    public PathTechniqueItem(Properties properties, PathTechnique technique) {
        super(properties);
        this.technique = technique;
    }

    @Override
    public boolean castTechnique(ServerPlayer player, ItemStack stack) {
        return PathTechniqueSpell.cast(player, stack, this.technique);
    }
}
