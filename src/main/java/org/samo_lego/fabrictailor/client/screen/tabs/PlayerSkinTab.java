package org.samo_lego.fabrictailor.client.screen.tabs;

import com.mojang.authlib.properties.Property;
import net.minecraft.client.gui.screens.advancements.AdvancementTabType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.samo_lego.fabrictailor.network.payload.VanillaSkinPayload;
import org.samo_lego.fabrictailor.util.SkinFetcher;
import org.samo_lego.fabrictailor.util.TextTranslations;

import java.util.Optional;

public class PlayerSkinTab implements SkinTabType {

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
    public Optional<CustomPacketPayload> getSkinChangePacket(LocalPlayer player, String playername, boolean _ignored) {
        Property skinData = SkinFetcher.fetchSkinByName(playername);

        if (skinData == null)
            return Optional.empty();

        return Optional.of(new VanillaSkinPayload(skinData));
    }

    @Override
    public AdvancementTabType getTabType() {
        return AdvancementTabType.ABOVE;
    }
}
