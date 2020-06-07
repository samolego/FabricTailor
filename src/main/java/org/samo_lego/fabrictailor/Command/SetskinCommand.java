package org.samo_lego.fabrictailor.Command;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.URL;

import static net.minecraft.command.arguments.MessageArgumentType.getMessage;
import static net.minecraft.command.arguments.MessageArgumentType.message;
import static org.samo_lego.fabrictailor.FabricTailor.setPlayerSkin;

public class SetskinCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean b) {
        dispatcher.register(CommandManager.literal("setskin")
            .then(CommandManager.argument("URL", message())
                    .executes(ctx -> skin(ctx.getSource(), getMessage(ctx, "URL").getString(), false))
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

    private static int skin(ServerCommandSource source, String skinUrl, boolean slim) throws CommandSyntaxException {
        ServerPlayerEntity player = (ServerPlayerEntity) source.getEntityOrThrow();

        new Thread(() -> {
            try {
                URL url = new URL(skinUrl);
                Connection connection = Jsoup.connect("https://api.mineskin.org/generate/url?url=" + url).method(Connection.Method.POST).ignoreContentType(true).ignoreHttpErrors(true).timeout(40000);

                // Getting gson for parsing
                final Gson gson = new Gson();
                String body = connection.execute().body();

                // Parsing response
                JsonObject json = gson.fromJson(body, JsonObject.class);
                if (json.has("error")) {
                    player.sendSystemMessage(
                            new LiteralText(
                                    "§cAn error occured."
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
                    // Updating player's gameprofile
                    /*player.setInvisible(true);
                    player.setInvisible(false);
                    player.inventory.updateItems();
                    player.networkHandler.sendPacket(new PlayerRespawnS2CPacket(
                            DimensionType.OVERWORLD_REGISTRY_KEY,
                            player.getEntityWorld().getRegistryKey(),
                            0,
                            player.interactionManager.getGameMode(),
                            player.getEntityWorld().isDebugWorld(),
                            false,
                            true
                    ));*/
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
