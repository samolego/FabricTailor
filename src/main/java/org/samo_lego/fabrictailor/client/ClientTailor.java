package org.samo_lego.fabrictailor.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * This doesn't work in server environment,
 * it's meant for singleplayer OR
 * if BOTH server and client have the mod.
 */
@Environment(EnvType.CLIENT)
public class ClientTailor implements ClientModInitializer {

    public static KeyBinding keyBinding;

    protected static final SkinChangeScreen SKIN_CHANGE_SCREEN = new SkinChangeScreen();

    @Override
    public void onInitializeClient() {
        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fabrictailor.toggle_skin_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K, // K for opening the window
                "category.fabrictailor.skin_category"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if(keyBinding.wasPressed()) {
                client.setScreen(SKIN_CHANGE_SCREEN);
            }
        });
    }
}
