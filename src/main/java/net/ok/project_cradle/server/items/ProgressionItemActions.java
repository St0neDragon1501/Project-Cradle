package net.ok.project_cradle.server.items;

import java.util.stream.Collectors;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.core.Core;
import net.ok.project_cradle.capability.core.IronBodyType;
import net.ok.project_cradle.capability.core.ProgressionHints;
import net.ok.project_cradle.server.progression.RemnantAspectData;
import net.ok.project_cradle.capability.core.advancement.AdvancementLevel;
import net.ok.project_cradle.capability.madra.Madra;
import net.ok.project_cradle.capability.madra.MadraType;
import net.ok.project_cradle.capability.madra.MaxMadra;
import net.ok.project_cradle.server.progression.ProgressionService;
import net.ok.project_cradle.server.progression.GoldsignAppearanceService;
import net.ok.project_cradle.server.progression.GoldPassiveService;

/** Server rules and item consumption for sacred-arts progression items. */
public final class ProgressionItemActions {
    private ProgressionItemActions() {
    }

    public static InteractionResult beginFoundation(ServerPlayer player, InteractionHand hand) {
        if (player.getData(ModAttachments.CORE).hasReceivedTutorial()) {
            player.sendSystemMessage(Component.translatable("message.project_cradle.path_already_started"));
            return InteractionResult.FAIL;
        }
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    public static void finishFoundation(ServerPlayer player, ItemStack stack) {
        if (ProgressionService.unlockFoundation(player) && !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

    public static InteractionResult alignIcon(ServerPlayer player, InteractionHand hand) {
        Core core = player.getData(ModAttachments.CORE);
        if (player.isShiftKeyDown()) {
            player.sendSystemMessage(Component.translatable("message.project_cradle.icon_select_hint", core.getPendingIcon()));
            return InteractionResult.SUCCESS;
        }
        if (!ProgressionService.alignIcon(player, core.getPendingIcon())) return InteractionResult.FAIL;
        if (!player.getAbilities().instabuild) player.getItemInHand(hand).shrink(1);
        return InteractionResult.SUCCESS;
    }

    public static InteractionResult completeIronBody(ServerPlayer player, InteractionHand hand, IronBodyType type) {
        if (player.getData(ModAttachments.CORE).getAdvancement() != AdvancementLevel.COPPER) {
            player.sendSystemMessage(Component.translatable("message.project_cradle.iron_body_requires_copper"));
            return InteractionResult.FAIL;
        }
        if (!ProgressionService.completeIronBody(player, type)) return InteractionResult.FAIL;
        if (!player.getAbilities().instabuild) player.getItemInHand(hand).shrink(1);
        return InteractionResult.SUCCESS;
    }

    public static InteractionResult absorbRemnant(ServerPlayer player, InteractionHand hand) {
        MadraType.Element aspect = RemnantAspectData.get(player.getItemInHand(hand), player.getData(ModAttachments.MADRA_TYPE).getTypes().get(0));
        if (!ProgressionService.absorbRemnant(player, aspect)) {
            player.sendSystemMessage(Component.translatable("message.project_cradle.remnant_requires_jade_or_archlord"));
            return InteractionResult.FAIL;
        }

        if (!player.getAbilities().instabuild) {
            player.getItemInHand(hand).shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    public static InteractionResult restoreMadra(ServerPlayer player, InteractionHand hand) {
        Madra madra = player.getData(ModAttachments.MADRA);
        MaxMadra maxMadra = player.getData(ModAttachments.MAXMADRA);
        madra.addMadra(50.0, maxMadra.getLevel());
        player.syncData(ModAttachments.MADRA);
        if (!player.getAbilities().instabuild) {
            player.getItemInHand(hand).shrink(1);
        }

        return InteractionResult.SUCCESS;
    }

    public static InteractionResult rejectReset(ServerPlayer player, InteractionHand hand) {
        player.sendSystemMessage(Component.translatable("message.project_cradle.path_irreversible"));

        return InteractionResult.FAIL;
    }

    public static InteractionResult addSoulfire(ServerPlayer player, InteractionHand hand) {
        double before = player.getData(ModAttachments.CORE).getSoulfire();
        ProgressionService.addSoulfire(player, 1.0);
        double after = player.getData(ModAttachments.CORE).getSoulfire();
        if (after <= before) {
            return InteractionResult.FAIL;
        }
        if (!player.getAbilities().instabuild) {
            player.getItemInHand(hand).shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    public static InteractionResult readManual(ServerPlayer player, InteractionHand hand) {
        Core core = player.getData(ModAttachments.CORE);
        player.sendSystemMessage(Component.translatable("message.project_cradle.manual_progression", Component.translatable("advancement.project_cradle." + core.getAdvancement().id())));
        player.sendSystemMessage(ProgressionHints.next(core));
        GoldPassiveService.describe(player);
        player.sendSystemMessage(Component.translatable("message.project_cradle.manual_commands"));
        if (core.hasSage() || core.hasHerald()) player.sendSystemMessage(Component.translatable("message.project_cradle.manual_path", core.hasHerald(), core.hasSage()));
        return InteractionResult.SUCCESS;
    }

    public static InteractionResult tuneMadraType(ServerPlayer player, InteractionHand hand) {
        if (player.getData(ModAttachments.CORE).getAdvancement().order() < AdvancementLevel.COPPER.order()) {
            player.sendSystemMessage(Component.translatable("message.project_cradle.need_copper_sight"));
            return InteractionResult.FAIL;
        }
        MadraType madraType = player.getData(ModAttachments.MADRA_TYPE);
        madraType.cycleType(player.isShiftKeyDown());
        player.syncData(ModAttachments.MADRA_TYPE);
        GoldsignAppearanceService.refresh(player);

        String typeNames = madraType.getTypes().stream()
                .map(element -> Component.translatable(element.translationKey()).getString())
                .collect(Collectors.joining(", "));
        player.sendSystemMessage(
                Component.translatable("message.project_cradle.madra_types_changed", typeNames)
        );

        ItemStack itemStack = player.getItemInHand(hand);
        player.getCooldowns().addCooldown(itemStack, 5);

        return InteractionResult.SUCCESS;
    }
}
