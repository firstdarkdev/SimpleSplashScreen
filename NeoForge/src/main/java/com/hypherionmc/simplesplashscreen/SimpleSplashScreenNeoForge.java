package com.hypherionmc.simplesplashscreen;

import com.hypherionmc.simplesplashscreen.client.config.SimpleSplashScreenConfigGui;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.ConfigScreenHandler;

/**
 * @author HypherionSA
 * Main NeoForge Entrypoint
 */
@Mod("simplesplashscreen")
public class SimpleSplashScreenNeoForge {

    public SimpleSplashScreenNeoForge(IEventBus modEventBus) {
       if (FMLEnvironment.dist.isClient()) {
           ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> SimpleSplashScreenConfigGui.getConfigScreen(parent)));
       }
    }
}