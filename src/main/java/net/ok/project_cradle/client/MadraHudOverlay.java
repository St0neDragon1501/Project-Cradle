package net.ok.project_cradle.client;

import java.text.NumberFormat;
import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.core.Core;
import net.ok.project_cradle.capability.core.ProgressionHints;
import net.ok.project_cradle.capability.madra.Madra;
import net.ok.project_cradle.capability.madra.MadraType;
import net.ok.project_cradle.capability.madra.MaxMadra;

@EventBusSubscriber(modid = ProjectCradle.MODID, value = Dist.CLIENT)
public class MadraHudOverlay {

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        Madra madra = minecraft.player.getData(ModAttachments.MADRA);
        MaxMadra maxMadra = minecraft.player.getData(ModAttachments.MAXMADRA);
        MadraType madraType = minecraft.player.getData(ModAttachments.MADRA_TYPE);
        Core core = minecraft.player.getData(ModAttachments.CORE);

        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.ROOT);
        numberFormat.setMaximumFractionDigits(2);
        numberFormat.setMinimumFractionDigits(0);

        Component display = Component.translatable(
                "hud.project_cradle.madra",
                numberFormat.format(madra.getALevel()),
                numberFormat.format(maxMadra.getLevel())
        );

        int rightAlignedX = event.getGuiGraphics().guiWidth() - minecraft.font.width(display) - 8;
        event.getGuiGraphics().text(minecraft.font, display, rightAlignedX, 8, 0xFFFFFFFF, true);

        String typeNames = madraType.getTypes().stream()
                .map(element -> Component.translatable(element.translationKey()).getString())
                .collect(java.util.stream.Collectors.joining(", "));
        Component typeDisplay = Component.translatable("hud.project_cradle.madra_types", typeNames);
        int typeX = event.getGuiGraphics().guiWidth() - minecraft.font.width(typeDisplay) - 8;
        event.getGuiGraphics().text(minecraft.font, typeDisplay, typeX, 20, 0xFFFFFFFF, true);

        Component progressionDisplay = Component.translatable(
                "hud.project_cradle.progression",
                Component.translatable("advancement.project_cradle." + core.getAdvancement().id())
        );
        int progressionX = event.getGuiGraphics().guiWidth() - minecraft.font.width(progressionDisplay) - 8;
        event.getGuiGraphics().text(minecraft.font, progressionDisplay, progressionX, 32, 0xFFFFFFFF, true);

        int detailY = 44;
        var nextAdvancement = core.getAdvancement().nextAdvancement();
        if (nextAdvancement != null) {
            double requiredMadra = core.getAdvancement().requiredMaxMadra();
            double requiredSoulfire = core.getAdvancement().requiredSoulfire();
            Component capacityGoal = requiredSoulfire > 0.0
                    ? Component.translatable("hud.project_cradle.capacity_goal_soulfire",
                            Component.translatable("advancement.project_cradle." + nextAdvancement.id()),
                            numberFormat.format(maxMadra.getLevel()), numberFormat.format(requiredMadra),
                            numberFormat.format(core.getSoulfire()), numberFormat.format(requiredSoulfire))
                    : Component.translatable("hud.project_cradle.capacity_goal",
                            Component.translatable("advancement.project_cradle." + nextAdvancement.id()),
                            numberFormat.format(maxMadra.getLevel()), numberFormat.format(requiredMadra));
            int capacityX = event.getGuiGraphics().guiWidth() - minecraft.font.width(capacityGoal) - 8;
            event.getGuiGraphics().text(minecraft.font, capacityGoal, capacityX, detailY, 0xFFFFFFFF, true);
            detailY += 12;
        }
        if (!core.getGoldsign().isBlank()) {
            Component goldsign = Component.translatable(
                    "hud.project_cradle.goldsign",
                    Component.translatable(core.getResolvedGoldsignStyle().translationKey())
            );
            int goldsignX = event.getGuiGraphics().guiWidth() - minecraft.font.width(goldsign) - 8;
            event.getGuiGraphics().text(minecraft.font, goldsign, goldsignX, detailY, 0xFFFFFFFF, true);
            detailY += 12;
        }
        if (core.hasSage() && !core.getIcons().isEmpty()) {
            String names = core.getIcons().stream().map(name -> ProgressionHints.iconName(name).getString())
                    .collect(java.util.stream.Collectors.joining(", "));
            String visible = minecraft.font.plainSubstrByWidth(names,
                    Math.max(60, event.getGuiGraphics().guiWidth() / 2));
            if (!visible.equals(names)) visible += "… (" + core.getIcons().size() + ")";
            Component icons = Component.translatable("hud.project_cradle.icons", visible);
            int iconX = event.getGuiGraphics().guiWidth() - minecraft.font.width(icons) - 8;
            event.getGuiGraphics().text(minecraft.font, icons, iconX, detailY, 0xFFFFFFFF, true);
            detailY += 12;
        }
        if (core.hasSoulspace()) {
            Component soulfire = Component.translatable("hud.project_cradle.soulfire", numberFormat.format(core.getSoulfire()));
            int soulfireX = event.getGuiGraphics().guiWidth() - minecraft.font.width(soulfire) - 8;
            event.getGuiGraphics().text(minecraft.font, soulfire, soulfireX, detailY, 0xFFFFFFFF, true);
            detailY += 12;
        }
        Component status = Component.translatable("hud.project_cradle.spirit",
                Component.translatable(core.getIronBodyType().translationKey()),
                core.hasSage() ? "Sage" : "—", core.hasHerald() ? "Herald" : "—");
        int statusX = event.getGuiGraphics().guiWidth() - minecraft.font.width(status) - 8;
        event.getGuiGraphics().text(minecraft.font, status, statusX, detailY, 0xFFFFFFFF, true);
        detailY += 12;
        String hint = ProgressionHints.next(core).getString();
        hint = minecraft.font.plainSubstrByWidth(hint, Math.max(80, event.getGuiGraphics().guiWidth() - 16));
        event.getGuiGraphics().text(minecraft.font, Component.literal(hint),
                event.getGuiGraphics().guiWidth() - minecraft.font.width(hint) - 8, detailY, 0xFFBFE9FF, true);
        detailY += 12;
        if (core.getAdvancement().techniqueSlots() > 0) {
            Component selectedTechnique = Component.translatable(
                    "hud.project_cradle.selected_technique",
                    core.getSelectedTechnique() + 1,
                    Math.min(4, core.getAdvancement().techniqueSlots())
            );
            int selectedX = event.getGuiGraphics().guiWidth() - minecraft.font.width(selectedTechnique) - 8;
            event.getGuiGraphics().text(minecraft.font, selectedTechnique, selectedX, detailY, 0xFFFFFFFF, true);
        }
    }
}
