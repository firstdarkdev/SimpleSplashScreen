package com.hypherionmc.simplesplashscreen.client.preview;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;

/**
 * @author HypherionSA
 * A dummy Loading Screen to preview the customization before applying changes
 */
public class ReloadPreviewScreen extends LoadingOverlay {

    public ReloadPreviewScreen(long durationMs, Runnable onDone) {
        super(Minecraft.getInstance(), new PreviewResourceReloader(durationMs, onDone), optional -> { }, true);
    }

}
