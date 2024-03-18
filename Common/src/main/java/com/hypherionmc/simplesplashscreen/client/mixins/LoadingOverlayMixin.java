package com.hypherionmc.simplesplashscreen.client.mixins;

import com.hypherionmc.simplesplashscreen.SimpleSplashScreenCommon;
import com.hypherionmc.simplesplashscreen.client.config.SimpleSplashScreenConfig;
import com.hypherionmc.simplesplashscreen.client.textures.FileBasedTexture;
import com.hypherionmc.simplesplashscreen.client.textures.GifTextureRenderer;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.function.Consumer;

import static com.hypherionmc.simplesplashscreen.SimpleSplashScreenCommon.CS_CONFIG;

/**
 * @author HypherionSA
 * Very "Destructive" mixin to the loading overlay GUI, so we can modify it nicely
 * Also prevents Optifine from BREAKING SHIT
 */
@Mixin(LoadingOverlay.class)
public abstract class LoadingOverlayMixin {

    // Shadow Fields
    @Shadow @Final private boolean fadeIn;
    @Shadow private long fadeInStart;
    @Shadow private long fadeOutStart;
    @Shadow @Final private Minecraft minecraft;
    @Shadow private float currentProgress;
    @Shadow @Final private ReloadInstance reload;
    @Shadow @Final private Consumer<Optional<Throwable>> onFinish;

    // Mod Fields
    @Unique
    private static final ResourceLocation EMPTY_TEXTURE = new ResourceLocation("empty.png");
    @Unique
    private static ResourceLocation ASPECT_1to1_TEXTURE;
    @Unique
    private static ResourceLocation MOJANG_LOGO;
    @Unique
    private static ResourceLocation BOSS_BAR_TEXTURE;
    @Unique
    private static ResourceLocation CUSTOM_PROGRESS_BAR_TEXTURE;
    @Unique
    private static ResourceLocation CUSTOM_PROGRESS_BAR_BACKGROUND_TEXTURE;
    @Unique
    private static ResourceLocation BACKGROUND_TEXTURE;
    @Unique
    private static GifTextureRenderer backgroundGifRenderer, logoGifRenderer;
    @Unique
    private int lastHeight = 0;
    @Unique
    private int lastWidth = 0;

    /**
     * Here we inject to register our custom textures and set up the default textures
     */
    @Inject(method = "registerTextures", at = @At("TAIL"), cancellable = true)
    private static void injectTextures(Minecraft arg, CallbackInfo ci) {
        // Set up the main textures, config etc. This is done once to prevent cloth config errors
        if (!SimpleSplashScreenCommon.initDone) {
            SimpleSplashScreenCommon.init();
            SimpleSplashScreenCommon.initDone = true;
        }

        // Register Textures from Config
        ASPECT_1to1_TEXTURE = new ResourceLocation(CS_CONFIG.textures.Aspect1to1Logo);
        BOSS_BAR_TEXTURE = new ResourceLocation(CS_CONFIG.textures.BossBarTexture);
        CUSTOM_PROGRESS_BAR_TEXTURE = new ResourceLocation(CS_CONFIG.textures.CustomBarTexture);
        CUSTOM_PROGRESS_BAR_BACKGROUND_TEXTURE = new ResourceLocation(CS_CONFIG.textures.CustomBarBackgroundTexture);
        BACKGROUND_TEXTURE = new ResourceLocation(CS_CONFIG.textures.BackgroundTexture);
        MOJANG_LOGO = new ResourceLocation(CS_CONFIG.textures.MojangLogo);
        arg.getTextureManager().register(MOJANG_LOGO, new FileBasedTexture(MOJANG_LOGO));

        // Set up GIF images
        if (ASPECT_1to1_TEXTURE.getPath().endsWith(".gif")) {
            logoGifRenderer = new GifTextureRenderer(CS_CONFIG.textures.Aspect1to1Logo, arg);
            logoGifRenderer.registerFrames();
        } else {
            arg.getTextureManager().register(ASPECT_1to1_TEXTURE, new FileBasedTexture(ASPECT_1to1_TEXTURE));
        }

        if (BACKGROUND_TEXTURE.getPath().endsWith("gif")) {
            backgroundGifRenderer = new GifTextureRenderer(CS_CONFIG.textures.BackgroundTexture, arg);
            backgroundGifRenderer.registerFrames();
        } else {
            arg.getTextureManager().register(BACKGROUND_TEXTURE, new FileBasedTexture(BACKGROUND_TEXTURE));
        }

        // Register Progress bar textures
        arg.getTextureManager().register(CUSTOM_PROGRESS_BAR_TEXTURE, new FileBasedTexture(CUSTOM_PROGRESS_BAR_TEXTURE));
        arg.getTextureManager().register(CUSTOM_PROGRESS_BAR_BACKGROUND_TEXTURE, new FileBasedTexture(CUSTOM_PROGRESS_BAR_BACKGROUND_TEXTURE));

        // We're done, so we return
        ci.cancel();
    }

    /**
     * Here we essentially replace the render method
     * This is mainly vanilla loading screen code, with added hooks to allow for customization
     */
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void injectRender(GuiGraphics arg, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        // Cancel the rendering completely, so that we can handle it
        ci.cancel();

        int width = arg.guiWidth();
        int height = arg.guiHeight();
        long millis = Util.getMillis();

        if (this.fadeIn && this.fadeInStart == -1L) {
            this.fadeInStart = millis;
        }

        float fadeOutTime = this.fadeOutStart > -1L ? (float)(millis - this.fadeOutStart) / 1000.0F : -1.0F;
        float fadeInTime = this.fadeInStart > -1L ? (float)(millis - this.fadeInStart) / 500.0F : -1.0F;
        float timedAlpha;

        if (fadeOutTime >= 1.0F) {
            if (this.minecraft.screen != null) {
                this.minecraft.screen.render(arg, mouseX, mouseY, partialTicks);
            }

            int alpha = Mth.ceil((1.0F - Mth.clamp(fadeOutTime - 1.0F, 0.0F, 1.0F)) * 255.0F);
            arg.fill(RenderType.guiOverlay(), 0, 0, width, height, changeAlpha(getBGColor(), alpha));
            timedAlpha = 1.0F - Mth.clamp(fadeOutTime - 1.0F, 0.0F, 1.0F);
        } else if (this.fadeIn) {
            if (this.minecraft.screen != null && fadeInTime < 1.0F) {
                this.minecraft.screen.render(arg, mouseX, mouseY, partialTicks);
            }

            int alpha = Mth.ceil(Mth.clamp((double)fadeInTime, 0.15, 1.0) * 255.0);
            arg.fill(RenderType.guiOverlay(), 0, 0, width, height, changeAlpha(getBGColor(), alpha));
            timedAlpha = Mth.clamp(fadeInTime, 0.0F, 1.0F);
        } else {
            int color = getBGColor();
            float r = (float)(color >> 16 & 255) / 255.0F;
            float g = (float)(color >> 8 & 255) / 255.0F;
            float b = (float)(color & 255) / 255.0F;
            GlStateManager._clearColor(r, g, b, 1.0F);
            GlStateManager._clear(16384, Minecraft.ON_OSX);
            timedAlpha = 1.0F;
        }

        // Custom Background Image Hook
        if (CS_CONFIG.backgroundImage) {
            if (backgroundGifRenderer != null) {
                backgroundGifRenderer.renderNextFrame(arg, width, height, timedAlpha);
            } else {
                RenderSystem.enableBlend();
                RenderSystem.blendEquation(32774);
                RenderSystem.blendFunc(770, 1);
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, timedAlpha);
                arg.blit(BACKGROUND_TEXTURE, 0, 0, 0, 0, 0, width, height, width, height);
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableBlend();
            }
        }

        int scaledWidth = (int)((double)arg.guiWidth() * 0.5);
        int scaledHeight = (int)((double)arg.guiHeight() * 0.5);
        double scale = Math.min((double)arg.guiWidth() * 0.75, (double)arg.guiHeight()) * 0.25;
        int i1 = (int)(scale * 0.5);
        double d0 = scale * 4.0;
        int j1 = (int)(d0 * 0.5);

        // Progress Bar
        int k1 = (int)((double)arg.guiHeight() * 0.8325);
        float actualProgress = this.reload.getActualProgress();
        this.currentProgress = Mth.clamp(this.currentProgress * 0.95F + actualProgress * 0.050000012F, 0.0F, 1.0F);
        if (fadeOutTime < 1.0F) {
            this.drawProgressBar(arg, width / 2 - j1, k1 - 5, width / 2 + j1, k1 + 5, 1.0F - Mth.clamp(fadeOutTime, 0.0F, 1.0F));
        }

        // Logo Hook
        if (CS_CONFIG.logoStyle == SimpleSplashScreenConfig.LogoStyle.Mojang) {
            // Mojang Logo
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(770, 1);
            arg.setColor(1.0F, 1.0F, 1.0F, timedAlpha);
            arg.blit(MOJANG_LOGO, scaledWidth - j1, scaledHeight - i1, j1, (int)scale, -0.0625F, 0.0F, 120, 60, 120, 120);
            arg.blit(MOJANG_LOGO, scaledWidth, scaledHeight - i1, j1, (int)scale, 0.0625F, 60.0F, 120, 60, 120, 120);
            arg.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
        } else if (CS_CONFIG.logoStyle == SimpleSplashScreenConfig.LogoStyle.Aspect1to1 && CS_CONFIG.progressBarType != SimpleSplashScreenConfig.ProgressBarType.Logo) {
            // Custom Logo
            renderLogo(arg, timedAlpha, 1);
        }

        // Return to Mojang Code
        if (fadeOutTime >= 2.0F) {
            this.minecraft.setOverlay((Overlay)null);
        }

        if (this.fadeOutStart == -1L && this.reload.isDone() && (!this.fadeIn || fadeInTime >= 2.0F)) {
            try {
                this.reload.checkExceptions();
                this.onFinish.accept(Optional.empty());
            } catch (Throwable var23) {
                this.onFinish.accept(Optional.of(var23));
            }

            this.fadeOutStart = Util.getMillis();
            if (this.minecraft.screen != null) {
                this.minecraft.screen.init(this.minecraft, arg.guiWidth(), arg.guiHeight());
            }
        }
    }

    /**
     * Render our Progress bars. This can be either Boss Bar, Custom Image progress bar, or the vanilla bar
     */
    @Unique
    private void drawProgressBar(GuiGraphics arg, int x1, int y1, int x2, int y2, float opacity) {
        int i = Mth.ceil((float)(x2 - x1 - 2) * this.currentProgress);

        if (CS_CONFIG.progressBarType != SimpleSplashScreenConfig.ProgressBarType.Logo && CS_CONFIG.progressBarType != SimpleSplashScreenConfig.ProgressBarType.Background) {
            // Bossbar Progress Bar
            if (CS_CONFIG.progressBarType == SimpleSplashScreenConfig.ProgressBarType.BossBar) {
                int color = CS_CONFIG.bossBarColor.ordinal() * 10;
                RenderSystem.setShader(GameRenderer::getPositionTexShader);

                int overlay = 70 + CS_CONFIG.bossBarType.ordinal() * 10;
                int width = (int) ((x2 - x1) * (85 * 0.01f));
                int offset = ((x2 - x1) - width) / 2;
                i = Mth.ceil((float)(width - 2) * this.currentProgress);

                arg.blit(BOSS_BAR_TEXTURE, x1 + offset, y1 + 1, width, (int) ((width / 182f) * 5), 0, color, 182, 5,256, 256);
                arg.blit(BOSS_BAR_TEXTURE, x1 + offset, y1 + 1, i, (int) ((width / 182f) * 5), 0, color+5, (int) (180 * this.currentProgress), 5, 256, 256);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                if (overlay != 120) {
                    arg.blit(BOSS_BAR_TEXTURE, x1 + offset, y1 + 1, width, (int) ((width / 182f) * 5), 0, overlay, 182, 5,256, 256);
                }
                RenderSystem.disableBlend();
            }

            // Custom Progress Bar
            if (CS_CONFIG.progressBarType == SimpleSplashScreenConfig.ProgressBarType.Custom) {
                int regionWidth = CS_CONFIG.customProgressBarMode == SimpleSplashScreenConfig.ProgressBarMode.Stretch ? x2 - x1 : i;
                int height = (int) (((x2 - x1) / 400f) * 10);
                int u = CS_CONFIG.customProgressBarMode.equals(SimpleSplashScreenConfig.ProgressBarMode.Slide) ? x2 - x1 - i : 0;
                if (CS_CONFIG.customProgressBarBackground) {
                    RenderSystem.setShader(GameRenderer::getPositionTexShader);
                    arg.blit(CUSTOM_PROGRESS_BAR_BACKGROUND_TEXTURE, x1, y1, x2 - x1, height, 0, 0, x2 - x1, height, x2 - x1, height);
                }

                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                arg.blit(CUSTOM_PROGRESS_BAR_TEXTURE, x1, y1, i, height, u, 0, regionWidth, height, x2 - x1, height);
            }

            // Vanilla / With Color progress bar
            if (CS_CONFIG.progressBarType == SimpleSplashScreenConfig.ProgressBarType.Vanilla) {
                int j = Math.round(opacity * 255.0F);
                int k = CS_CONFIG.progressBarColor | j << 24;
                int kk = CS_CONFIG.progressFrameColor | j << 24;

                arg.fill(x1 + 2, y1 + 2, x1 + i, y2 - 2, k);
                arg.fill(x1 + 1, y1, x2 - 1, y1 + 1, kk);
                arg.fill(x1 + 1, y2, x2 - 1, y2 - 1, kk);
                arg.fill(x1, y1, x1 + 1, y2, kk);
                arg.fill(x2, y1, x2 - 1, y2, kk);
            }

        } else if (CS_CONFIG.progressBarType == SimpleSplashScreenConfig.ProgressBarType.Logo) {
            renderLogo(arg, 1.0F, currentProgress);
        } else {
            renderBackgroundBar(arg, 1.0F, currentProgress);
        }
    }

    /**
     * Render a custom logo. Used for Aspect1to1 and GIF logos
     */
    @Unique
    private void renderLogo(GuiGraphics matrixStack, float o, float currentProgress) {
        double d = Math.min((double)this.minecraft.getWindow().getGuiScaledWidth() * 0.75D, this.minecraft.getWindow().getGuiScaledHeight()) * 0.25D;
        int m = (int)((double)this.minecraft.getWindow().getGuiScaledWidth() * 0.5D);
        int r = (int)(d * 0.5D);
        double e = d * 4.0D;
        int s = (int)(e * 0.5D);
        lastHeight = Math.max(Math.round(currentProgress * 512), lastHeight);
        int prog = (int) (((float) lastHeight / 512) * s);

        if (logoGifRenderer != null) {
            logoGifRenderer.renderNextFrame(matrixStack, m - (s / 2), (r + s) - prog, s, s, o, lastHeight);
        } else {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, o);
            matrixStack.blit(ASPECT_1to1_TEXTURE, m - (s / 2), (r + s) - prog, s, s, 0, 512 - lastHeight, 512, 512, 512, 512);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
        }
    }

    /**
     * Render the Background Progress bar. This uses the Background Image/Color, with the Progress Bar texture
     * as an overlay over the background
     */
    @Unique
    private void renderBackgroundBar(GuiGraphics matrixStack, float o, float currentProgress) {
        int maxX = this.minecraft.getWindow().getGuiScaledWidth();
        int maxY = this.minecraft.getWindow().getGuiScaledHeight();
        double d = Math.min((double)this.minecraft.getWindow().getGuiScaledWidth() * 0.75D, this.minecraft.getWindow().getGuiScaledHeight()) * 0.25D;
        double e = d * 4.0D;
        int s = (int)(e * 0.5D);
        lastWidth = Math.max(Math.round(currentProgress * maxX), lastWidth);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, o);
        matrixStack.blit(CUSTOM_PROGRESS_BAR_TEXTURE, 0, 0, 0, 0, 0, lastWidth, maxY, maxX, maxY);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    // Helper Methods
    @Unique
    private static int changeAlpha(int clr, int alpha) {
        return getBGColor() & 16777215 | alpha << 24;
    }

    @Unique
    private static int getBGColor() {
        return CS_CONFIG.backgroundImage ? FastColor.ARGB32.color(0, 0, 0, 0) : CS_CONFIG.backgroundColor;
    }
}
