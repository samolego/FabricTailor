package org.samo_lego.fabrictailor.mixin.client;

import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static org.samo_lego.fabrictailor.client.ClientTailor.ALLOW_DEFAULT_SKIN;
import static org.samo_lego.fabrictailor.client.ClientTailor.TAILORED_SERVER;

@Mixin(ClientLoginNetworkHandler.class)
public class ClientLoginNetworkHandler_ServerJoin {

    @Inject(method = "joinServerSession", at = @At("RETURN"))
    private void onServerJoin(String serverId, CallbackInfoReturnable<Text> cir) {
        // Reset server having fabrictailor installed status
        TAILORED_SERVER = false;
        ALLOW_DEFAULT_SKIN = true;
    }
}
