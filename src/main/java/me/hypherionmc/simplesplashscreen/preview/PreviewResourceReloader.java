package me.hypherionmc.simplesplashscreen.preview;

import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;

import java.util.concurrent.CompletableFuture;

public class PreviewResourceReloader implements ReloadInstance {

    protected final long start;
    protected final long duration;
    protected final Runnable onDone;

    public PreviewResourceReloader(long durationMs, Runnable onDone) {
        start = System.currentTimeMillis();
        duration = durationMs;
        this.onDone = onDone;
    }

    @Override
    public CompletableFuture<Unit> done() {
        if (onDone != null) {
            onDone.run();
        }
        return null;
    }

    @Override
    public float getActualProgress() {
        return Mth.clamp(
       (float) (System.currentTimeMillis() - start) / duration, 0, 1
        );
    }

    @Override
    public boolean isDone() {
        return System.currentTimeMillis() - start >= duration;
    }

    @Override
    public void checkExceptions() {

    }


}
