package net.ok.project_cradle.items;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.ok.project_cradle.server.spells.ElementalTechniqueSpell;
import net.ok.project_cradle.spells.ElementalTechnique;

/** One inventory manual per elemental technique; its effects run on the server. */
public final class ElementalTechniqueItem extends MadraTechniqueItem {
    private final ElementalTechnique technique;

    public ElementalTechniqueItem(Properties properties, ElementalTechnique technique) {
        super(properties);
        this.technique = technique;
    }

    public ElementalTechnique getTechnique() {
        return this.technique;
    }

    @Override
    public boolean castTechnique(ServerPlayer player, ItemStack stack) {
        return ElementalTechniqueSpell.cast(player, stack, this.technique);
    }
}
