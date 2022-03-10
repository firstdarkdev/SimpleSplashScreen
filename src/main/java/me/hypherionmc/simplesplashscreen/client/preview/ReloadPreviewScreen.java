package me.hypherionmc.simplesplashscreen.client.preview;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;

public class ReloadPreviewScreen extends LoadingOverlay {

    public ReloadPreviewScreen(long durationMs, Runnable onDone) {
        super(Minecraft.getInstance(), new PreviewResourceReloader(durationMs, onDone), optional -> { }, true);
    }

}
