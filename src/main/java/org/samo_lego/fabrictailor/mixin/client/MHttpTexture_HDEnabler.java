package org.samo_lego.fabrictailor.mixin.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SkinTextureDownloader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SkinTextureDownloader.class)
public abstract class MHttpTexture_HDEnabler {

    @Shadow
    private static void setNoAlpha(NativeImage image, int x1, int y1, int x2, int y2) {
    }

    @Shadow
    private static void doNotchTransparencyHack(NativeImage image, int x1, int y1, int x2, int y2) {
    }

    /**
     * Essentially an overwrite of the processLegacySkin method, as
     * most of the values are hardcoded.
     *
     * @param skinImg NativeImage skin
     * @param cir     mixin callback info
     * @author samo_lego
     * @reason Enable HD textures
     */
    @Inject(method = "processLegacySkin", at = @At("HEAD"), cancellable = true)
    private static void ft_enableHD(NativeImage skinImg, String string, CallbackInfoReturnable<NativeImage> cir) {
        cir.setReturnValue(ft_proccessSkin(skinImg));
    }


    @Unique
    private static NativeImage ft_proccessSkin(NativeImage skinImg) {
        int height = skinImg.getHeight();
        int width = skinImg.getWidth();

        boolean legacyFormat = height == width / 2;

        if (legacyFormat) {
            NativeImage newFormatSkin = new NativeImage(width, width, true);
            newFormatSkin.copyFrom(skinImg);
            skinImg.close();

            skinImg = newFormatSkin;
            skinImg.fillRect(0, height, width, height, 0);
            skinImg.copyRect(width / 16, width / 4, width / 4, height, width / 16, width / 16, true, false);
            skinImg.copyRect(8, width / 4, width / 4, height, width / 16, width / 16, true, false);
            skinImg.copyRect(0, 20, 24, height, width / 16, 12, true, false);
            skinImg.copyRect(width / 16, 20, width / 4, height, width / 16, 12, true, false);
            skinImg.copyRect(8, 20, 8, height, width / 16, 12, true, false);
            skinImg.copyRect(12, 20, width / 4, height, width / 16, 12, true, false);
            skinImg.copyRect(44, width / 4, -8, height, width / 16, width / 16, true, false);
            skinImg.copyRect((width / 4) * 3, width / 4, -8, height, width / 16, width / 16, true, false);
            skinImg.copyRect(40, 20, 0, height, width / 16, 12, true, false);
            skinImg.copyRect(44, 20, -8, height, width / 16, 12, true, false);
            skinImg.copyRect((width / 4) * 3, 20, -width / 4, height, width / 16, 12, true, false);
            skinImg.copyRect(52, 20, -8, height, width / 16, 12, true, false);
        }
        setNoAlpha(skinImg, 0, 0, width / 2, width / 4);
        if (legacyFormat) {
            doNotchTransparencyHack(skinImg, width / 2, 0, width, width / 2);
        }
        setNoAlpha(skinImg, 0, width / 4, width, width / 2);
        setNoAlpha(skinImg, width / 4, (width / 4) * 3, (width / 4) * 3, width);
        return skinImg;
    }
}
