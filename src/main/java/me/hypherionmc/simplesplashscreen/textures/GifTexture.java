package me.hypherionmc.simplesplashscreen.textures;

import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.resources.data.TextureMetadataSection;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class GifTexture extends SimpleTexture {

    private final BufferedImage image;
    private final int width, height;

    public GifTexture(ResourceLocation location, BufferedImage image) {
        super(location);
        this.image = image;
        width = image.getWidth();
        height = image.getHeight();
    }

    @Override
    protected TextureData getTextureData(IResourceManager resourceManager) {
        try {
            TextureData texture;

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(image, "png", os);
            InputStream is = new ByteArrayInputStream(os.toByteArray());

            try {
                texture = new TextureData(new TextureMetadataSection(true, true), NativeImage.read(is));
            } finally {
                is.close();
            }

            return texture;
        } catch (IOException var18) {
            return new TextureData(var18);
        }
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }
}
