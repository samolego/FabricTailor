package org.samo_lego.fabrictailor.client.screen.tabs;

import com.google.common.net.InternetDomainName;
import com.google.gson.JsonObject;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.samo_lego.fabrictailor.network.payload.HDSkinPayload;
import org.samo_lego.fabrictailor.network.payload.VanillaSkinPayload;
import org.samo_lego.fabrictailor.util.SkinFetcher;
import org.samo_lego.fabrictailor.util.TextTranslations;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Optional;

import static org.samo_lego.fabrictailor.FabricTailor.config;

public class UrlSkinTab implements SkinTabType {

    private final MutableComponent TITLE;
    private final MutableComponent DESCRIPTION;
    private final ItemStack ICON;

    public UrlSkinTab() {
        this.TITLE = TextTranslations.create("tab.fabrictailor.title_url");
        this.DESCRIPTION = TextTranslations.create("description.fabrictailor.title_url");
        this.ICON = new ItemStack(Items.GLOBE_BANNER_PATTERN);
    }

    @Override
    public MutableComponent getTitle() {
        return this.TITLE;
    }

    @Override
    public MutableComponent getDescription() {
        return this.DESCRIPTION;
    }

    @Override
    public ItemStack getIcon() {
        return this.ICON;
    }


    @Override
    public Optional<CustomPacketPayload> getSkinChangePacket(LocalPlayer player, String url, boolean useSlim) {
        try {
            URL skinUrl = URI.create(url).toURL();
            BufferedImage image = ImageIO.read(skinUrl);
            int height = image.getHeight();
            int width = image.getWidth();

            if (width == 64 && (height == 32 || height == 64)) {
                // Normal skin (vanilla compatible)
                var skinData = SkinFetcher.fetchSkinByUrl(url, useSlim);

                if (skinData == null)
                    return Optional.empty();

                return Optional.of(new VanillaSkinPayload(skinData));
            } else {
                // HD skin (not vanilla compatible)
                JsonObject metadata = null;
                if (useSlim) {
                    metadata = new JsonObject();
                    metadata.addProperty("model", "slim");
                }

                // Check if tld is allowed
                String tld = InternetDomainName.from(skinUrl.getHost()).topDomainUnderRegistrySuffix().toString();
                if (!config.allowedTextureDomains.contains(tld)) {
                    // Redirect to duckduckgo
                    // e.g. convert https://image.com/image.png to https://external-content.duckduckgo.com/iu/?u=https://image.com/image.png
                    url = "https://external-content.duckduckgo.com/iu/?u=" + skinUrl;
                }

                var skinData = this.getExtendedProperty(player, MinecraftProfileTexture.Type.SKIN, url, metadata);
                return Optional.of(new HDSkinPayload(skinData));
            }

        } catch (IOException ignored) {
            return Optional.empty();
        }

    }
}
