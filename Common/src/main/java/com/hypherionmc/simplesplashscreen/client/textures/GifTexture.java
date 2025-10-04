package com.hypherionmc.simplesplashscreen.client.textures;

import com.mojang.blaze3d.platform.NativeImage;
import lombok.Getter;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author HypherionSA
 * A SimpleTexture that supports GIF images
 */
public class GifTexture extends FileBasedTexture {

    private final BufferedImage image;
    @Getter private final int width, height;

    public GifTexture(ResourceLocation location, BufferedImage image) {
        super(location);
        this.image = image;
        width = image.getWidth();
        height = image.getHeight();
    }

    @NotNull
    @Override
    public TextureContents loadContents(ResourceManager arg) {
        try {
            TextureContents texture;

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(image, "png", os);

            try (InputStream is = new ByteArrayInputStream(os.toByteArray())) {
                texture = new TextureContents(NativeImage.read(is), new TextureMetadataSection(true, true));
            }

            return texture;
        } catch (IOException var18) {
            return TextureContents.createMissing();
        }
    }
}
