package org.samo_lego.fabrictailor.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.samo_lego.fabrictailor.TailoredPlayer;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;
import java.util.UUID;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.command.argument.MessageArgumentType.getMessage;
import static net.minecraft.command.argument.MessageArgumentType.message;
import static org.samo_lego.fabrictailor.FabricTailor.THREADPOOL;

public class SkinCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(CommandManager.literal("skin")
            .then(CommandManager.literal("set")
                .then(CommandManager.literal("URL")
                        .then(CommandManager.argument("skin URL", message())
                                .executes(ctx ->
                                        fetchSkinByUrl(
                                                (ServerPlayerEntity) ctx.getSource().getEntityOrThrow(),
                                                getMessage(ctx, "skin URL").getString()
                                        )
                                )
                        )
                        .executes(ctx -> {
                            ServerPlayerEntity player = (ServerPlayerEntity) ctx.getSource().getEntityOrThrow();
                            player.sendMessage(
                                    new LiteralText(
                                            "§6You have to provide URL of the skin you want."
                                    ),
                                    false
                            );
                            return 1;
                        })
                )
                .then(CommandManager.literal("upload")
                        .then(CommandManager.argument("skin file path", message())
                                .executes(ctx -> setSkinFromFile(
                                        (ServerPlayerEntity) ctx.getSource().getEntityOrThrow(),
                                        getMessage(ctx, "skin file path").getString()
                                        )
                                )

                        )
                        .executes(ctx -> {
                            ServerPlayerEntity player = (ServerPlayerEntity) ctx.getSource().getEntityOrThrow();
                            player.sendMessage(
                                    new LiteralText(
                                            "§6You have to provide file path of the skin you want."
                                    ),
                                    false
                            );
                            return 1;
                        })
                )
                .then(CommandManager.literal("player")
                        .then(CommandManager.argument("playername", greedyString())
                            .executes(ctx -> fetchSkinByName((ServerPlayerEntity) ctx.getSource().getEntityOrThrow(), getString(ctx, "playername"), true))
                        )
                        .executes(ctx -> {
                            ServerPlayerEntity player = (ServerPlayerEntity) ctx.getSource().getEntityOrThrow();
                            player.sendMessage(
                                    new LiteralText(
                                            "§6You have to provide player's name."
                                    ),
                                    false
                            );
                            return 1;
                        })
                )
                .executes(ctx -> {
                    ServerPlayerEntity player = (ServerPlayerEntity) ctx.getSource().getEntityOrThrow();
                    player.sendMessage(
                            new LiteralText(
                                    "§6You have to provide URL or player's name of the skin you want."
                            ),
                            false
                    );
                    return 1;
                })
            )
            .then(CommandManager.literal("clear")
                .executes(ctx -> {
                        ServerPlayerEntity player = (ServerPlayerEntity) ctx.getSource().getEntityOrThrow();
                        if(((TailoredPlayer) player).setSkin(null, null)) {
                            player.sendMessage(new LiteralText(
                                        "§aYour skin was cleared successfully."
                                ),
                                false
                            );
                            return 1;
                        }
                        player.sendMessage(new LiteralText(
                                        "§cAn error occurred when trying to clear your skin."
                                ),
                                false
                        );
                        return 0;
                    }
                )
            )
        );
    }

    /**
     * Gets the file.
     *
     * @param player player that will have skin changed.
     * @param skinFilePath file path
     * @return 1 if image is valid, otherwise 0
     */
    private static int setSkinFromFile(ServerPlayerEntity player, String skinFilePath) {
        if(Objects.requireNonNull(player.getServer()).isDedicated()) {
            player.sendMessage(new LiteralText(
                    "§6FabricTailor mod is running in server environment.\n" +
                            "Make sure the path points to the skin file on the SERVER."
                    ),
                    false
            );
        }

        File skinFile = new File(skinFilePath);
        try (FileInputStream input = new FileInputStream(skinFile)) {
            if(input.read() == 137) {
                player.sendMessage(new LiteralText(
                                "§6Uploading skin. Please wait."
                        ),
                        false
                );
                THREADPOOL.submit(() -> {
                    try {
                        String reply = urlRequest(new URL("https://api.mineskin.org/generate/upload"), skinFile);
                        setSkinFromReply(reply, player, true);
                    } catch (IOException e) {
                        player.sendMessage(
                                new LiteralText(
                                        "§cA problem occurred when trying to upload the skin."
                                ),
                                false
                        );
                    }
                });
                return 1;
            }
        } catch (IOException ignored) {
            // Not an image
        }
        player.sendMessage(new LiteralText("§cThe provided file is not a valid PNG image."), false);
        return 0;
    }

    /**
     * Sets skin setting from the provided URL.
     *
     * @param player player who executed the command
     * @param skinUrl string url of the skin
     * @return 1
     */
    public static int fetchSkinByUrl(ServerPlayerEntity player, String skinUrl) {
        player.sendMessage(
                new LiteralText(
                        "§eTrying to set your skin ... Please wait."
                ),
                false
        );
        THREADPOOL.submit(() -> {
            try {
                String reply = urlRequest(new URL(String.format("https://api.mineskin.org/generate/url?url=%s", skinUrl)), null);
                setSkinFromReply(reply, player, true);
            } catch (IOException e) {
                player.sendMessage(
                        new LiteralText(
                                "§cMalformed url!"
                        ),
                        false
                );
            }
        });
        return 0;
    }

    /**
     * Sets skin by playername.
     *
     * @param player player to change skin for
     * @param playername name of the player who has the skin wanted
     * @param giveFeedback whether player should receive feedback if skin setting was successful
     * @return 0
     */
    public static int fetchSkinByName(ServerPlayerEntity player, String playername, boolean giveFeedback) {
        if(giveFeedback)
            player.sendMessage(
                    new LiteralText(
                            "§eTrying to set your skin ... Please wait."
                    ),
                    false
            );
        THREADPOOL.submit(() -> {
            // If user has no skin data
            // Getting skin data from ely.by api, since it can be used with usernames
            // it also includes mojang skins
            try {
                String reply = urlRequest(new URL(String.format("http://skinsystem.ely.by/textures/signed/%s.png?proxy=true", playername)), null);
                setSkinFromReply(reply, player, giveFeedback);
            } catch (IOException e) {
                if(giveFeedback)
                    player.sendMessage(
                            new LiteralText(
                                    "§cThis player doesn't seem to have any skins saved."
                            ),
                            false
                    );
            }
        });
        return 0;
    }

    /**
     * Sets skin from reply that was got from API.
     * Used internally only.
     *
     * @param reply API reply
     * @param player player to send message to.
     * @param giveFeedback whether feedback should be sent to player
     */
    private static void setSkinFromReply(String reply, ServerPlayerEntity player, boolean giveFeedback) {
        if(reply == null || (reply.contains("error") && giveFeedback)) {
            if(giveFeedback)
                player.sendMessage(
                        new LiteralText(
                                "§cAn error occurred when trying to fetch skin."
                        ),
                        false
                );
            return;
        }

        String value = reply.split("\"value\":\"")[1].split("\"")[0];
        String signature = reply.split("\"signature\":\"")[1].split("\"")[0];

        // Setting skin
        if(((TailoredPlayer) player).setSkin(value, signature) && giveFeedback) {
            player.sendMessage(
                    new LiteralText(
                            "§aYour skin was set successfully."
                    ),
                    false
            );
        }
    }

    /**
     * Gets reply from a skin website.
     * Used internally only.
     *
     * @param url url of the website
     * @param image image to upload, otherwise null
     * @return reply from website as string
     * @throws IOException IOException is thrown when connection fails for some reason.
     */
    private static String urlRequest(URL url, File image) throws IOException {
        URLConnection connection = url.openConnection();

        String reply = null;
        if(image != null) {
            HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
            httpsConnection.setUseCaches(false);
            httpsConnection.setDoOutput(true);
            httpsConnection.setDoInput(true);

            String boundary = UUID.randomUUID().toString();
            httpsConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            httpsConnection.setRequestProperty("User-Agent", "User-Agent");

            OutputStream outputStream = httpsConnection.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);

            final String LINE = "\r\n";
            writer.append("--").append(boundary).append(LINE);
            writer.append("Content-Disposition: form-data; name=\"file\"").append(LINE);
            writer.append("Content-Type: text/plain; charset=UTF-8").append(LINE);
            writer.append(LINE);
            writer.append(image.getName()).append(LINE);
            writer.flush();

            writer.append("--").append(boundary).append(LINE);
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(image.getName()).append("\"").append(LINE);
            writer.append("Content-Type: image/png").append(LINE);
            writer.append("Content-Transfer-Encoding: binary").append(LINE);
            writer.append(LINE);
            writer.flush();

            byte[] fileBytes =  Files.readAllBytes(image.toPath());
            outputStream.write(fileBytes,  0, fileBytes.length);

            outputStream.flush();
            writer.append(LINE);
            writer.flush();

            writer.append("--").append(boundary).append("--").append(LINE);
            writer.close();

            if (httpsConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                reply = new BufferedReader(new InputStreamReader(httpsConnection.getInputStream())).readLine();
                httpsConnection.disconnect();
            }
            httpsConnection.disconnect();
        }
        else {
            if(connection instanceof HttpsURLConnection) {
                HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
                httpsConnection.setRequestMethod("POST");
                if (httpsConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    reply = new BufferedReader(new InputStreamReader(httpsConnection.getInputStream())).readLine();
                }
                httpsConnection.disconnect();
            }
            else
                reply = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
        }
        return reply;
    }
}
