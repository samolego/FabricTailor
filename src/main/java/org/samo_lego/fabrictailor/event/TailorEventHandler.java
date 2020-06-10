package org.samo_lego.fabrictailor.event;

import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import static org.samo_lego.fabrictailor.Command.SetskinCommand.fetchSkin;
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
                    URL url = new URL("http://skinsystem.ely.by/skins/" + player.getName().getString() + ".png");

                    // Since api does a redirect, we need to get the new URL
                    HttpClient httpclient = HttpClientBuilder.create().build();
                    HttpGet get = new HttpGet(url.toString());
                    HttpClientContext context = HttpClientContext.create();
                    HttpResponse response = httpclient.execute(get, context);

                    if(response == null) {
                        return;
                    }
                    // Executing method from `/setskin` command
                    fetchSkin(player, context.getRedirectLocations().iterator().next().toString());
                } catch (IOException e) {
                    errorLog(e.getMessage());
                }
            }).start();
    }
}
