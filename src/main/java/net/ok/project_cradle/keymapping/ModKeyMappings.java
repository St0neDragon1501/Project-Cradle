package net.ok.project_cradle.keymapping;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.util.Lazy;
import net.ok.project_cradle.ProjectCradle;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class ModKeyMappings {

    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(
            Identifier.fromNamespaceAndPath(ProjectCradle.MODID, "controls")
    );
	
    private static final KeyMapping CYCLING_KEYMAPPING = new KeyMapping("key.project_cradle.cycling",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, CATEGORY);
    
    public static final Lazy<KeyMapping> PRESS_CYCLING = Lazy.of(() -> CYCLING_KEYMAPPING);

    public static final List<KeyMapping> QUICK_DROP_BINDINGS = List.of(
            new KeyMapping("key.project_cradle.string_1", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, CATEGORY),
            new KeyMapping("key.project_cradle.string_2", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, CATEGORY),
            new KeyMapping("key.project_cradle.string_3", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, CATEGORY),
            new KeyMapping("key.project_cradle.string_4", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, CATEGORY),
            new KeyMapping("key.project_cradle.string_5", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, CATEGORY),
            new KeyMapping("key.project_cradle.string_6", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, CATEGORY)
    );

    public static void register() {

    }
}
