package org.samo_lego.fabrictailor.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.properties.Property;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import java.awt.image.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;


public class SkinFetcher {

    /**
     * Gets skin data from file.
     *
     * @param skinFilePath file path
     * @param useSlim whether slim format should be used
     * @return property containing skin value and signature if successful, otherwise null.
     */
    public static Optional<Property> setSkinFromFile(String skinFilePath, boolean useSlim) {
        Logging.debug("Fetching skin from file: " + skinFilePath);
        File skinFile = new File(skinFilePath);
        try (FileInputStream input = new FileInputStream(skinFile)) {
            var fileType = input.read();
            Logging.debug("Checking file type: " + fileType);
            if (fileType == 137) {
                // Check image dimensions
                BufferedImage image = ImageIO.read(skinFile);
                if (image.getWidth() != 64 || (image.getHeight() != 64 && image.getHeight() != 32)) {
                    Logging.error("Image dimensions are not 64x64 or 32x64! The actual format is: " + image.getWidth() + "x" + image.getHeight());
                    return Optional.empty();
                }
                
                try {
                    String reply = urlRequest(URI.create("https://api.mineskin.org/v2/generate").toURL(), false, skinFile, null, useSlim ? "slim" : "classic");
                    return getSkinFromReply(reply);
                } catch (IOException e) {
                    // Error uploading
                    Logging.error(e.getMessage());
                }
            }
        } catch (IOException e) {
            // Not an image
            Logging.error(e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Sets skin setting from the provided URL.
     *
     * @param skinUrl string url of the skin
     * @return property containing skin value and signature if successful, otherwise null.
     */
    public static Optional<Property> fetchSkinByUrl(String skinUrl, boolean useSlim) {
        Logging.debug("Fetching skin from URL: " + skinUrl);
        try {
            String reply = urlRequest(URI.create("https://api.mineskin.org/v2/generate").toURL(), false, null, skinUrl, useSlim ? "slim" : "classic");
            return getSkinFromReply(reply);
        } catch (IOException e) {
            Logging.error(e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Sets skin by playername.
     *
     * @param playername name of the player who has the skin wanted
     * @return property containing skin value and signature if successful, otherwise null.
     */
    public static Optional<Property> fetchSkinByName(String playername) {
        Logging.debug("Fetching Mojang skin of player: " + playername);
        try {
            String reply = urlRequest(URI.create("https://api.mojang.com/users/profiles/minecraft/" + playername).toURL(), true, null);

            if (reply == null || !reply.contains("id")) {
                Logging.debug("Mojang skin not found, trying via proxy.");
                reply = urlRequest(URI.create(String.format("http://skinsystem.ely.by/textures/signed/%s.png?proxy=true", playername)).toURL(), false, null);
            } else {
                String uuid = JsonParser.parseString(reply).getAsJsonObject().get("id").getAsString();
                Logging.debug("Mojang skin found. UUID: " + uuid);
                reply = urlRequest(URI.create("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false").toURL(), true, null);
            }
            return getSkinFromReply(reply);
        } catch (IOException e) {
            Logging.error(e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Sets skin from reply that was got from API.
     * Used internally only.
     *
     * @param reply API reply
     * @return property containing skin value and signature if successful, otherwise null.
     */
    protected static Optional<Property> getSkinFromReply(String reply) {
        Logging.debug("Parsing skin reply: " + reply);
        if (reply == null || reply.contains("error") || reply.isEmpty()) {
            return Optional.empty();
        }

        String value = reply.split("\"value\":\"")[1].split("\"")[0];
        String signature = reply.split("\"signature\":\"")[1].split("\"")[0];

        return Optional.of(new Property(TailoredPlayer.PROPERTY_TEXTURES, value, signature));
    }

    private static String urlRequest(URL url, boolean useGetMethod, File image) throws IOException {
        return urlRequest(url, useGetMethod, image, null, "classic");
    }

    /**
     * Gets reply from a skin website.
     * Used internally only.
     *
     * @param url url of the website
     * @param useGetMethod whether to use GET method instead of POST
     * @param image image to upload, otherwise null
     * @return reply from website as string
     * @throws IOException IOException is thrown when connection fails for some reason.
     */
    private static String urlRequest(URL url, boolean useGetMethod, File image, String skinUrl, String variant) throws IOException {
        URLConnection connection = url.openConnection();

        String reply = null;

        if (connection instanceof HttpsURLConnection httpsConnection) {
            httpsConnection.setUseCaches(false);
            httpsConnection.setDoOutput(true);
            httpsConnection.setDoInput(true);
            httpsConnection.setRequestMethod(useGetMethod ? "GET" : "POST");

            if (image != null) {
                // Do a POST request
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
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(image.getName()).append("\"");
                if (variant != null)
                    writer.append("; variant=\"").append(variant).append("\"");
                writer.append(LINE);
                writer.append("Content-Type: image/png").append(LINE);
                writer.append("Content-Transfer-Encoding: binary").append(LINE);
                writer.append(LINE);
                writer.flush();

                byte[] fileBytes = Files.readAllBytes(image.toPath());
                outputStream.write(fileBytes, 0, fileBytes.length);

                outputStream.flush();
                writer.append(LINE);
                writer.flush();

                writer.append("--").append(boundary).append("--").append(LINE);
                writer.close();
            }

            if (skinUrl != null) {
                // Do a POST request
                httpsConnection.setRequestProperty("Content-Type", "application/json");
                httpsConnection.setRequestProperty("User-Agent", "User-Agent");
                OutputStream outputStream = httpsConnection.getOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));

                JsonObject json = new JsonObject();
                json.addProperty("url", skinUrl);
                json.addProperty("variant", variant);

                writer.write(json.toString());
                writer.flush();
                writer.close();
            }

            if (httpsConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                reply = getContent(connection);
            }
            httpsConnection.disconnect();
        } else {
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
                Scanner scanner = new Scanner(isr)
        ) {
            StringBuilder reply = new StringBuilder();
            while (scanner.hasNextLine()) {
                String line = scanner.next();
                if (line.trim().isEmpty())
                    continue;
                reply.append(line);
            }

            return reply.toString();
        }
    }
}
