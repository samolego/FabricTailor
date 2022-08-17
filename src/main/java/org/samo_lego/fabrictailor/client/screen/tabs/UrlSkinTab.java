package org.samo_lego.fabrictailor.client.screen.tabs;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.samo_lego.fabrictailor.network.SkinPackets;
import org.samo_lego.fabrictailor.util.SkinFetcher;
import org.samo_lego.fabrictailor.util.TextTranslations;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

import static org.samo_lego.fabrictailor.network.SkinPackets.FABRICTAILOR_HD_CHANGE;
import static org.samo_lego.fabrictailor.network.SkinPackets.FABRICTAILOR_VANILLA_CHANGE;

public class UrlSkinTab extends GuiComponent implements SkinTabType {

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
    public Optional<Pair<ResourceLocation, FriendlyByteBuf>> getSkinChangePacket(LocalPlayer player, String url, boolean useSlim) {
        Property skinData;
        ResourceLocation channel;
        try {
            URL skinUrl = new URL(url);
            BufferedImage image = ImageIO.read(skinUrl);
            int height = image.getHeight();
            int width = image.getWidth();

            if (width == 64 && (height == 32 || height == 64)) {
                // Normal skin (vanilla compatible)
                skinData = SkinFetcher.fetchSkinByUrl(url, useSlim);

                if (skinData == null)
                    return Optional.empty();

                channel = FABRICTAILOR_VANILLA_CHANGE;
            } else {
                // HD skin (not vanilla compatible)
                skinData = this.getExtendedProperty(player, MinecraftProfileTexture.Type.SKIN, url);
                channel = FABRICTAILOR_HD_CHANGE;
            }

        } catch (IOException ignored) {
            return Optional.empty();
        }

        return Optional.of(new Pair<>(channel, SkinPackets.skin2ByteBuf(skinData)));
    }
}
