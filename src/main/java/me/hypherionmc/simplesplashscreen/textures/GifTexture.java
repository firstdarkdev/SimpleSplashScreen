package me.hypherionmc.simplesplashscreen.textures;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

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
    protected TextureImage getTextureImage(ResourceManager p_118140_) {
        try {
            TextureImage texture;

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(image, "png", os);
            InputStream is = new ByteArrayInputStream(os.toByteArray());

            try {
                texture = new TextureImage(new TextureMetadataSection(true, true), NativeImage.read(is));
            } finally {
                is.close();
            }

            return texture;
        } catch (IOException var18) {
            return new TextureImage(var18);
        }
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }
}
