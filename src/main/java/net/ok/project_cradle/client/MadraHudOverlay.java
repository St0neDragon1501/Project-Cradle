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
    }
}
