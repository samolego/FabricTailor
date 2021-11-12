package org.samo_lego.fabrictailor.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;
import org.samo_lego.fabrictailor.client.screen.SkinChangeScreen;
import org.samo_lego.fabrictailor.util.TranslatedText;

/**
 * This doesn't work in server environment,
 * it's meant for singleplayer OR
 * if BOTH server and client have the mod.
 */
@Environment(EnvType.CLIENT)
public class ClientTailor implements ClientModInitializer {

    /**
     * Whether mod is present on server
     */
    public static boolean TAILORED_SERVER = false;
    public static boolean ALLOW_DEFAULT_SKIN = true;

    public static KeyBinding skinKeybind;

    protected static final SkinChangeScreen SKIN_CHANGE_SCREEN = new SkinChangeScreen();
    private boolean forceOpen = false;

    @Override
    public void onInitializeClient() {
        skinKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fabrictailor.toggle_skin_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K, // K for opening the window
                "category.fabrictailor.skin_category"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if(skinKeybind.wasPressed()) {
                if (TAILORED_SERVER && forceOpen) {
                    client.setScreen(SKIN_CHANGE_SCREEN);
                    forceOpen = false;
                } else {
                    client.player.sendMessage(new TranslatedText("error.fabrictailor.not_installed").formatted(Formatting.RED), true);
                    forceOpen = true;
                }
            }
        });
    }
}
