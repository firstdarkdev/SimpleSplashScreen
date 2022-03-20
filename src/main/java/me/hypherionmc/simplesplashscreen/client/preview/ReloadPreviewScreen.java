package me.hypherionmc.simplesplashscreen.client.preview;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ResourceLoadProgressGui;

public class ReloadPreviewScreen extends ResourceLoadProgressGui {

    public ReloadPreviewScreen(long durationMs, Runnable onDone) {
        super(Minecraft.getInstance(), new PreviewResourceReloader(durationMs, onDone), optional -> { }, true);
    }

}
