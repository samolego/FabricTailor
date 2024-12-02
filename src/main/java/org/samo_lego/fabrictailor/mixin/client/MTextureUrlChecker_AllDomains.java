package org.samo_lego.fabrictailor.mixin.client;

import com.google.common.net.InternetDomainName;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.yggdrasil.TextureUrlChecker;
import java.net.URI;
import static org.samo_lego.fabrictailor.FabricTailor.config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = TextureUrlChecker.class, remap = false)
public final class MTextureUrlChecker_AllDomains {

    @Unique
    private static final String FILE_SCHEME_CONST = "file";

    private MTextureUrlChecker_AllDomains() {
    }

    @Inject(method = "isAllowedTextureDomain",
            at = @At(value = "INVOKE", target = "Ljava/net/URI;getScheme()Ljava/lang/String;"),
            cancellable = true)
    private static void ft_allowAllTextureDomains(String url, CallbackInfoReturnable<Boolean> cir, @Local URI uri) {
        if (FILE_SCHEME_CONST.equals(uri.getScheme())) {
            cir.setReturnValue(false);  // todo, allow files
            return;
        }

        if (uri.getHost() == null) {
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
