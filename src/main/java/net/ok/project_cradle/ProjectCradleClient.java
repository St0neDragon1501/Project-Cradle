package net.ok.project_cradle;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.ok.project_cradle.keymapping.ModKeyMappings;
import net.ok.project_cradle.networking.packet.TestPacketC2S;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = ProjectCradle.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = ProjectCradle.MODID, value = Dist.CLIENT)
public class ProjectCradleClient {
    public ProjectCradleClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
       
    }
    

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        ProjectCradle.LOGGER.info("HELLO FROM CLIENT SETUP");
        ProjectCradle.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        
        
    }
    
    @SubscribeEvent
    public static void registerKeymapping(RegisterKeyMappingsEvent event) {
        event.register(ModKeyMappings.PRESS_CYCLING.get());
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        while(ModKeyMappings.PRESS_CYCLING.get().consumeClick()) {
            // IN HERE: WE ARE ON THE CLIENT!
            Minecraft.getInstance().player.sendSystemMessage(Component.literal("You core feels bright!"));
            ClientPacketDistributor.sendToServer(new TestPacketC2S("Testing Pack", 67));
        }
    }
    
}
