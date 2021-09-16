package me.hypherionmc.simplesplashscreen.textures;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.hypherionmc.simplesplashscreen.util.GifDecoder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Random;

import static net.minecraft.client.gui.AbstractGui.blit;

public class GifTextureRenderer {

    private HashMap<Integer, SimpleTexture> frames;
    private int frameCount;
    private int currentFrame;
    private final Minecraft mc;
    private int tick = 5;
    private final int textureID;

    public GifTextureRenderer(String texture, Minecraft minecraft) {
        this.mc = minecraft;

        textureID = new Random().nextInt();

        try {
            InputStream input = new FileInputStream(FMLPaths.CONFIGDIR.get() + "/simplesplashscreen/" + texture);
            GifDecoder gifDecoder = new GifDecoder();
            gifDecoder.read(input);
            frameCount = gifDecoder.getFrameCount();

            currentFrame = 0;
            frames = new HashMap<>();

            for (int i = 0; i < frameCount; i++) {
                BufferedImage frame = gifDecoder.getFrame(i);
                if (frame != null) {
                    ResourceLocation location = new ResourceLocation(textureID + "_frame_" + i);
                    frames.put(i, new GifTexture(location, frame));
                }
            }

            frameCount = frames.size();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void registerFrames() {
        frames.forEach(((location, simpleTexture) ->  {
            mc.getTextureManager().loadTexture(new ResourceLocation(textureID + "_frame_" + location), simpleTexture);
        }));
    }

    public void renderNextFrame(MatrixStack stack, int maxX, int maxY, float alpha) {
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

            frames.get(currentFrame).bindTexture();
            RenderSystem.enableBlend();
            RenderSystem.alphaFunc(516, 0.0F);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
            blit(stack, 0, 0, 0, 0, 0, maxX, maxY, maxY, maxX);
            RenderSystem.defaultBlendFunc();
            RenderSystem.defaultAlphaFunc();
            RenderSystem.disableBlend();
        }
    }

    public void renderNextFrame(MatrixStack stack, int maxX, int maxY, int width, int height, float alpha) {
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

            frames.get(currentFrame).bindTexture();

            RenderSystem.enableBlend();
            RenderSystem.alphaFunc(516, 0.0F);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
            blit(stack, maxX, maxY, width, height, 0, 0, 512, 512, 512, 512);
            RenderSystem.defaultBlendFunc();
            RenderSystem.defaultAlphaFunc();
            RenderSystem.disableBlend();
        }
    }

    public void unloadAll() {
        frames.forEach(((location, simpleTexture) ->  {
            mc.getTextureManager().deleteTexture(new ResourceLocation(textureID + "_frame_" + location));
        }));
        frames.clear();

    }

}
