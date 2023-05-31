package org.samo_lego.fabrictailor.mixin.client;

import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SkinCustomizationScreen;
import net.minecraft.network.chat.Component;
import org.samo_lego.fabrictailor.client.ClientTailor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;


@Mixin(SkinCustomizationScreen.class)
public class MSkinCustomizationScreen_SkinButton extends OptionsSubScreen {
    public MSkinCustomizationScreen_SkinButton(Screen screen, Options options, Component component) {
        super(screen, options, component);
    }

    @Inject(method = "init", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onInit(CallbackInfo ci, int i) {
        if (this.minecraft.player != null) {
            this.addRenderableWidget(Button.builder(Component.literal("FabricTailor"),
                    button -> this.minecraft.setScreen(ClientTailor.SKIN_CHANGE_SCREEN)).bounds(this.width / 2 - 100, this.height / 6 + 24 * (i >> 1) + 22, 200, 20).build());
        }
    }
}
