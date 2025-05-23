package org.funkybunch.capes;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Capes implements ModInitializer, ClientModInitializer {
    public static final String MOD_ID = "capes";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    private static final String GITHUB_CONFIG_URL = "https://raw.githubusercontent.com/YOUR_USERNAME/YOUR_REPO/main/config.json";
    private static final String GITHUB_TEXTURES_URL = "https://raw.githubusercontent.com/thefunkybunch/.github/refs/heads/main/resources/imgs/";
    
    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Custom Capes Mod");
    }
    
    @Override
    @Environment(EnvType.CLIENT)
    public void onInitializeClient() {
        LOGGER.info("Initializing Custom Capes Client");
        
        // Initialize cape manager on client startup
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            CapeManager.getInstance().initialize(GITHUB_CONFIG_URL, GITHUB_TEXTURES_URL);
        });
        
        // Register tick event to reload capes periodically (every 5 minutes)
        MinecraftClient.getInstance().execute(() -> {
            new CapeReloadTask().start();
        });
    }
    
    // Inner class for periodic cape config reloading
    private static class CapeReloadTask extends Thread {
        private static final long RELOAD_INTERVAL = 5 * 60 * 1000; // 5 minutes in milliseconds
        
        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    Thread.sleep(RELOAD_INTERVAL);
                    LOGGER.info("Reloading cape configuration");
                    CapeManager.getInstance().reloadConfig();
                } catch (InterruptedException e) {
                    LOGGER.error("Cape reload task interrupted", e);
                    break;
                } catch (Exception e) {
                    LOGGER.error("Error reloading cape configuration", e);
                }
            }
        }
    }
}
