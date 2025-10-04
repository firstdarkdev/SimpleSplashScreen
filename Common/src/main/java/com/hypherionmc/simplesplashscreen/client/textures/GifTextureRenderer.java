package com.hypherionmc.simplesplashscreen.client.textures;

import com.hypherionmc.simplesplashscreen.SimpleSplashScreenCommon;
import com.hypherionmc.simplesplashscreen.client.util.GifDecoder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Random;

/**
 * @author HypherionSA
 * A Hacky renderer to render GIF's in game
 */
public class GifTextureRenderer {

    private HashMap<Integer, GifTextureHolder> frames;
    private int frameCount;
    private int currentFrame;
    private final TextureManager mc;
    private int tick = 5;

    public GifTextureRenderer(String texture, TextureManager minecraft) {
        this.mc = minecraft;
        int textureID = new Random().nextInt();

        try {
            InputStream input = new FileInputStream(new File(SimpleSplashScreenCommon.CONFIG_PATH, texture));
            GifDecoder gifDecoder = new GifDecoder();
            int status = gifDecoder.read(input);
            SimpleSplashScreenCommon.LOGGER.info("Status of gif {}: {}", texture, status);
            frameCount = gifDecoder.getFrameCount();
            SimpleSplashScreenCommon.LOGGER.info("Found {} frames in gif {}", frameCount, texture);

            currentFrame = 0;
            frames = new HashMap<>();

            for (int i = 0; i < frameCount; i++) {
                BufferedImage frame = gifDecoder.getFrame(i);
                if (frame != null) {
                    ResourceLocation location = ResourceLocation.parse(textureID + "_frame_" + i);
                    frames.put(i, new GifTextureHolder(location, new GifTexture(location, frame)));
                }
            }

            frameCount = frames.size();
            SimpleSplashScreenCommon.LOGGER.info("Loaded {} frames from gif {}", frameCount, texture);
        } catch (Exception e) {
            SimpleSplashScreenCommon.LOGGER.error("Failed to decode gif {}", texture, e);
        }

    }

    public void registerFrames() {
        frames.forEach(((location, simpleTexture) -> mc.registerAndLoad(simpleTexture.location(), simpleTexture.texture())));
    }

    public void renderNextFrame(GuiGraphics stack, int maxX, int maxY, float alpha) {
        tick++;
        if (!frames.isEmpty()) {

            if (tick >= 5) {
                if (currentFrame + 1 >= frameCount) {
                    currentFrame = 0;
                } else {
                    currentFrame++;
                }
                tick = 0;
            }

            GifTextureHolder holder = frames.get(currentFrame);
            int color = ARGB.colorFromFloat(alpha, 1.0f, 1.0f, 1.0f);
            stack.blit(RenderPipelines.GUI_TEXTURED, holder.location(), 0, 0, 0f, 0f, maxX, maxY, maxX, maxY, color);
        }
    }

    public void renderNextFrame(GuiGraphics stack, int maxX, int maxY, int width, int height, float alpha, int clip) {
        tick++;
        if (!frames.isEmpty()) {

            if (tick >= 5) {
                if (currentFrame + 1 >= frameCount) {
                    currentFrame = 0;
                } else {
                    currentFrame++;
                }
                tick = 0;
            }

            GifTextureHolder holder = frames.get(currentFrame);
            int color = ARGB.colorFromFloat(alpha, 1.0f, 1.0f, 1.0f);
            stack.blit(RenderPipelines.GUI_TEXTURED, holder.location(), maxX, maxY, 0, 512 - clip, width, height, 512, 512, 512, 512, color);
        }
    }

    public void unloadAll() {
        frames.forEach(((location, simpleTexture) ->  {
            mc.release(simpleTexture.location());
        }));
        frames.clear();
    }
}
