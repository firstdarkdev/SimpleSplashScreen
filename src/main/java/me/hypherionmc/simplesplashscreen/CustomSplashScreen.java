package me.hypherionmc.simplesplashscreen;

import me.hypherionmc.simplesplashscreen.config.CustomSplashScreenConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Mod("simplesplashscreen")
public class CustomSplashScreen {

    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public static CustomSplashScreenConfig CS_CONFIG;
    public static File CONFIG_PATH = new File(FMLPaths.CONFIGDIR.get() + "/simplesplashscreen");
    private static final Path BackgroundTexture = Paths.get(CONFIG_PATH + "/background.png");
    private static final Path MojangTexture = Paths.get(CONFIG_PATH + "/mojangstudios.png");
    private static final Path MojankTexture = Paths.get(CONFIG_PATH + "/mojank.png");
    private static final Path ProgressBarTexture = Paths.get(CONFIG_PATH + "/progressbar.png");
    private static final Path ProgressBarBackgroundTexture = Paths.get(CONFIG_PATH + "/progressbar_background.png");

    public CustomSplashScreen() {
        AutoConfig.register(CustomSplashScreenConfig.class, JanksonConfigSerializer::new);
        CS_CONFIG = AutoConfig.getConfigHolder(CustomSplashScreenConfig.class).getConfig();

        if (!CONFIG_PATH.exists()) { // Run when config directory is nonexistant //
            CONFIG_PATH.mkdir(); // Create our custom config directory //

            // Open Input Streams for copying the default textures to the config directory //
            InputStream background = Thread.currentThread().getContextClassLoader().getResourceAsStream("background.png");
            InputStream mojangstudios = Thread.currentThread().getContextClassLoader().getResourceAsStream("mojangstudios.png");
            InputStream mojank = Thread.currentThread().getContextClassLoader().getResourceAsStream("mojank.png");
            InputStream progressbar = Thread.currentThread().getContextClassLoader().getResourceAsStream("progressbar.png");
            InputStream progressbarBG = Thread.currentThread().getContextClassLoader().getResourceAsStream("progressbar_background.png");

            try {
                // Copy the default textures into the config directory //
                Files.copy(background, BackgroundTexture, StandardCopyOption.REPLACE_EXISTING);
                Files.copy(mojangstudios, MojangTexture, StandardCopyOption.REPLACE_EXISTING);
                Files.copy(mojank, MojankTexture, StandardCopyOption.REPLACE_EXISTING);
                Files.copy(progressbar, ProgressBarTexture, StandardCopyOption.REPLACE_EXISTING);
                Files.copy(progressbarBG, ProgressBarBackgroundTexture, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
    }

    public void clientSetup(FMLClientSetupEvent event) {
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> (mc, screen) -> AutoConfig.getConfigScreen(CustomSplashScreenConfig.class, screen).get());
    }

}
