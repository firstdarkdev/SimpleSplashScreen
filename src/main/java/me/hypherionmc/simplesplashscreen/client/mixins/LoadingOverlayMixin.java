package me.hypherionmc.simplesplashscreen.client.mixins;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.hypherionmc.simplesplashscreen.SimpleSplashScreen;
import me.hypherionmc.simplesplashscreen.client.config.SimpleSplashScreenConfig;
import me.hypherionmc.simplesplashscreen.client.textures.ConfigTexture;
import me.hypherionmc.simplesplashscreen.client.textures.GifTextureRenderer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.function.Consumer;

import static me.hypherionmc.simplesplashscreen.SimpleSplashScreen.CS_CONFIG;
import static net.minecraft.client.gui.GuiComponent.blit;
import static net.minecraft.client.gui.GuiComponent.fill;

@Mixin(LoadingOverlay.class)
public class LoadingOverlayMixin {

    @Shadow @Final static ResourceLocation MOJANG_STUDIOS_LOGO_LOCATION;
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private boolean fadeIn;

    @Shadow private float currentProgress;
    @Shadow private long fadeOutStart = -1L;
    @Shadow private long fadeInStart = -1L;
    @Shadow @Final private ReloadInstance reload;

    @Shadow @Final private Consumer<Optional<Throwable>> onFinish;

    private int lastHeight = 0;
    private int lastWidth = 0;

    private static final ResourceLocation EMPTY_TEXTURE = new ResourceLocation("empty.png");
    private static ResourceLocation ASPECT_1to1_TEXTURE;
    private static ResourceLocation BOSS_BAR_TEXTURE;
    private static ResourceLocation CUSTOM_PROGRESS_BAR_TEXTURE;
    private static ResourceLocation CUSTOM_PROGRESS_BAR_BACKGROUND_TEXTURE;
    private static ResourceLocation BACKGROUND_TEXTURE;

    private static GifTextureRenderer gifBackground, gifLogo;

    @Inject(at = @At("TAIL"), method = "registerTextures", cancellable = true)
    private static void init(Minecraft client, CallbackInfo ci) {
        if (!SimpleSplashScreen.initDone) {
            SimpleSplashScreen.init();
            SimpleSplashScreen.initDone = true;
        }

        ASPECT_1to1_TEXTURE = new ResourceLocation(CS_CONFIG.textures.Aspect1to1Logo);
        BOSS_BAR_TEXTURE = new ResourceLocation(CS_CONFIG.textures.BossBarTexture);
        CUSTOM_PROGRESS_BAR_TEXTURE = new ResourceLocation(CS_CONFIG.textures.CustomBarTexture);
        CUSTOM_PROGRESS_BAR_BACKGROUND_TEXTURE = new ResourceLocation(CS_CONFIG.textures.CustomBarBackgroundTexture);
        BACKGROUND_TEXTURE = new ResourceLocation(CS_CONFIG.textures.BackgroundTexture);

        gifLogo = null;
        gifBackground = null;

        if (ASPECT_1to1_TEXTURE.getPath().endsWith("gif")) {
            gifLogo = new GifTextureRenderer(CS_CONFIG.textures.Aspect1to1Logo, client);
            gifLogo.registerFrames();
        } else {
            client.getTextureManager().register(ASPECT_1to1_TEXTURE, new ConfigTexture(ASPECT_1to1_TEXTURE));
        }

        if (BACKGROUND_TEXTURE.getPath().endsWith("gif")) {
            gifBackground = new GifTextureRenderer(CS_CONFIG.textures.BackgroundTexture, client);
            gifBackground.registerFrames();
        } else {
            client.getTextureManager().register(BACKGROUND_TEXTURE, new ConfigTexture(BACKGROUND_TEXTURE));
        }

        client.getTextureManager().register(CUSTOM_PROGRESS_BAR_TEXTURE, new ConfigTexture(CUSTOM_PROGRESS_BAR_TEXTURE));
        client.getTextureManager().register(CUSTOM_PROGRESS_BAR_BACKGROUND_TEXTURE, new ConfigTexture(CUSTOM_PROGRESS_BAR_BACKGROUND_TEXTURE));

        ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
    private void injectRender(PoseStack poseStack, int p_96179_, int p_96180_, float partialTicks, CallbackInfo ci) {
        ci.cancel();

        int width = this.minecraft.getWindow().getGuiScaledWidth();
        int height = this.minecraft.getWindow().getGuiScaledHeight();

        long millis = Util.getMillis();
        if (this.fadeIn && this.fadeInStart == -1L) {
            this.fadeInStart = millis;
        }

        float fadeOutTime = this.fadeOutStart > -1L ? (float)(millis - this.fadeOutStart) / 1000.0F : -1.0F;
        float fadeInTime = this.fadeInStart > -1L ? (float)(millis - this.fadeInStart) / 500.0F : -1.0F;
        float timedAlpha;

        if (fadeOutTime >= 1.0F) {
            if (this.minecraft.screen != null) {
                this.minecraft.screen.render(poseStack, 0, 0, partialTicks);
            }

            int alpha = Mth.ceil((1.0F - Mth.clamp(fadeOutTime - 1.0F, 0.0F, 1.0F)) * 255.0F);
            fill(poseStack, 0, 0, width, height, replaceAlpha(getBGColor(), alpha));
            timedAlpha = 1.0F - Mth.clamp(fadeOutTime - 1.0F, 0.0F, 1.0F);
        } else if (this.fadeIn) {
            if (this.minecraft.screen != null && fadeInTime < 1.0F) {
                this.minecraft.screen.render(poseStack, p_96179_, p_96180_, partialTicks);
            }

            int alpha = Mth.ceil(Mth.clamp(fadeInTime, 0.15D, 1.0D) * 255.0D);
            fill(poseStack, 0, 0, width, height, replaceAlpha(getBGColor(), alpha));
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

        /* Custom Background */
        if (CS_CONFIG.backgroundImage) {
            if (gifBackground != null) {
                gifBackground.renderNextFrame(poseStack, width, height, timedAlpha);
            } else {
                RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
                RenderSystem.enableBlend();
                RenderSystem.blendEquation(32774);
                RenderSystem.blendFunc(770, 1);
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, timedAlpha);
                blit(poseStack, 0, 0, 0, 0, 0, width, height, width, height);
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableBlend();
            }
        }

        int scaledWidth = (int)((double)this.minecraft.getWindow().getGuiScaledWidth() * 0.5D);
        int scaledHeight = (int)((double)this.minecraft.getWindow().getGuiScaledHeight() * 0.5D);
        double scale = Math.min((double)this.minecraft.getWindow().getGuiScaledWidth() * 0.75D, this.minecraft.getWindow().getGuiScaledHeight()) * 0.25D;
        int i1 = (int)(scale * 0.5D);
        double d0 = scale * 4.0D;
        int j1 = (int)(d0 * 0.5D);

        /* Progress Bar */
        int k1 = (int)((double)this.minecraft.getWindow().getGuiScaledHeight() * 0.8325D);
        float f6 = this.reload.getActualProgress();
        this.currentProgress = Mth.clamp(this.currentProgress * 0.95F + f6 * 0.050000012F, 0.0F, 1.0F);

        if (CS_CONFIG.showProgressText) {
            net.minecraftforge.client.loading.ClientModLoader.renderProgressText();
            fixForgeOverlay();
        }

        if (fadeOutTime < 1.0F) {
            this.drawProgressBar(poseStack, width / 2 - j1, k1 - 5, width / 2 + j1, k1 + 5, 1.0F - Mth.clamp(fadeOutTime, 0.0F, 1.0F));
        }

        /* LOGO */
        if (CS_CONFIG.logoStyle == SimpleSplashScreenConfig.LogoStyle.Mojang) {
            RenderSystem.setShaderTexture(0, MOJANG_STUDIOS_LOGO_LOCATION);
            RenderSystem.enableBlend();
            RenderSystem.blendEquation(32774);
            RenderSystem.blendFunc(770, 1);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, timedAlpha);
            blit(poseStack, scaledWidth - j1, scaledHeight - i1, j1, (int)scale, -0.0625F, 0.0F, 120, 60, 120, 120);
            blit(poseStack, scaledWidth, scaledHeight - i1, j1, (int)scale, 0.0625F, 60.0F, 120, 60, 120, 120);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
        } else if (CS_CONFIG.logoStyle == SimpleSplashScreenConfig.LogoStyle.Aspect1to1 && CS_CONFIG.progressBarType != SimpleSplashScreenConfig.ProgressBarType.Logo) {
            renderLogo(poseStack, timedAlpha, 1);
        }

        if (fadeOutTime >= 2.0F) {
            this.minecraft.setOverlay(null);
        }

        if (this.fadeOutStart == -1L && this.reload.isDone() && (!this.fadeIn || fadeInTime >= 2.0F)) {
            this.fadeOutStart = Util.getMillis(); // Moved up to guard against inf loops caused by callback
            try {
                this.reload.checkExceptions();
                this.onFinish.accept(Optional.empty());
            } catch (Throwable throwable) {
                this.onFinish.accept(Optional.of(throwable));
            }

            if (this.minecraft.screen != null) {
                this.minecraft.screen.init(this.minecraft, this.minecraft.getWindow().getGuiScaledWidth(), this.minecraft.getWindow().getGuiScaledHeight());
            }
        }
    }

    private static int replaceAlpha(int clr, int alpha) {
        return getBGColor() & 16777215 | alpha << 24;
    }

    private static int getBGColor() {
        return CS_CONFIG.backgroundImage ? FastColor.ARGB32.color(0, 0, 0, 0) : CS_CONFIG.backgroundColor;
    }

    private void drawProgressBar(PoseStack matrixStack, int x1, int y1, int x2, int y2, float opacity) {
        int i = Mth.ceil((float)(x2 - x1 - 2) * this.currentProgress);

        if (CS_CONFIG.progressBarType != SimpleSplashScreenConfig.ProgressBarType.Logo && CS_CONFIG.progressBarType != SimpleSplashScreenConfig.ProgressBarType.Background) {
            // Bossbar Progress Bar
            if (CS_CONFIG.progressBarType == SimpleSplashScreenConfig.ProgressBarType.BossBar) {
                RenderSystem.setShaderTexture(0, BOSS_BAR_TEXTURE);

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
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                if (overlay != 0) {
                    blit(matrixStack, x1, y1 + 1, 0, 0, overlay, x2 - x1, (int) ((y2 - y1) / 1.4f), bbHeight, bbWidth);
                }
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableBlend();
            }

            // Custom Progress Bar
            if (CS_CONFIG.progressBarType == SimpleSplashScreenConfig.ProgressBarType.Custom) {

                fixForgeOverlay();
                int customWidth = CS_CONFIG.customProgressBarMode == SimpleSplashScreenConfig.ProgressBarMode.Linear ? x2 - x1 : i;

                if (CS_CONFIG.customProgressBarBackground) {
                    RenderSystem.setShaderTexture(0, CUSTOM_PROGRESS_BAR_BACKGROUND_TEXTURE);
                    blit(matrixStack, x1, y1, 0, 0, 0, x2 - x1, y2 - y1, 10, x2 - x1);
                }

                RenderSystem.setShaderTexture(0, CUSTOM_PROGRESS_BAR_TEXTURE);
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                blit(matrixStack, x1, y1, 0, 0, 0, i, y2 - y1, 10, customWidth);
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
        } else if (CS_CONFIG.progressBarType == SimpleSplashScreenConfig.ProgressBarType.Logo) {
            renderLogo(matrixStack, 1.0F, currentProgress);
        } else {
            renderBackgroundBar(matrixStack, 1.0F, currentProgress);
        }
    }

    private void renderLogo(PoseStack matrixStack, float o, float currentProgress) {
        double d = Math.min((double)this.minecraft.getWindow().getGuiScaledWidth() * 0.75D, this.minecraft.getWindow().getGuiScaledHeight()) * 0.25D;
        int m = (int)((double)this.minecraft.getWindow().getGuiScaledWidth() * 0.5D);
        int r = (int)(d * 0.5D);
        double e = d * 4.0D;
        int s = (int)(e * 0.5D);
        lastHeight = Math.max(Math.round(currentProgress * 512), lastHeight);
        int prog = (int) (((float) lastHeight / 512) * s);

        if (gifLogo != null) {
            gifLogo.renderNextFrame(matrixStack, m - (s / 2), (r + s) - prog, s, s, o, lastHeight);
        } else {
            RenderSystem.setShaderTexture(0, ASPECT_1to1_TEXTURE);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, o);
            blit(matrixStack, m - (s / 2), (r + s) - prog, s, s, 0, 512 - lastHeight, 512, 512, 512, 512);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
        }
    }

    private void renderBackgroundBar(PoseStack matrixStack, float o, float currentProgress) {
        int maxX = this.minecraft.getWindow().getGuiScaledWidth();
        int maxY = this.minecraft.getWindow().getGuiScaledHeight();
        double d = Math.min((double)this.minecraft.getWindow().getGuiScaledWidth() * 0.75D, this.minecraft.getWindow().getGuiScaledHeight()) * 0.25D;
        int m = (int)((double)this.minecraft.getWindow().getGuiScaledWidth() * 0.5D);
        int r = (int)(d * 0.5D);
        double e = d * 4.0D;
        int s = (int)(e * 0.5D);
        lastWidth = Math.max(Math.round(currentProgress * maxX), lastWidth);
        int prog = (int) (((float) lastWidth / maxX) * s);

        RenderSystem.setShaderTexture(0, CUSTOM_PROGRESS_BAR_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, o);
        blit(matrixStack, 0, 0, 0, 0, 0, lastWidth, maxY, maxX, maxY);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void fixForgeOverlay() {
        // The forge Progress Text breaks the custom Image Rendering, so this is a workaround to fix it
        if (CS_CONFIG.showProgressText) {
            RenderSystem.enableTexture();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0f);
        }
    }

}
