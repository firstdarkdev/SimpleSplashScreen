package me.hypherionmc.simplesplashscreen.client.textures;

import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.resources.data.TextureMetadataSection;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;

public class EmptyTexture extends SimpleTexture {

    public EmptyTexture(ResourceLocation location) {
        super(location);
    }

    @Override
    protected TextureData getTextureImage(IResourceManager p_118140_) {
        try {
            InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("empty.png");
            TextureData texture;

            try {
                texture = new TextureData(new TextureMetadataSection(true, true), NativeImage.read(input));
            } finally {
                input.close();
            }

            return texture;
        } catch (IOException var18) {
            return new TextureData(var18);
        }
    }

}
