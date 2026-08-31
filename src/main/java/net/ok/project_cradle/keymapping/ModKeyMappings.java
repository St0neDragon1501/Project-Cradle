package net.ok.project_cradle.keymapping;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

public class ModKeyMappings {
	
    private static final KeyMapping CYCLING_KEYMAPPING = new KeyMapping("key.project_cradle.cycling",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, KeyMapping.Category.MISC);
    
    public static final Lazy<KeyMapping> PRESS_CYCLING = Lazy.of(() -> CYCLING_KEYMAPPING);


    public static void register() {

    }
}