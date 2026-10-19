package net.ok.project_cradle.items;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.ok.project_cradle.server.spells.ForgerSpell;

public final class ForgerTechniqueItem extends MadraTechniqueItem {
    public ForgerTechniqueItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean castTechnique(ServerPlayer player, ItemStack stack) {
        return ForgerSpell.cast(player, stack);
    }
}
