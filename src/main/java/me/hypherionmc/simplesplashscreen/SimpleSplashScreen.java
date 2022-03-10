package me.hypherionmc.simplesplashscreen;

import me.hypherionmc.simplesplashscreen.client.config.SimpleSplashScreenConfig;
import me.hypherionmc.simplesplashscreen.client.config.SimpleSplashScreenConfigGui;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Mod("simplesplashscreen")
public class SimpleSplashScreen {

    public static final Logger LOGGER = LogManager.getLogger();

    public static SimpleSplashScreenConfig CS_CONFIG;
    public static File CONFIG_PATH = new File(FMLPaths.CONFIGDIR.get() + "/simplesplashscreen");

    private static final Path[] textures = new Path[] {
        Paths.get(CONFIG_PATH + "/background.png"),
        Paths.get(CONFIG_PATH + "/mojangstudios.png"),
        Paths.get(CONFIG_PATH + "/mojank.png"),
        Paths.get(CONFIG_PATH + "/progressbar.png"),
        Paths.get(CONFIG_PATH + "/progressbar_background.png")
    };

    public SimpleSplashScreen() {
        LOGGER.info("Registering Config...");
        AutoConfig.register(SimpleSplashScreenConfig.class, JanksonConfigSerializer::new);
        CS_CONFIG = AutoConfig.getConfigHolder(SimpleSplashScreenConfig.class).getConfig();

        if (!CONFIG_PATH.exists()) {
            LOGGER.info("Creating default Resources...");
            CONFIG_PATH.mkdir();

            for (Path path : textures) {
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path.getFileName().toString());
                try {
                    Files.copy(is, path, StandardCopyOption.REPLACE_EXISTING);
                    is.close();
                } catch (Exception e) {
                    LOGGER.error("Failed to load resource " + path.getFileName().toString());
                    e.printStackTrace();
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

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
    }

    public void clientSetup(FMLClientSetupEvent event) {
        ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class, () -> new ConfigGuiHandler.ConfigGuiFactory((client, parent) -> SimpleSplashScreenConfigGui.getConfigScreen(parent)));
    }

    private String renameFiles(String file) {
        File tmpfile = new File(CONFIG_PATH + "/" + file);
        if (tmpfile.exists()) {
            return tmpfile.renameTo(new File(CONFIG_PATH + "/" + file.toLowerCase())) ? file.toLowerCase() : file;
        }
        return file;
    }
}
