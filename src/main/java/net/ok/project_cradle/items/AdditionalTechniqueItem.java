package net.ok.project_cradle.items;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.ok.project_cradle.server.spells.AdditionalTechniqueSpell;
import net.ok.project_cradle.spells.AdditionalTechnique;

/** Item adapter shared by the eight general-purpose spells. */
public final class AdditionalTechniqueItem extends MadraTechniqueItem {
    private final AdditionalTechnique technique;

    public AdditionalTechniqueItem(Properties properties, AdditionalTechnique technique) {
        super(properties);
        this.technique = technique;
    }

    public AdditionalTechnique getTechnique() {
        return this.technique;
    }

    @Override
    public boolean castTechnique(ServerPlayer player, ItemStack stack) {
        return AdditionalTechniqueSpell.cast(player, stack, this.technique);
    }
}
