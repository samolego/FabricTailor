package org.samo_lego.fabrictailor.Command;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URL;

import static net.minecraft.command.arguments.MessageArgumentType.getMessage;
import static net.minecraft.command.arguments.MessageArgumentType.message;
import static org.samo_lego.fabrictailor.FabricTailor.setPlayerSkin;

public class SetskinCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean b) {
        dispatcher.register(CommandManager.literal("setskin")
            .then(CommandManager.argument("URL", message())
                    .executes(ctx -> fetchSkin((ServerPlayerEntity) ctx.getSource().getEntityOrThrow(), getMessage(ctx, "URL").getString()))
                    //.then(CommandManager.argument("slim", string())
                            //.executes(ctx -> skin(ctx.getSource(), StringArgumentType.getString(ctx, "URL"), true)))
            )
            .executes(ctx -> {
                Entity player = ctx.getSource().getEntityOrThrow();
                player.sendSystemMessage(
                    new LiteralText(
                            "You have to provide URL of the skin."
                    ),
                    player.getUuid()
                );
                return 1;
            })
        );
    }

    public static int fetchSkin(ServerPlayerEntity player, String skinUrl) {
        new Thread(() -> {
            /*try {
                URL url = new URL(skinUrl);
                Connection connection = Jsoup.connect("https://api.mineskin.org/generate/url?url=" + url).method(Connection.Method.POST).ignoreContentType(true).ignoreHttpErrors(true).timeout(40000);

                // Getting gson for parsing
                final Gson gson = new Gson();
                String body = connection.execute().body();
                System.out.println(body);

                // Parsing response
                JsonObject json = gson.fromJson(body, JsonObject.class);
                if (json.has("error")) {
                    player.sendSystemMessage(
                            new LiteralText(
                                    "§cAn error occurred when trying to fetch skin."
                            ),
                            player.getUuid()
                    );
                    return;
                }

                JsonObject textureObject = json.get("data").getAsJsonObject().get("texture").getAsJsonObject();

                // Getting skin data that we need
                String value = textureObject.get("value").getAsString();
                String signature = textureObject.get("signature").getAsString();

                if(setPlayerSkin(player, value, signature)) {
                    player.sendSystemMessage(
                            new LiteralText(
                                    "§aSkin data set."
                            ),
                            player.getUuid()
                    );
                }

            } catch (IOException e) {
                player.sendSystemMessage(
                    new LiteralText(
                            "§cMalformed url!"
                    ),
                    player.getUuid()
                );
            }*/
            try {
                URL url = new URL("https://api.mineskin.org/generate/url?url=" + skinUrl);
                /*URLConnection con = url.openConnection();
                HttpURLConnection http = (HttpURLConnection)con;
                http.setRequestMethod("POST");
                http.setDoOutput(true);
                http.connect();
                System.out.println(http.getRequestMethod());*/

                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(url.toString());
                HttpEntity httpEntity = client.execute(post).getEntity();

                if(httpEntity == null) {
                    return;
                }
                // Getting gson for parsing
                final Gson gson = new Gson();
                String body = EntityUtils.toString(httpEntity);
                System.out.println(body);

                // Parsing response
                JsonObject json = gson.fromJson(body, JsonObject.class);
                if (json.has("error")) {
                    player.sendSystemMessage(
                            new LiteralText(
                                    "§cAn error occurred when trying to fetch skin."
                            ),
                            player.getUuid()
                    );
                    return;
                }

                JsonObject textureObject = json.get("data").getAsJsonObject().get("texture").getAsJsonObject();

                // Getting skin data that we need
                String value = textureObject.get("value").getAsString();
                String signature = textureObject.get("signature").getAsString();

                if(setPlayerSkin(player, value, signature)) {
                    player.sendSystemMessage(
                            new LiteralText(
                                    "§aSkin data set."
                            ),
                            player.getUuid()
                    );
                }

            } catch (IOException e) {
                player.sendSystemMessage(
                        new LiteralText(
                                "§cMalformed url!"
                        ),
                        player.getUuid()
                );
            }
        }).start();
        return 1;
    }

}
