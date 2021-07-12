package me.hypherionmc.simplesplashscreen.textures;

import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.resources.data.TextureMetadataSection;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BlurredConfigTexture extends SimpleTexture {

    public BlurredConfigTexture(ResourceLocation location) {
        super(location);
    }

    @Override
    protected TextureData getTextureData(IResourceManager resourceManager) {
        try {
            InputStream input = new FileInputStream(FMLPaths.CONFIGDIR.get() + "/simplesplashscreen/" + this.textureLocation.toString().replace("minecraft:", ""));
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
