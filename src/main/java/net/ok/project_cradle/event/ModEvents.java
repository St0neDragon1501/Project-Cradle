package net.ok.project_cradle.event;

import net.minecraft.world.entity.player.Player;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import net.ok.project_cradle.ProjectCradle;
import net.ok.project_cradle.capability.ModAttachments;
import net.ok.project_cradle.capability.core.Core;
import net.ok.project_cradle.capability.madra.Madra;
import net.ok.project_cradle.capability.madra.MaxMadra;

@EventBusSubscriber(modid = ProjectCradle.MODID)
public class ModEvents {

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {

        if (event.isWasDeath()) {

            Player original = event.getOriginal();
            Player newPlayer = event.getEntity();
            
            Core oldCoreShaper = original.getData(ModAttachments.CORE);
            Core newCoreShaper = newPlayer.getData(ModAttachments.CORE);
            
            newCoreShaper.copyFromCore(oldCoreShaper);
            
            
            
            Player originalMadra = event.getOriginal();
            Player newPlayerMadra = event.getEntity();
            
            Madra oldMadraShaper = originalMadra.getData(ModAttachments.MADRA);
            Madra newMadraShaper = newPlayerMadra.getData(ModAttachments.MADRA);

            newMadraShaper.copyFromMadra(oldMadraShaper);
            
            
            
            Player originalMaxMadra = event.getOriginal();
            Player newPlayerMaxMadra = event.getEntity();
            
            MaxMadra oldMaxMadraShaper = originalMaxMadra.getData(ModAttachments.MAXMADRA);
            MaxMadra newMaxMadraShaper = newPlayerMaxMadra.getData(ModAttachments.MAXMADRA);

            newMaxMadraShaper.copyFromMaxMadra(oldMaxMadraShaper);
            
            
        }
        
        
    }
    
    
}