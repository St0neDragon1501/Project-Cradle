package net.ok.project_cradle.items;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.ok.project_cradle.server.spells.EnforcerSpell;

public final class EnforcerTechniqueItem extends MadraTechniqueItem {
    public EnforcerTechniqueItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean castTechnique(ServerPlayer player, ItemStack stack) {
        return EnforcerSpell.cast(player, stack);
    }
}
