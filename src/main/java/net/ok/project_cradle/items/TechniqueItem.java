package net.ok.project_cradle.items;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Technique activation always requires the authoritative server player. */
public interface TechniqueItem {
    boolean castTechnique(ServerPlayer player, ItemStack stack);
}
