package org.samo_lego.fabrictailor.mixin.client;

import com.google.common.net.InternetDomainName;
import com.mojang.authlib.yggdrasil.TextureUrlChecker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.net.URI;

import static org.samo_lego.fabrictailor.FabricTailor.config;

@Mixin(value = TextureUrlChecker.class, remap = false)
public class MTextureUrlChecker_AllDomains {

    @Inject(method = "isAllowedTextureDomain",
            at = @At(value = "INVOKE", target = "Ljava/net/URI;getScheme()Ljava/lang/String;"),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private static void ft_allowAllTextureDomains(String url, CallbackInfoReturnable<Boolean> cir, URI uri) {
        if (uri.getScheme().equals("file")) {
            cir.setReturnValue(false);  // todo, allow files
            return;
        }

        String topDomain = InternetDomainName.from(uri.getHost())
                .topDomainUnderRegistrySuffix()
                .toString()
                .toLowerCase();

        var allowed = config.allowedTextureDomains.contains(topDomain);
        cir.setReturnValue(allowed);
    }
}
