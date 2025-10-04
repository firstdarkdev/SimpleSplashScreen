package com.hypherionmc.simplesplashscreen.client.textures;

import com.hypherionmc.simplesplashscreen.SimpleSplashScreenCommon;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author HypherionSA
 * A Custom SimpleTexture to allow loading images from disk, rather than from resources
 */
public class FileBasedTexture extends SimpleTexture {

    public FileBasedTexture(ResourceLocation arg) {
        super(arg);
    }

    @Override
    public @NotNull TextureContents loadContents(ResourceManager arg) {
        try {
            try (InputStream is = new FileInputStream(new File(SimpleSplashScreenCommon.CONFIG_PATH, this.resourceId().toString().replace("minecraft:", "")))) {
                return new TextureContents(NativeImage.read(is), new TextureMetadataSection(true, true));
            }
        } catch (IOException e) {
            return TextureContents.createMissing();
        }
    }
}
