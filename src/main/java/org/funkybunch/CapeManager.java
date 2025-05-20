package org.funkybunch.capes;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CapeManager {
    private static CapeManager instance;
    
    private final Map<String, Identifier> usernameToCape = new HashMap<>();
    private String configUrl;
    private String texturesUrl;
    
    private CapeManager() {
        // Private constructor for singleton
    }
    
    public static CapeManager getInstance() {
        if (instance == null) {
            instance = new CapeManager();
        }
        return instance;
    }
    
    public void initialize(String configUrl, String texturesUrl) {
        this.configUrl = configUrl;
        this.texturesUrl = texturesUrl;
        reloadConfig();
    }
    
    public Identifier getCapeTexture(String username) {
        return usernameToCape.get(username.toLowerCase());
    }
    
    public boolean hasCustomCape(PlayerEntity player) {
        String username = player.getName().getString().toLowerCase();
        return usernameToCape.containsKey(username);
    }
    
    public void reloadConfig() {
        CompletableFuture.runAsync(() -> {
            try {
                String configJson = fetchStringFromUrl(configUrl);
                parseConfig(configJson);
            } catch (Exception e) {
                Capes.LOGGER.error("Failed to load cape configuration from GitHub", e);
            }
        });
    }
    
    private void parseConfig(String configJson) {
        try {
            Gson gson = new Gson();
            JsonObject config = gson.fromJson(configJson, JsonObject.class);
            
            // Clear current capes
            usernameToCape.clear();
            
            // Parse cape configurations
            JsonArray capeEntries = config.getAsJsonArray("capes");
            for (JsonElement element : capeEntries) {
                JsonObject capeEntry = element.getAsJsonObject();
                String capeId = capeEntry.get("id").getAsString();
                String textureFile = capeEntry.get("texture").getAsString();
                
                // Load cape texture
                Identifier capeTexture = loadCapeTexture(capeId, textureFile);
                
                // Assign cape to users
                JsonArray users = capeEntry.getAsJsonArray("users");
                for (JsonElement userElement : users) {
                    String username = userElement.getAsString().toLowerCase();
                    usernameToCape.put(username, capeTexture);
                    Capes.LOGGER.debug("Added cape for user: " + username);
                }
            }
            
            Capes.LOGGER.info("Successfully loaded cape configuration for " + usernameToCape.size() + " users");
        } catch (Exception e) {
            Capes.LOGGER.error("Error parsing cape configuration", e);
        }
    }
    
    private Identifier loadCapeTexture(String capeId, String textureFile) {
        try {
            String textureUrl = texturesUrl + textureFile;
            Capes.LOGGER.debug("Loading cape texture from: " + textureUrl);
            
            byte[] textureBytes = fetchBytesFromUrl(textureUrl);
            
            // Create texture from bytes
            NativeImage image = NativeImage.read(textureBytes);
            NativeImageBackedTexture texture = new NativeImageBackedTexture(image);
            
            // Register texture
            Identifier textureId = new Identifier(Capes.MOD_ID, "capes/" + capeId);
            MinecraftClient.getInstance().getTextureManager().registerTexture(textureId, texture);
            
            return textureId;
        } catch (Exception e) {
            Capes.LOGGER.error("Failed to load cape texture: " + textureFile, e);
            return null;
        }
    }
    
    private String fetchStringFromUrl(String url) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        
        try (InputStream inputStream = connection.getInputStream()) {
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        }
    }
    
    private byte[] fetchBytesFromUrl(String url) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        
        try (InputStream inputStream = connection.getInputStream()) {
            return IOUtils.toByteArray(inputStream);
        }
    }
}