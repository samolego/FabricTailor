package org.samo_lego.fabrictailor.command;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;
import org.samo_lego.fabrictailor.compatibility.TaterzensCompatibility;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.command.argument.MessageArgumentType.getMessage;
import static net.minecraft.command.argument.MessageArgumentType.message;
import static net.minecraft.server.command.CommandManager.literal;
import static org.samo_lego.fabrictailor.FabricTailor.THREADPOOL;

public class SkinCommand {
    public static LiteralCommandNode<ServerCommandSource> skinNode;
    private static final boolean TATERZENS_LOADED;
    private static final String SET_SKIN_ATTEMPT = "Trying to set the skin ... Please wait.";

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        skinNode = dispatcher.register(literal("skin")
                .then(literal("set")
                        .then(literal("URL")
                                .then(literal("classic")
                                        .then(CommandManager.argument("skin URL", message())
                                                .executes(ctx ->
                                                        fetchSkinByUrl(
                                                                ctx.getSource(),
                                                                getMessage(ctx, "skin URL").getString(),
                                                                false
                                                        )
                                                )
                                        )
                                )
                                .then(literal("slim")
                                        .then(CommandManager.argument("skin URL", message())
                                                .executes(ctx ->
                                                        fetchSkinByUrl(
                                                                ctx.getSource(),
                                                                getMessage(ctx, "skin URL").getString(),
                                                                true
                                                        )
                                                )
                                        )
                                )
                                .executes(ctx -> {
                                    ctx.getSource().sendError(
                                            new LiteralText(
                                                    "You have to provide URL and variant of the skin you want."
                                            ).formatted(Formatting.RED)
                                    );
                                    return 1;
                                })
                        )
                        .then(literal("upload")
                                .then(literal("classic")
                                        .then(CommandManager.argument("skin file path", message())
                                                .executes(ctx -> setSkinFromFile(
                                                        ctx.getSource(),
                                                        getMessage(ctx, "skin file path").getString(),
                                                        false
                                                        )
                                                )
                                        )
                                )
                                .then(literal("slim")
                                        .then(CommandManager.argument("skin file path", message())
                                                .executes(ctx -> setSkinFromFile(
                                                        ctx.getSource(),
                                                        getMessage(ctx, "skin file path").getString(),
                                                        true
                                                        )
                                                )
                                        )
                                )
                                .executes(ctx -> {
                                    ctx.getSource().sendError(
                                            new LiteralText(
                                                    "You have to provide file path and variant of the skin you want."
                                            ).formatted(Formatting.RED)
                                    );
                                    return 1;
                                })
                        )
                        .then(literal("player")
                                .then(CommandManager.argument("playername", greedyString())
                                        .executes(ctx -> fetchSkinByName(ctx.getSource(), getString(ctx, "playername"), true))
                                )
                                .executes(ctx -> {
                                    ctx.getSource().sendError(
                                            new LiteralText(
                                                    "You have to provide player's name."
                                            ).formatted(Formatting.RED)
                                    );
                                    return 1;
                                })
                        )
                        .executes(ctx -> {
                            ctx.getSource().sendError(
                                    new LiteralText(
                                            "You have to provide URL, player's name or file of the skin you want."
                                    ).formatted(Formatting.RED)
                            );
                            return 1;
                        })
                )
                .then(literal("clear").executes(context -> clearSkin(context.getSource())))
        );
    }

    private static int clearSkin(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayer();

        if(((TailoredPlayer) player).setSkin("", "", true)) {
            player.sendMessage(new LiteralText(
                            "Your skin was cleared successfully."
                    ).formatted(Formatting.GREEN),
                    false
            );
            return 1;
        }
        player.sendMessage(new LiteralText(
                        "An error occurred when trying to clear your skin."
                ).formatted(Formatting.RED),
                false
        );
        return 0;
    }

    /**
     * Gets the file.
     *
     * @param src src that is changing the skin.
     * @param skinFilePath file path
     * @param useSlim whether slim format should be used
     * @return 1 if image is valid, otherwise 0
     */
    private static int setSkinFromFile(ServerCommandSource src, String skinFilePath, boolean useSlim) {
        if(src.getMinecraftServer().isDedicated()) {
            src.sendFeedback(new LiteralText(
                    "FabricTailor mod is running in server environment.\n" +
                            "Make sure the path points to the skin file on the SERVER."
                    ).formatted(Formatting.GOLD),
                    false
            );
        }

        File skinFile = new File(skinFilePath);
        try (FileInputStream input = new FileInputStream(skinFile)) {
            if(input.read() == 137) {
                src.sendFeedback(new LiteralText(
                                "Uploading skin. Please wait."
                        ).formatted(Formatting.GOLD),
                        false
                );
                THREADPOOL.submit(() -> {
                    try {
                        String reply = urlRequest(new URL("https://api.mineskin.org/generate/upload?model=" + (useSlim ? "slim" : "steve")), skinFile);
                        setSkinFromReply(reply, src.getPlayer(), true);
                    } catch (IOException | CommandSyntaxException e) {
                        src.sendError(
                                new LiteralText(
                                        "A problem occurred when trying to upload the skin."
                                ).formatted(Formatting.RED)
                        );
                    }
                });
                return 1;
            }
        } catch (IOException ignored) {
            // Not an image
        }
        src.sendError(new LiteralText("The provided file is not a valid PNG image.").formatted(Formatting.RED));
        return 0;
    }

    /**
     * Sets skin setting from the provided URL.
     *
     * @param src executor of the command
     * @param skinUrl string url of the skin
     * @return 1
     */
    public static int fetchSkinByUrl(ServerCommandSource src, String skinUrl, boolean useSlim) {
        src.sendFeedback(new LiteralText(SET_SKIN_ATTEMPT).formatted(Formatting.AQUA), false);
        THREADPOOL.submit(() -> {
            try {
                URL url = new URL(String.format("https://api.mineskin.org/generate/url?url=%s&model=%s", skinUrl, useSlim ? "slim" : "steve"));
                String reply = urlRequest(url, null);
                setSkinFromReply(reply, src.getPlayer(), true);
            } catch (IOException | CommandSyntaxException e) {
                e.printStackTrace();
                src.sendError(
                        new LiteralText(
                                "Malformed url!"
                        ).formatted(Formatting.RED)
                );
            }
        });
        return 0;
    }

    /**
     * Sets skin by playername.
     *
     * @param src src changing the skin
     * @param playername name of the player who has the skin wanted
     * @param giveFeedback whether player should receive feedback if skin setting was successful
     * @return 0
     */
    public static int fetchSkinByName(ServerCommandSource src, String playername, boolean giveFeedback) throws CommandSyntaxException {
        if(giveFeedback)
            src.sendFeedback(
                    new LiteralText(SET_SKIN_ATTEMPT).formatted(Formatting.AQUA),
                    false
            );
        ServerPlayerEntity player = src.getPlayer();

        THREADPOOL.submit(() -> {
            // If user has no skin data

            // Try to get Mojang skin first
            GameProfile profile = new GameProfile(null, playername);
            SkullBlockEntity.loadProperties(profile, gameProfile -> {
                PropertyMap propertyMap = gameProfile.getProperties();
                if(propertyMap.containsKey("textures")) {
                    Property textures = propertyMap.get("textures").iterator().next();
                    String value = textures.getValue();
                    String signature = textures.getSignature();
                    if(
                            TATERZENS_LOADED && TaterzensCompatibility.setTaterzenSkin(player, value, signature) ||
                                    (((TailoredPlayer) player).setSkin(value, signature, true) && giveFeedback)
                    ) {
                        player.sendMessage(
                                new LiteralText(
                                        "Skin was set successfully."
                                ).formatted(Formatting.GREEN),
                                false
                        );
                    }
                } else {
                    // Getting skin data from ely.by api, since it can be used with usernames
                    // it also includes mojang skins
                    String reply = null;
                    try {
                        reply = urlRequest(new URL(String.format("http://skinsystem.ely.by/textures/signed/%s.png?proxy=true", playername)), null);
                    } catch(IOException e) {
                        if(giveFeedback)
                            src.sendError(
                                    new LiteralText(
                                            "This player doesn't seem to have any skins saved."
                                    ).formatted(Formatting.RED)
                            );

                    }
                    setSkinFromReply(reply, player, giveFeedback);
                }
            });
        });
        return 0;
    }

    /**
     * Sets skin from reply that was got from API.
     * Used internally only.
     *
     * @param reply API reply
     * @param player player to send message to.
     * @param giveFeedback whether feedback should be sent to player.
     */
    private static void setSkinFromReply(String reply, ServerPlayerEntity player, boolean giveFeedback) {
        if(reply == null || (reply.contains("error") && giveFeedback)) {
            if(giveFeedback)
                player.sendMessage(
                        new LiteralText(
                                "An error occurred when trying to fetch skin."
                        ).formatted(Formatting.RED),
                        false
                );
            return;
        }

        String value = reply.split("\"value\":\"")[1].split("\"")[0];
        String signature = reply.split("\"signature\":\"")[1].split("\"")[0];

        if(
                TATERZENS_LOADED && TaterzensCompatibility.setTaterzenSkin(player, value, signature) ||
                (((TailoredPlayer) player).setSkin(value, signature, true) && giveFeedback)
        ) {
            player.sendMessage(
                    new LiteralText(
                            "Skin was set successfully."
                    ).formatted(Formatting.GREEN),
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

        if(connection instanceof HttpsURLConnection) {
            HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
            httpsConnection.setUseCaches(false);
            httpsConnection.setDoOutput(true);
            httpsConnection.setDoInput(true);
            httpsConnection.setRequestMethod("POST");
            if(image != null) {
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
            }

            if(httpsConnection.getResponseCode() == HttpURLConnection.HTTP_OK)
                reply = getContent(connection);
            httpsConnection.disconnect();
        }
        else {
            reply = getContent(connection);
        }
        return reply;
    }

    /**
     * Reads repsonse from API.
     * Used just to avoid duplicate code.
     *
     * @param connection connection where to take output stream from
     * @return API reply as String
     * @throws IOException exception when something went wrong
     */
    private static String getContent(URLConnection connection) throws IOException {
        try (
                InputStream is = connection.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
        ) {
            return br.readLine();
        }
    }

    static {
        TATERZENS_LOADED = FabricLoader.getInstance().isModLoaded("taterzens");
    }
}
