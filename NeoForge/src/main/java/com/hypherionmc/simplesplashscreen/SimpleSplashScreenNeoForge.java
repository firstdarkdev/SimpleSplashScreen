package com.hypherionmc.simplesplashscreen;

import com.hypherionmc.simplesplashscreen.client.config.SimpleSplashScreenConfigGui;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * @author HypherionSA
 * Main NeoForge Entrypoint
 */
@Mod("simplesplashscreen")
public class SimpleSplashScreenNeoForge {

    public SimpleSplashScreenNeoForge(IEventBus modEventBus) {
       if (FMLEnvironment.dist.isClient()) {
           ModList.get().getModContainerById("simplesplashscreen").ifPresent(c -> {
               c.registerExtensionPoint(IConfigScreenFactory.class, ((minecraft, screen) -> SimpleSplashScreenConfigGui.getConfigScreen(screen)));
           });
       }
    }
}