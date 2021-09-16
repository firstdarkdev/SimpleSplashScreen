package me.hypherionmc.simplesplashscreen.mixins;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.hypherionmc.simplesplashscreen.config.SimpleSplashScreenConfig;
import me.hypherionmc.simplesplashscreen.textures.BlurredConfigTexture;
import me.hypherionmc.simplesplashscreen.textures.ConfigTexture;
import me.hypherionmc.simplesplashscreen.textures.EmptyTexture;
import me.hypherionmc.simplesplashscreen.textures.GifTextureRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.LoadingGui;
import net.minecraft.client.gui.ResourceLoadProgressGui;
import net.minecraft.resources.IAsyncReloader;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.ClientModLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.hypherionmc.simplesplashscreen.SimpleSplashScreen.CS_CONFIG;
import static net.minecraft.client.gui.AbstractGui.blit;
import static net.minecraft.client.gui.AbstractGui.fill;

@OnlyIn(Dist.CLIENT)
@Mixin(ResourceLoadProgressGui.class)
public class ResourceLoadProgressGuiMixin {

    @Shadow @Final private static ResourceLocation MOJANG_LOGO_TEXTURE;
    @Shadow @Final private Minecraft mc;
    @Shadow @Final private boolean reloading;
    @Shadow private float progress;
    @Shadow private final long fadeOutStart = -1L;
    @Shadow private long fadeInStart = -1L;
    @Shadow @Final private IAsyncReloader asyncReloader;

    private static final ResourceLocation EMPTY_TEXTURE = new ResourceLocation("empty.png");
    private static ResourceLocation MOJANG_TEXTURE;
    private static ResourceLocation ASPECT_1to1_TEXTURE;
    private static ResourceLocation BOSS_BAR_TEXTURE;
    private static ResourceLocation CUSTOM_PROGRESS_BAR_TEXTURE;
    private static ResourceLocation CUSTOM_PROGRESS_BAR_BACKGROUND_TEXTURE;
    private static ResourceLocation BACKGROUND_TEXTURE;

    private static GifTextureRenderer gifBackground, gifLogo, gifLoader;

    @Inject(at = @At("TAIL"), method = "loadLogoTexture", cancellable = true)
    private static void init(Minecraft client, CallbackInfo ci) {

        MOJANG_TEXTURE = new ResourceLocation(CS_CONFIG.textures.MojangLogo);
        ASPECT_1to1_TEXTURE = new ResourceLocation(CS_CONFIG.textures.Aspect1to1Logo);
        BOSS_BAR_TEXTURE = new ResourceLocation(CS_CONFIG.textures.BossBarTexture);
        CUSTOM_PROGRESS_BAR_TEXTURE = new ResourceLocation(CS_CONFIG.textures.CustomBarTexture);
        CUSTOM_PROGRESS_BAR_BACKGROUND_TEXTURE = new ResourceLocation(CS_CONFIG.textures.CustomBarBackgroundTexture);
        BACKGROUND_TEXTURE = new ResourceLocation(CS_CONFIG.textures.BackgroundTexture);

        gifLogo = null;
        gifBackground = null;

        if (CS_CONFIG.logoStyle == SimpleSplashScreenConfig.LogoStyle.Mojang) {
            client.getTextureManager().loadTexture(MOJANG_LOGO_TEXTURE, new BlurredConfigTexture(MOJANG_TEXTURE));
        }
        else {
            client.getTextureManager().loadTexture(MOJANG_LOGO_TEXTURE, new EmptyTexture(EMPTY_TEXTURE));
        }

        if (ASPECT_1to1_TEXTURE.getPath().endsWith("gif")) {
            gifLogo = new GifTextureRenderer(CS_CONFIG.textures.Aspect1to1Logo, client);
            gifLogo.registerFrames();
        } else {
            client.getTextureManager().loadTexture(ASPECT_1to1_TEXTURE, new ConfigTexture(ASPECT_1to1_TEXTURE));
        }

        if (BACKGROUND_TEXTURE.getPath().endsWith("gif")) {
            gifBackground = new GifTextureRenderer(CS_CONFIG.textures.BackgroundTexture, client);
            gifBackground.registerFrames();
        } else {
            client.getTextureManager().loadTexture(BACKGROUND_TEXTURE, new ConfigTexture(BACKGROUND_TEXTURE));
        }

        /*if (CUSTOM_PROGRESS_BAR_TEXTURE.getPath().endsWith("gif")) {
            gifLoader = new GifTextureRenderer(CS_CONFIG.textures.CustomBarTexture, client);
            gifLoader.registerFrames();
        } else {*/
            client.getTextureManager().loadTexture(CUSTOM_PROGRESS_BAR_TEXTURE, new ConfigTexture(CUSTOM_PROGRESS_BAR_TEXTURE));
        //}
        client.getTextureManager().loadTexture(CUSTOM_PROGRESS_BAR_BACKGROUND_TEXTURE, new ConfigTexture(CUSTOM_PROGRESS_BAR_BACKGROUND_TEXTURE));

        ci.cancel();
    }

    // Render Custom Background
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/TextureManager;bindTexture(Lnet/minecraft/util/ResourceLocation;)V"))
    public void renderBackground(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        long l = Util.milliTime();
        if (this.reloading && (this.asyncReloader.asyncPartDone() || this.mc.currentScreen != null) && this.fadeInStart == -1L) {
            this.fadeInStart = l;
        }

        float f = this.fadeOutStart > -1L ? (float)(l - this.fadeOutStart) / 1000.0F : -1.0F;
        float g = this.fadeInStart > -1L ? (float)(l - this.fadeInStart) / 500.0F : -1.0F;
        float o;

        if (f >= 1.0F) {
            o = 1.0F - MathHelper.clamp(f - 1.0F, 0.0F, 1.0F);
        } else if (this.reloading) {
            o = MathHelper.clamp(g, 0.0F, 1.0F);
        } else {
            o = 1.0F;
        }

        int maxX = this.mc.getMainWindow().getScaledWidth();
        int maxY = this.mc.getMainWindow().getScaledHeight();

        if (CS_CONFIG.backgroundImage) {

            if (gifBackground != null) {
                gifBackground.renderNextFrame(matrixStack, maxX, maxY, o);
            } else {
                mc.getTextureManager().bindTexture(BACKGROUND_TEXTURE);
                RenderSystem.enableBlend();
                RenderSystem.alphaFunc(516, 0.0F);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, o);
                blit(matrixStack, 0, 0, 0, 0, 0, maxX, maxY, maxY, maxX);
                RenderSystem.defaultBlendFunc();
                RenderSystem.defaultAlphaFunc();
                RenderSystem.disableBlend();
            }
        }
    }

    // Render Custom Logo
    @Inject(at = @At("TAIL"), method = "render")
    public void renderLogo(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        long l = Util.milliTime();
        if (this.reloading && (this.asyncReloader.asyncPartDone() || this.mc.currentScreen != null) && this.fadeInStart == -1L) {
            this.fadeInStart = l;
        }
        float f = this.fadeOutStart > -1L ? (float)(l - this.fadeOutStart) / 1000.0F : -1.0F;
        float g = this.fadeInStart > -1L ? (float)(l - this.fadeInStart) / 500.0F : -1.0F;
        float o;
        int m;

        if (f >= 1.0F) {
            o = 1.0F - MathHelper.clamp(f - 1.0F, 0.0F, 1.0F);
        } else if (this.reloading) {
            o = MathHelper.clamp(g, 0.0F, 1.0F);
        } else {
            o = 1.0F;
        }

        m = (int)((double)this.mc.getMainWindow().getScaledWidth() * 0.5D);

        if (CS_CONFIG.logoStyle == SimpleSplashScreenConfig.LogoStyle.Aspect1to1) {

            // The forge Progress Text breaks the custom Image Rendering, so this is a workaround to fix it
            if (CS_CONFIG.showProgressText) {
                RenderSystem.enableTexture();
                RenderSystem.defaultBlendFunc();
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0f);
            }

            double d = Math.min((double)this.mc.getMainWindow().getScaledWidth() * 0.75D, this.mc.getMainWindow().getScaledHeight()) * 0.25D;
            int r = (int)(d * 0.5D);
            double e = d * 4.0D;
            int s = (int)(e * 0.5D);

            if (gifLogo != null) {
                gifLogo.renderNextFrame(matrixStack, m - (s / 2), r, s, s, o);
            } else {
                mc.getTextureManager().bindTexture(ASPECT_1to1_TEXTURE);
                RenderSystem.enableBlend();
                RenderSystem.alphaFunc(516, 0.0F);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, o);
                blit(matrixStack, m - (s / 2), r, s, s, 0, 0, 512, 512, 512, 512);
                RenderSystem.defaultBlendFunc();
                RenderSystem.defaultAlphaFunc();
                RenderSystem.disableBlend();
            }

        }
    }

    // Modify Background Color
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/ResourceLoadProgressGui;fill(Lcom/mojang/blaze3d/matrix/MatrixStack;IIIII)V"))
    private void modifyBackgroundColor(MatrixStack matrixStack, int minX, int minY, int maxX, int maxY, int color) {
        long l = Util.milliTime();
        if (this.reloading && (this.asyncReloader.asyncPartDone() || this.mc.currentScreen != null) && this.fadeInStart == -1L) {
            this.fadeInStart = l;
        }
        float f = this.fadeOutStart > -1L ? (float)(l - this.fadeOutStart) / 1000.0F : -1.0F;
        int m;
        m = MathHelper.ceil((1.0F - MathHelper.clamp(f - 1.0F, 0.0F, 1.0F)) * 255.0F);

        int clr = color;

        if (CS_CONFIG.backgroundImage) {
            clr = ColorHelper.PackedColor.packColor(0, 0, 0, 0);
        }
        else {
            clr = CS_CONFIG.backgroundColor | m << 24;
        }

        fill(matrixStack, minX, minY, maxX, maxY, clr);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/ResourceLoadProgressGui;func_238629_a_(Lcom/mojang/blaze3d/matrix/MatrixStack;IIIIF)V"))
    private void renderProgressBar(ResourceLoadProgressGui resourceLoadProgressGui, MatrixStack matrixStack, int x1, int y1, int x2, int y2, float opacity) {
        int i = MathHelper.ceil((float)(x2 - x1 - 2) * this.progress);

        // Bossbar Progress Bar
        if (CS_CONFIG.progressBarType == SimpleSplashScreenConfig.ProgressBarType.BossBar) {
            this.mc.getTextureManager().bindTexture(BOSS_BAR_TEXTURE);

            int overlay = 0;

            if (CS_CONFIG.bossBarType == SimpleSplashScreenConfig.BossBarType.NOTCHED_6) {overlay = 93;}
            else if (CS_CONFIG.bossBarType == SimpleSplashScreenConfig.BossBarType.NOTCHED_10) {overlay = 105;}
            else if (CS_CONFIG.bossBarType == SimpleSplashScreenConfig.BossBarType.NOTCHED_12) {overlay = 117;}
            else if (CS_CONFIG.bossBarType == SimpleSplashScreenConfig.BossBarType.NOTCHED_20) {overlay = 129;}

            int bbWidth = (int) ((x2 - x1+1) * 1.4f);
            int bbHeight = (y2 - y1) * 30;
            blit(matrixStack, x1, y1 + 1, 0, 0, 0, x2 - x1, (int) ((y2-y1) / 1.4f), bbHeight, bbWidth);
            blit(matrixStack, x1, y1 + 1, 0, 0, 5f, i, (int) ((y2 - y1) / 1.4f), bbHeight, bbWidth);

            RenderSystem.enableBlend();
            RenderSystem.blendEquation(32774);
            RenderSystem.blendFunc(770, 1);
            if (overlay != 0) {
                blit(matrixStack, x1, y1 + 1, 0, 0, overlay, x2 - x1, (int) ((y2 - y1) / 1.4f), bbHeight, bbWidth);
            }
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
        }

        // Custom Progress Bar
        if (CS_CONFIG.progressBarType == SimpleSplashScreenConfig.ProgressBarType.Custom) {

            // The forge Progress Text breaks the custom Image Rendering, so this is a workaround to fix it
            if (CS_CONFIG.showProgressText) {
                RenderSystem.enableTexture();
                RenderSystem.defaultBlendFunc();
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0f);
            }

            /*if (gifLoader != null) {
                double d = Math.min((double)this.mc.getMainWindow().getScaledWidth() * 0.75D, this.mc.getMainWindow().getScaledHeight()) * 0.25D;
                int r = (int)(d * 0.5D);
                double e = d * 4.0D;
                int s = (int)(e * 0.5D);
                gifLoader.renderProgressBar(matrixStack, x1 + (64 / 2), y2 - 74, 64);
            } else {*/
                int customWidth = CS_CONFIG.customProgressBarMode == SimpleSplashScreenConfig.ProgressBarMode.Linear ? x2 - x1 : i;

                if (CS_CONFIG.customProgressBarBackground) {
                    this.mc.getTextureManager().bindTexture(CUSTOM_PROGRESS_BAR_BACKGROUND_TEXTURE);
                    blit(matrixStack, x1, y1, 0, 0, 0, x2 - x1, y2 - y1, 10, x2 - x1);
                }

                this.mc.getTextureManager().bindTexture(CUSTOM_PROGRESS_BAR_TEXTURE);
                blit(matrixStack, x1, y1, 0, 0, 0, i, y2 - y1, 10, customWidth);
            //}

        }

        // Vanilla / With Color progress bar
        if (CS_CONFIG.progressBarType == SimpleSplashScreenConfig.ProgressBarType.Vanilla) {
            int j = Math.round(opacity * 255.0F);
            int k = CS_CONFIG.progressBarColor | j << 24;
            int kk = CS_CONFIG.progressFrameColor | j << 24;

            fill(matrixStack, x1 + 2, y1 + 2, x1 + i, y2 - 2, k);
            fill(matrixStack, x1 + 1, y1, x2 - 1, y1 + 1, kk);
            fill(matrixStack, x1 + 1, y2, x2 - 1, y2 - 1, kk);
            fill(matrixStack, x1, y1, x1 + 1, y2, kk);
            fill(matrixStack, x2, y1, x2 - 1, y2, kk);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setLoadingGui(Lnet/minecraft/client/gui/LoadingGui;)V"))
    public void setLoadingGui(Minecraft minecraft, LoadingGui loadingGuiIn) {
        this.mc.setLoadingGui((LoadingGui)null);
        if (gifBackground != null) {
            gifBackground.unloadAll();
        }
        if (gifLogo != null) {
            gifLogo.unloadAll();
        }
        if (gifLoader != null) {
            gifLoader.unloadAll();
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/client/ClientModLoader;renderProgressText()V"))
    private void renderProgressTextForge() {
        if (CS_CONFIG.showProgressText) {
            ClientModLoader.renderProgressText();
        }
    }

}
