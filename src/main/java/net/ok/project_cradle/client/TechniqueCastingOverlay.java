package net.ok.project_cradle.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.networking.packet.CastingStateS2C;

/** A technique name and its preparation progress, centered above the hotbar. */
@EventBusSubscriber(modid = ProjectCradle.MODID, value = Dist.CLIENT)
public final class TechniqueCastingOverlay {
    private static LocalPlayer caster;
    private static String translationKey = "";
    private static int startedAt;
    private static int duration;

    private TechniqueCastingOverlay() {
    }

    static void accept(CastingStateS2C packet) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (!packet.active() || packet.durationTicks() <= 0 || player == null) {
            clear();
            return;
        }
        caster = player;
        translationKey = packet.translationKey();
        startedAt = player.tickCount;
        duration = Math.min(600, packet.durationTicks());
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (caster == null || minecraft.player != caster || minecraft.gui.hud.isHidden()
                || minecraft.gui.screen() != null) return;
        float elapsed = caster.tickCount - startedAt
                + minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float progress = Mth.clamp(elapsed / duration, 0.0F, 1.0F);
        var graphics = event.getGuiGraphics();
        int width = Math.min(140, graphics.guiWidth() - 16);
        int left = (graphics.guiWidth() - width) / 2;
        int top = graphics.guiHeight() - 66;
        Component name = Component.translatable(translationKey);
        graphics.centeredText(minecraft.font, name, graphics.guiWidth() / 2, top - 12, 0xFFE5F8FF);
        graphics.fill(left - 1, top - 1, left + width + 1, top + 6, 0xC010202B);
        graphics.fill(left, top, left + width, top + 5, 0xD0293E4C);
        graphics.fill(left, top, left + Math.round(width * progress), top + 5, 0xFF85D8F5);
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (caster != null && (player != caster || !caster.isAlive() || caster.isSpectator()
                || caster.tickCount - startedAt > duration + 100)) clear();
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }

    private static void clear() {
        caster = null;
        translationKey = "";
        startedAt = 0;
        duration = 0;
    }
}
