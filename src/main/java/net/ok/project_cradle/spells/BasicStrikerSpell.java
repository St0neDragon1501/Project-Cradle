package net.ok.project_cradle.spells;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.madra.Madra;
import net.ok.project_cradle.capability.madra.MadraType;
import net.ok.project_cradle.capability.madra.MaxMadra;
import net.ok.project_cradle.entities.BasicStrikerProjectile;

public final class BasicStrikerSpell {
    public static final double MADRA_COST = 10.0;

    private static final int COOLDOWN_TICKS = 10;
    private static final float PROJECTILE_SPEED = 1.8F;

    private BasicStrikerSpell() {
    }

    public static InteractionResult use(Level level, Player player, ItemStack itemStack) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        return cast(player, itemStack) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    public static boolean castFromQuickSlot(Player player, ItemStack itemStack) {
        return cast(player, itemStack);
    }

    private static boolean cast(Player player, ItemStack itemStack) {
        if (!(player.level() instanceof ServerLevel serverLevel)
                || player.getCooldowns().isOnCooldown(itemStack)) {
            return false;
        }

        Madra madra = player.getData(ModAttachments.MADRA);
        if (!madra.trySpendMadra(MADRA_COST)) {
            player.sendSystemMessage(
                    Component.translatable("message.project_cradle.not_enough_madra", MADRA_COST)
            );
            return false;
        }

        BasicStrikerProjectile projectile = new BasicStrikerProjectile(player.level(), player, itemStack);
        MadraType madraType = player.getData(ModAttachments.MADRA_TYPE);
        projectile.setMadraElement(madraType.getTypes().get(0));
        projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, PROJECTILE_SPEED, 0.0F);
        if (!serverLevel.addFreshEntity(projectile)) {
            MaxMadra maxMadra = player.getData(ModAttachments.MAXMADRA);
            madra.addMadra(MADRA_COST, maxMadra.getLevel());
            player.syncData(ModAttachments.MADRA);
            return false;
        }

        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.EVOKER_CAST_SPELL,
                SoundSource.PLAYERS,
                0.65F,
                1.2F
        );
        player.getCooldowns().addCooldown(itemStack, COOLDOWN_TICKS);
        player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
        player.syncData(ModAttachments.MADRA);
        return true;
    }
}
