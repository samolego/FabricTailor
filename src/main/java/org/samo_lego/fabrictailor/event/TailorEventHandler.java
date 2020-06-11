package org.samo_lego.fabrictailor.event;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import sun.net.www.protocol.http.HttpURLConnection;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Objects;

import static org.samo_lego.fabrictailor.FabricTailor.errorLog;
import static org.samo_lego.fabrictailor.FabricTailor.setPlayerSkin;

public class TailorEventHandler {

    // Pretty self explanatory
    public static void onPlayerJoin(ServerPlayerEntity player, String value, String signature) {
        if(!value.isEmpty() && !signature.isEmpty())
            setPlayerSkin(player, value, signature);
        else if (!Objects.requireNonNull(player.getServer()).isOnlineMode())
            new Thread(() -> {
                // If user has no skin data
                // Getting skin data from ely.by api, since it can be used with usernames
                // it also includes mojang skins
                try {
                    HttpURLConnection connection = (HttpURLConnection) new URL("http://skinsystem.ely.by/textures/signed/" + player.getName().getString() + ".png?proxy=true").openConnection();
                    if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                        String reply = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
                        if (reply.contains("error")) {
                            player.sendSystemMessage(
                                    new LiteralText(
                                            "§cAn error occurred when trying to fetch skin."
                                    ),
                                    player.getUuid()
                            );
                            return;
                        }

                        String replyValue = reply.split("\"value\":\"")[1].split("\"")[0];
                        String replySignature = reply.split("\"signature\":\"")[1].split("\"")[0];

                        setPlayerSkin(player, replyValue, replySignature);

                    }
                } catch (IOException e) {
                    errorLog(e.getMessage());
                }
            }).start();
    }
}
