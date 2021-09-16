package me.hypherionmc.simplesplashscreen.preview;

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
    public CompletableFuture<Unit> onceDone() {
        if (onDone != null) {
            onDone.run();
        }
        return null;
    }

    @Override
    public float estimateExecutionSpeed() {
        return MathHelper.clamp(
       (float) (System.currentTimeMillis() - start) / duration, 0, 1
        );
    }

    @Override
    public boolean asyncPartDone() {
        return System.currentTimeMillis() - start >= duration;
    }

    @Override
    public boolean fullyDone() {
        return System.currentTimeMillis() - start >= duration;
    }

    @Override
    public void join() {}
}
