package de.hannazockt.blockshelfitemstealing.client.keybind;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

/**
 * Handles the bypass keybind registration.
 */
public class ModKeyBindings {
    public static KeyBinding bypassKey;

    public static void register() {
        bypassKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.blockshelfitemstealing.bypass",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_ALT,
                KeyBinding.Category.create(Identifier.of("category.blockshelfitemstealing.main"))
        ));
    }
}