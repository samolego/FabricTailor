package org.samo_lego.fabrictailor.Command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
                            "§6You have to provide URL of the skin."
                    ),
                    player.getUuid()
                );
                return 1;
            })
        );
    }

    public static int fetchSkin(ServerPlayerEntity player, String skinUrl) {
        player.sendSystemMessage(
                new LiteralText(
                        "§eTrying to set your skin ... Please wait."
                ),
                player.getUuid()
        );
        new Thread(() -> {
            try {
                // Code from https://stackoverflow.com/questions/45809234/change-player-skin-with-nms-in-minecraft-bukkit-spigot
                HttpsURLConnection connection = (HttpsURLConnection) new URL(String.format("https://api.mineskin.org/generate/url?url=%s", skinUrl)).openConnection();
                connection.setRequestMethod("POST");
                if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                    String reply = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
                    if(reply.contains("error")) {
                        player.sendSystemMessage(
                                new LiteralText(
                                        "§cAn error occurred when trying to fetch skin."
                                ),
                                player.getUuid()
                        );
                        return;
                    }
                    String value = reply.split("\"value\":\"")[1].split("\"")[0];
                    String signature = reply.split("\"signature\":\"")[1].split("\"")[0];

                    if(setPlayerSkin(player, value, signature)) {
                        player.sendSystemMessage(
                                new LiteralText(
                                        "§aYour skin was set successfully."
                                ),
                                player.getUuid()
                        );
                    }
                }
                else
                    player.sendSystemMessage(
                            new LiteralText(
                                    "§cAn error occurred when trying to fetch skin."
                            ),
                            player.getUuid()
                    );
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
