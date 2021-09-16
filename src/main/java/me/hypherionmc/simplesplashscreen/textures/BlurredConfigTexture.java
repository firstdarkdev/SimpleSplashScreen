package me.hypherionmc.simplesplashscreen.textures;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BlurredConfigTexture extends SimpleTexture {

    public BlurredConfigTexture(ResourceLocation location) {
        super(location);
    }

    @Override
    protected TextureImage getTextureImage(ResourceManager p_118140_) {
        try {
            InputStream input = new FileInputStream(FMLPaths.CONFIGDIR.get() + "/simplesplashscreen/" + this.location.toString().replace("minecraft:", ""));
            TextureImage texture;

            try {
                texture = new TextureImage(new TextureMetadataSection(true, true), NativeImage.read(input));
            } finally {
                input.close();
            }

            return texture;
        } catch (IOException var18) {
            return new TextureImage(var18);
        }
    }
}
