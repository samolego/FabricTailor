package org.samo_lego.fabrictailor.client.screen.tabs;

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

import java.util.Optional;

import static org.samo_lego.fabrictailor.network.SkinPackets.FABRICTAILOR_VANILLA_CHANGE;

public class PlayerSkinTab extends GuiComponent implements SkinTabType {

    private final MutableComponent TITLE;
    private final MutableComponent DESCRIPTION;
    private final ItemStack ICON;

    public PlayerSkinTab() {
        this.ICON = new ItemStack(Items.PLAYER_HEAD);
        this.DESCRIPTION = TextTranslations.create("description.fabrictailor.title_player");
        this.TITLE = TextTranslations.create("tab.fabrictailor.title_player");
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
    public boolean hasSkinModels() {
        return false;
    }

    @Override
    public Optional<Pair<ResourceLocation, FriendlyByteBuf>> getSkinChangePacket(LocalPlayer player, String playername, boolean _ignored) {
        Property skinData = SkinFetcher.fetchSkinByName(playername);

        if (skinData == null)
            return Optional.empty();

        return Optional.of(new Pair<>(FABRICTAILOR_VANILLA_CHANGE, SkinPackets.skin2ByteBuf(skinData)));
    }
}
