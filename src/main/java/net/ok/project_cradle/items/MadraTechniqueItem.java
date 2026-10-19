package net.ok.project_cradle.items;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;
import net.ok.project_cradle.spells.TechniqueCastProfile;
import net.ok.project_cradle.spells.PathTechnique;
import net.ok.project_cradle.spells.TechniqueScaling;
import net.ok.project_cradle.server.spells.FoxDreamCharmService;
import net.ok.project_cradle.server.spells.TechniqueCastingService;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/** Common item adapter: the client predicts use; the server validates and casts the technique. */
public abstract class MadraTechniqueItem extends Item implements TechniqueItem {
    protected MadraTechniqueItem(Properties properties) {
        super(properties);
    }

    @Override
    public final InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        return TechniqueCastingService.begin(serverPlayer, serverPlayer.getItemInHand(hand))
                ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
            Consumer<Component> tooltip, TooltipFlag flag) {
        TechniqueCastProfile profile = TechniqueCastProfile.fromItemStack(stack);
        if (profile == null) return;
        if (this instanceof AdditionalTechniqueItem extra) {
            tooltip.accept(Component.translatable(extra.getTechnique().descriptionTranslationKey()).withStyle(ChatFormatting.GRAY));
        }
        Player viewer = context.player();
        if (this instanceof PathTechniqueItem path && path.getTechnique() == PathTechnique.FOX_DREAM) {
            tooltip.accept(Component.translatable("tooltip.project_cradle.fox_dream").withStyle(ChatFormatting.GRAY));
            int activeTicks = viewer == null ? FoxDreamCharmService.AURA_TICKS
                    : TechniqueScaling.duration(viewer, FoxDreamCharmService.AURA_TICKS);
            int charmTicks = viewer == null ? FoxDreamCharmService.CHARM_TICKS
                    : TechniqueScaling.duration(viewer, FoxDreamCharmService.CHARM_TICKS);
            tooltip.accept(Component.translatable("tooltip.project_cradle.fox_dream_duration",
                    activeTicks / 20.0, charmTicks / 20.0).withStyle(ChatFormatting.GRAY));
            double charmCost = viewer == null ? FoxDreamCharmService.CHARM_MADRA_COST
                    : TechniqueScaling.madraCost(viewer, FoxDreamCharmService.CHARM_MADRA_COST);
            tooltip.accept(Component.translatable("tooltip.project_cradle.fox_dream_charm_cost", charmCost)
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        tooltip.accept(Component.translatable(viewer == null
                        ? "tooltip.project_cradle.technique_base_cost" : "tooltip.project_cradle.technique_cost",
                viewer == null ? profile.madraCost() : profile.madraCost(viewer)).withStyle(ChatFormatting.GOLD));
        tooltip.accept(Component.translatable("tooltip.project_cradle.technique_timing", profile.castTimeTicks() / 20.0,
                profile.cooldownTicks() / 20.0).withStyle(ChatFormatting.GRAY));
        if (profile.soulfireCost() > 0) tooltip.accept(Component.translatable("tooltip.project_cradle.technique_soulfire", profile.soulfireCost()).withStyle(ChatFormatting.GOLD));
    }
}

