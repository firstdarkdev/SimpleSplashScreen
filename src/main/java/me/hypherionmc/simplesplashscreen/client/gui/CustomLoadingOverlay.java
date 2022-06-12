package me.hypherionmc.simplesplashscreen.client.gui;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FastColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

public class CustomLoadingOverlay extends Overlay {

    static final ResourceLocation MOJANG_STUDIOS_LOGO_LOCATION = new ResourceLocation("textures/gui/title/mojangstudios.png");
    private static final int LOGO_BACKGROUND_COLOR = FastColor.ARGB32.color(255, 239, 50, 61);
    private static final int LOGO_BACKGROUND_COLOR_DARK = FastColor.ARGB32.color(255, 0, 0, 0);
    private static final IntSupplier BRAND_BACKGROUND = () -> {
        return Minecraft.getInstance().options.darkMojangStudiosBackground().get() ? LOGO_BACKGROUND_COLOR_DARK : LOGO_BACKGROUND_COLOR;
    };
    private static final int LOGO_SCALE = 240;
    private static final float LOGO_QUARTER_FLOAT = 60.0F;
    private static final int LOGO_QUARTER = 60;
    private static final int LOGO_HALF = 120;
    private static final float LOGO_OVERLAP = 0.0625F;
    private static final float SMOOTHING = 0.95F;
    public static final long FADE_OUT_TIME = 1000L;
    public static final long FADE_IN_TIME = 500L;
    private final Minecraft minecraft;
    private final ReloadInstance reload;
    private final Consumer<Optional<Throwable>> onFinish;
    private final boolean fadeIn;
    private float currentProgress;
    private long fadeOutStart = -1L;
    private long fadeInStart = -1L;

    public CustomLoadingOverlay(Minecraft p_96172_, ReloadInstance p_96173_, Consumer<Optional<Throwable>> p_96174_, boolean p_96175_) {
        this.minecraft = p_96172_;
        this.reload = p_96173_;
        this.onFinish = p_96174_;
        this.fadeIn = p_96175_;
    }

    public static void registerTextures(Minecraft p_96190_) {
        p_96190_.getTextureManager().register(MOJANG_STUDIOS_LOGO_LOCATION, new CustomLoadingOverlay.LogoTexture());
    }

    @Override
    public void render(PoseStack p_94669_, int p_94670_, int p_94671_, float p_94672_) {

    }

    @OnlyIn(Dist.CLIENT)
    static class LogoTexture extends SimpleTexture {
        public LogoTexture() {
            super(CustomLoadingOverlay.MOJANG_STUDIOS_LOGO_LOCATION);
        }

        protected SimpleTexture.TextureImage getTextureImage(ResourceManager p_96194_) {
            Minecraft minecraft = Minecraft.getInstance();
            VanillaPackResources vanillapackresources = minecraft.getClientPackSource().getVanillaPack();

            try {
                InputStream inputstream = vanillapackresources.getResource(PackType.CLIENT_RESOURCES, CustomLoadingOverlay.MOJANG_STUDIOS_LOGO_LOCATION);

                SimpleTexture.TextureImage simpletexture$textureimage;
                try {
                    simpletexture$textureimage = new SimpleTexture.TextureImage(new TextureMetadataSection(true, true), NativeImage.read(inputstream));
                } catch (Throwable throwable1) {
                    if (inputstream != null) {
                        try {
                            inputstream.close();
                        } catch (Throwable throwable) {
                            throwable1.addSuppressed(throwable);
                        }
                    }

                    throw throwable1;
                }

                if (inputstream != null) {
                    inputstream.close();
                }

                return simpletexture$textureimage;
            } catch (IOException ioexception) {
                return new SimpleTexture.TextureImage(ioexception);
            }
        }
    }
}
