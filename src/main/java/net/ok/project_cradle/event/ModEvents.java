package net.ok.project_cradle.event;

import net.minecraft.world.entity.player.Player;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.capability.test.ModAttachments;
import net.ok.project_cradle.capability.test.Core;

@EventBusSubscriber(modid = ProjectCradle.MODID)
public class ModEvents {

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {

        if (event.isWasDeath()) {

            Player original = event.getOriginal();
            Player newPlayer = event.getEntity();

            Core oldShaper = original.getData(ModAttachments.CORE);
            Core newShaper = newPlayer.getData(ModAttachments.CORE);

            newShaper.copyFromCore(oldShaper);
        }
    }
}