package net.ok.project_cradle.items;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.ok.project_cradle.server.spells.BasicStrikerSpell;

public final class BasicStrikerItem extends MadraTechniqueItem {
    public BasicStrikerItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean castTechnique(ServerPlayer player, ItemStack stack) {
        return BasicStrikerSpell.cast(player, stack);
    }
}
