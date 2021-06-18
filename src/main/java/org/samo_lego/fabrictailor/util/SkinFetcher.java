package org.samo_lego.fabrictailor.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.server.MinecraftServer;
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


public class SkinFetcher {

    private static final boolean TATERZENS_LOADED = FabricLoader.getInstance().isModLoaded("taterzens");;
    private static final TranslatedText SET_SKIN_ATTEMPT = new TranslatedText("command.fabrictailor.skin.set.attempt");

    public static boolean clearSkin(ServerPlayerEntity player) {
        if(((TailoredPlayer) player).setSkin("", "", true)) {
            player.sendMessage(
                    new TranslatedText("command.fabrictailor.skin.clear.success").formatted(Formatting.GREEN),
                    false
            );
            return true;
        }
        player.sendMessage(new TranslatedText("command.fabrictailor.skin.clear.error").formatted(Formatting.RED),
                false
        );
        return false;
    }

    /**
     * Gets the file.
     *
     * @param player player that is changing the skin.
     * @param skinFilePath file path
     * @param useSlim whether slim format should be used
     */
    public static void setSkinFromFile(ServerPlayerEntity player, String skinFilePath, boolean useSlim) {
        MinecraftServer server = player.getServer();
        if(server != null && server.isDedicated()) {
            player.sendMessage(
                    new TranslatedText("hint.fabrictailor.server_skin_path").formatted(Formatting.GOLD),
                    false
            );
        }

        File skinFile = new File(skinFilePath);
        try (FileInputStream input = new FileInputStream(skinFile)) {
            if(input.read() == 137) {
                player.sendMessage(new TranslatedText("command.fabrictailor.skin.please_wait").formatted(Formatting.GOLD),
                        false
                );

                    try {
                        String reply = urlRequest(new URL("https://api.mineskin.org/generate/upload?model=" + (useSlim ? "slim" : "steve")), skinFile);
                        setSkinFromReply(reply, player, true);
                    } catch (IOException e) {
                        player.sendMessage(
                                new TranslatedText("command.fabrictailor.skin.upload.failed").formatted(Formatting.RED),
                                false
                        );
                    }
                return;
            }
        } catch (IOException ignored) {
            // Not an image
        }
        player.sendMessage(new TranslatedText("error.fabrictailor.invalid_skin").formatted(Formatting.RED), false);
    }

    /**
     * Sets skin setting from the provided URL.
     *
     * @param player player to change skin for
     * @param skinUrl string url of the skin
     */
    public static void fetchSkinByUrl(ServerPlayerEntity player, String skinUrl, boolean useSlim) {
        player.sendMessage(SET_SKIN_ATTEMPT.formatted(Formatting.AQUA), false);
        try {
            URL url = new URL(String.format("https://api.mineskin.org/generate/url?url=%s&model=%s", skinUrl, useSlim ? "slim" : "steve"));
            String reply = urlRequest(url, null);
            setSkinFromReply(reply, player, true);
        } catch (IOException e) {
            e.printStackTrace();
            player.sendMessage(
                    new TranslatedText("command.fabrictailor.skin.upload.malformed_url").formatted(Formatting.RED),
                    false
            );
        }
    }

    /**
     * Sets skin by playername.
     *
     * @param player player changing the skin
     * @param playername name of the player who has the skin wanted
     * @param giveFeedback whether player should receive feedback if skin setting was successful
     */
    public static void fetchSkinByName(ServerPlayerEntity player, String playername, boolean giveFeedback) {
        if(giveFeedback)
            player.sendMessage(
                    SET_SKIN_ATTEMPT.formatted(Formatting.AQUA),
                    false
            );

        // If user has no skin data
        // Try to get Mojang skin first
        GameProfile profile = new GameProfile(player.getUuid(), playername);

        SkullBlockEntity.loadProperties(profile, gameProfile -> {
            PropertyMap propertyMap = gameProfile.getProperties();

            // We check if player is online as well as there is
            // edge case when skin for your own self doesn't get fetched (#30)
            if(propertyMap.containsKey("textures")) {
                Property textures = propertyMap.get("textures").iterator().next();
                String value = textures.getValue();
                String signature = textures.getSignature();
                if(
                        !value.equals("") && !signature.equals("") &&
                        (
                            TATERZENS_LOADED && TaterzensCompatibility.setTaterzenSkin(player, value, signature) ||
                            (((TailoredPlayer) player).setSkin(value, signature, true) && giveFeedback)
                        )
                ) {
                    player.sendMessage(
                            new TranslatedText("command.fabrictailor.skin.set.success").formatted(Formatting.GREEN),
                            false
                    );
                    return;
                }
            }
            // Getting skin data from ely.by api, since it can be used with usernames
            // it also includes mojang skins
            String reply = null;
            try {
                reply = urlRequest(new URL(String.format("http://skinsystem.ely.by/textures/signed/%s.png?proxy=true", playername)), null);
            } catch(IOException e) {
                if(giveFeedback)
                    player.sendMessage(
                            new LiteralText("\n" + e.getMessage()).formatted(Formatting.RED),
                            false
                    );
            }
            setSkinFromReply(reply, player, giveFeedback);
        });
    }

    /**
     * Sets skin from reply that was got from API.
     * Used internally only.
     *
     * @param reply API reply
     * @param player player to send message to.
     * @param giveFeedback whether feedback should be sent to player.
     */
    protected static void setSkinFromReply(String reply, ServerPlayerEntity player, boolean giveFeedback) {
        if(reply == null || (reply.contains("error") && giveFeedback)) {
            if(giveFeedback)
                player.sendMessage(
                        new TranslatedText("command.fabrictailor.skin.set.error")
                            .formatted(Formatting.RED),
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
                    new TranslatedText("command.fabrictailor.skin.set.success")
                            .formatted(Formatting.GREEN),
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

        if(connection instanceof HttpsURLConnection httpsConnection) {
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
     * Reads response from API.
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
}
