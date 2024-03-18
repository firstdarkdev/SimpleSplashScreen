package com.hypherionmc.simplesplashscreen;

import com.hypherionmc.simplesplashscreen.client.config.SimpleSplashScreenConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * @author HypherionSA
 * Ah, the common entry point for the Mod. Shared across modloaders
 */
public class SimpleSplashScreenCommon {

    public static final Logger LOGGER = LogManager.getLogger();
    public static SimpleSplashScreenConfig CS_CONFIG;
    public static File CONFIG_PATH = new File("config/simplesplashscreen/");
    public static boolean initDone = false;

    private static final Path[] textures = new Path[] {
            Paths.get(CONFIG_PATH + "/background.png"),
            Paths.get(CONFIG_PATH + "/mojangstudios.png"),
            Paths.get(CONFIG_PATH + "/mojank.png"),
            Paths.get(CONFIG_PATH + "/progressbar.png"),
            Paths.get(CONFIG_PATH + "/progressbar_background.png")
    };

    public static void init() {
        // Register the Config
        LOGGER.info("Registering Config...");
        AutoConfig.register(SimpleSplashScreenConfig.class, JanksonConfigSerializer::new);
        CS_CONFIG = AutoConfig.getConfigHolder(SimpleSplashScreenConfig.class).getConfig();

        // Set up the default textures
        if (!CONFIG_PATH.exists()) {
            LOGGER.info("Creating default Resources...");
            boolean ignored = CONFIG_PATH.mkdir();

            for (Path path : textures) {
                try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path.getFileName().toString())) {
                    Files.copy(is, path, StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    LOGGER.error("Failed to load resource " + path.getFileName().toString(), e);
                }
            }
        }

        // Check that all texture names are lowercase
        CS_CONFIG.textures.Aspect1to1Logo = renameFiles(CS_CONFIG.textures.Aspect1to1Logo);
        CS_CONFIG.textures.BackgroundTexture = renameFiles(CS_CONFIG.textures.BackgroundTexture);
        CS_CONFIG.textures.BossBarTexture = renameFiles(CS_CONFIG.textures.BossBarTexture);
        CS_CONFIG.textures.CustomBarTexture = renameFiles(CS_CONFIG.textures.CustomBarTexture);
        CS_CONFIG.textures.CustomBarBackgroundTexture = renameFiles(CS_CONFIG.textures.CustomBarBackgroundTexture);
        CS_CONFIG.textures.MojangLogo = renameFiles(CS_CONFIG.textures.MojangLogo);
    }

    private static String renameFiles(String file) {
        File tmpfile = new File(CONFIG_PATH, file);
        if (tmpfile.exists()) {
            return tmpfile.renameTo(new File(CONFIG_PATH, file.toLowerCase())) ? file.toLowerCase() : file;
        }
        return file;
    }
}
