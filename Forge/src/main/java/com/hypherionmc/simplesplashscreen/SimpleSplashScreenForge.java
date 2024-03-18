package com.hypherionmc.simplesplashscreen;

import com.hypherionmc.simplesplashscreen.client.config.SimpleSplashScreenConfigGui;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;

/**
 * @author HypherionSA
 * Main Forge Entrypoint
 */
@Mod("simplesplashscreen")
public class SimpleSplashScreenForge {

    public SimpleSplashScreenForge() {
        if (FMLLoader.getDist().isClient()) {
            ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> SimpleSplashScreenConfigGui.getConfigScreen(parent)));
        }
    }

}