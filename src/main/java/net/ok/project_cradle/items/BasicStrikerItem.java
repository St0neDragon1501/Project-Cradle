package net.ok.project_cradle.items;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.ok.project_cradle.spells.BasicStrikerSpell;

public class BasicStrikerItem extends Item {
    public BasicStrikerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        return BasicStrikerSpell.use(level, player, itemStack);
    }
}
