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
import java.net.HttpURLConnection;
import java.net.URL;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.command.argument.MessageArgumentType.getMessage;
import static net.minecraft.command.argument.MessageArgumentType.message;
import static org.samo_lego.fabrictailor.FabricTailor.THREADPOOL;
import static org.samo_lego.fabrictailor.FabricTailor.setPlayerSkin;

public class SkinCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean b) {
        dispatcher.register(CommandManager.literal("skin")
                .then(CommandManager.literal("set")
                    .then(CommandManager.literal("URL")
                            .then(CommandManager.argument("skin URL", message())
                                    .executes(ctx -> fetchSkinByUrl((ServerPlayerEntity) ctx.getSource().getEntityOrThrow(), getMessage(ctx, "skin URL").getString()))
                            )
                            .executes(ctx -> {
                                Entity player = ctx.getSource().getEntityOrThrow();
                                player.sendSystemMessage(
                                        new LiteralText(
                                                "§6You have to provide URL of the skin you want."
                                        ),
                                        player.getUuid()
                                );
                                return 1;
                            })
                    )
                    .then(CommandManager.literal("player")
                            .then(CommandManager.argument("playername", greedyString())
                                .executes(ctx -> fetchSkinByName((ServerPlayerEntity) ctx.getSource().getEntityOrThrow(), getString(ctx, "playername"), true))
                            )
                            .executes(ctx -> {
                                Entity player = ctx.getSource().getEntityOrThrow();
                                player.sendSystemMessage(
                                        new LiteralText(
                                                "§6You have to provide player's name."
                                        ),
                                        player.getUuid()
                                );
                                return 1;
                            })
                    )
                )
                .then(CommandManager.literal("clear")
                    .executes(ctx -> {
                            ServerPlayerEntity player = (ServerPlayerEntity) ctx.getSource().getEntityOrThrow();
                            if(setPlayerSkin(player, "", "")) {
                                player.sendSystemMessage(new LiteralText(
                                            "§aYour skin was cleared successfully."
                                    ),
                                    player.getUuid()
                                );
                                return 1;
                            }
                            player.sendSystemMessage(new LiteralText(
                                            "§cAn error occurred when trying to clear your skin."
                                    ),
                                    player.getUuid()
                            );
                            return 0;
                        }
                    )
                )
                .executes(ctx -> {
                    Entity player = ctx.getSource().getEntityOrThrow();
                    player.sendSystemMessage(
                        new LiteralText(
                                "§6You have to provide URL or player's name of the skin you want."
                        ),
                        player.getUuid()
                    );
                    return 1;
                })
        );
    }

    // Skin setting by URL
    public static int fetchSkinByUrl(ServerPlayerEntity player, String skinUrl) {
        player.sendSystemMessage(
                new LiteralText(
                        "§eTrying to set your skin ... Please wait."
                ),
                player.getUuid()
        );
        THREADPOOL.submit(() -> {
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
                    else {
                        player.sendSystemMessage(
                                new LiteralText(
                                        "§aA problem occurred when trying to set the skin."
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
        });
        return 0;
    }

    // Skin setting by playername
    public static int fetchSkinByName(ServerPlayerEntity player, String playername, boolean giveFeedback) {
        if(giveFeedback)
            player.sendSystemMessage(
                    new LiteralText(
                            "§eTrying to set your skin ... Please wait."
                    ),
                    player.getUuid()
            );
        THREADPOOL.submit(() -> {
            // If user has no skin data
            // Getting skin data from ely.by api, since it can be used with usernames
            // it also includes mojang skins
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL("http://skinsystem.ely.by/textures/signed/" + playername + ".png?proxy=true").openConnection();
                if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                    String reply = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
                    if (reply.contains("error") && giveFeedback) {
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

                    if(setPlayerSkin(player, value, signature) && giveFeedback) {
                        player.sendSystemMessage(
                                new LiteralText(
                                        "§aYour skin was set successfully."
                                ),
                                player.getUuid()
                        );
                    }

                }
            } catch (IOException e) {
                if(giveFeedback)
                    player.sendSystemMessage(
                            new LiteralText(
                                    "§cThis player doesn't seem to have any skins saved."
                            ),
                            player.getUuid()
                    );
            }
        });
        return 0;
    }
}
