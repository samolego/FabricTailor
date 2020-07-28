package org.samo_lego.fabrictailor.event;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Objects;

import static org.samo_lego.fabrictailor.Command.SkinCommand.fetchSkinByName;
import static org.samo_lego.fabrictailor.FabricTailor.setPlayerSkin;

public class TailorEventHandler {

    // Pretty self explanatory
    public static void onPlayerJoin(ServerPlayerEntity player, String value, String signature) {
        if(!value.isEmpty() && !signature.isEmpty())
            setPlayerSkin(player, value, signature);
        else if (!Objects.requireNonNull(player.getServer()).isOnlineMode() || player.getServerWorld().isClient())
            // Trying to fetch skin by playername
            fetchSkinByName(player, player.getName().toString(), false);
    }
}
