package org.samo_lego.fabrictailor.event;

import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.net.URL;

import static org.samo_lego.fabrictailor.Command.SetskinCommand.fetchSkin;

public class TailorEventHandler {

    // Pretty self explanatory
    public static void onPlayerJoin(ServerPlayerEntity player) {
        // todo get skin data from player nbt with ccapi
        // Puts the saved skindata to player's profile
        //setPlayerSkin(player, value, signature);

        // Getting skin data from ely.by api, since it can be used with usernames
        // it also includes mojang skins
        new Thread(() -> {
            try {
                URL url = new URL("http://skinsystem.ely.by/skins/" + player.getName().getString() + ".png");

                HttpClient httpclient = HttpClientBuilder.create().build();
                HttpGet get = new HttpGet(url.toString());
                HttpClientContext context = HttpClientContext.create();
                HttpResponse response = httpclient.execute(get, context);
                if(response == null) {
                    return;
                }
                fetchSkin(player, context.getRedirectLocations().iterator().next().toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
