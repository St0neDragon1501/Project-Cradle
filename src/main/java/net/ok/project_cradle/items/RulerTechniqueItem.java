package net.ok.project_cradle.items;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.ok.project_cradle.server.spells.RulerSpell;

public final class RulerTechniqueItem extends MadraTechniqueItem {
    public RulerTechniqueItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean castTechnique(ServerPlayer player, ItemStack stack) {
        return RulerSpell.cast(player, stack);
    }
}
