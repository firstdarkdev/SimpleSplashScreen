package me.hypherionmc.simplesplashscreen.client.preview;

import net.minecraft.resources.IAsyncReloader;
import net.minecraft.util.Unit;
import net.minecraft.util.math.MathHelper;

import java.util.concurrent.CompletableFuture;

public class PreviewResourceReloader implements IAsyncReloader {

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
        return MathHelper.clamp(
        (float) (System.currentTimeMillis() - start) / duration, 0, 1
        );
    }

    @Override
    public boolean isApplying() {
        return false;
    }

    @Override
    public boolean isDone() {
        return System.currentTimeMillis() - start >= duration;
    }

    @Override
    public void checkExceptions() {

    }
}
