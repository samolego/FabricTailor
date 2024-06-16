package org.samo_lego.fabrictailor.mixin.client;

import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.options.SkinCustomizationScreen;
import net.minecraft.network.chat.Component;
import org.samo_lego.fabrictailor.client.ClientTailor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;


@Mixin(SkinCustomizationScreen.class)
public abstract class MSkinCustomizationScreen_SkinButton extends OptionsSubScreen {

    public MSkinCustomizationScreen_SkinButton(Screen screen, Options options, Component component) {
        super(screen, options, component);
    }

    @Inject(method = "addOptions",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/OptionsList;addSmall(Ljava/util/List;)V"
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onInit(CallbackInfo ci, List<AbstractWidget> widgets) {
        if (this.minecraft != null && this.minecraft.player != null) {
            var ftButton = Button.builder(Component.literal("FabricTailor"),
                    button -> this.minecraft.setScreen(ClientTailor.SKIN_CHANGE_SCREEN)).build();
            widgets.add(ftButton);
        }
    }
}
