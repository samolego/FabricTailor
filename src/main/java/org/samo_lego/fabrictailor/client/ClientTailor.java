package org.samo_lego.fabrictailor.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;
import org.samo_lego.fabrictailor.client.screen.SkinChangeScreen;

/**
 * This doesn't work in server environment,
 * it's meant for singleplayer OR
 * if BOTH server and client have the mod.
 */
@Environment(EnvType.CLIENT)
public class ClientTailor implements ClientModInitializer {

    public static KeyMapping keyBinding;

    protected static final SkinChangeScreen SKIN_CHANGE_SCREEN = new SkinChangeScreen();

    @Override
    public void onInitializeClient() {
        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.fabrictailor.toggle_skin_gui",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_K, // K for opening the window
                "category.fabrictailor.skin_category"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if(keyBinding.consumeClick()) {
                client.setScreen(SKIN_CHANGE_SCREEN);
            }
        });
    }
}
