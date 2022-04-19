package me.hypherionmc.simplesplashscreen.client.mixins;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.hypherionmc.simplesplashscreen.SimpleSplashScreen;
import me.hypherionmc.simplesplashscreen.client.config.SimpleSplashScreenConfig;
import me.hypherionmc.simplesplashscreen.client.textures.BlurredConfigTexture;
import me.hypherionmc.simplesplashscreen.client.textures.ConfigTexture;
import me.hypherionmc.simplesplashscreen.client.textures.EmptyTexture;
import me.hypherionmc.simplesplashscreen.client.textures.GifTextureRenderer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.loading.ClientModLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.hypherionmc.simplesplashscreen.SimpleSplashScreen.CS_CONFIG;
import static net.minecraft.client.gui.GuiComponent.blit;
import static net.minecraft.client.gui.GuiComponent.fill;

@OnlyIn(Dist.CLIENT)
@Mixin(LoadingOverlay.class)
public class ResourceLoadProgressGuiMixin {

    @Shadow @Final static ResourceLocation MOJANG_STUDIOS_LOGO_LOCATION;
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private boolean fadeIn;
    @Shadow private float currentProgress;
    @Shadow private final long fadeOutStart = -1L;
    @Shadow private long fadeInStart = -1L;
    @Shadow @Final private ReloadInstance reload;

    private int lastHeight = 0;
    private int lastWidth = 0;

    private static final ResourceLocation EMPTY_TEXTURE = new ResourceLocation("empty.png");
    private static ResourceLocation MOJANG_TEXTURE;
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

        MOJANG_TEXTURE = new ResourceLocation(CS_CONFIG.textures.MojangLogo);
        ASPECT_1to1_TEXTURE = new ResourceLocation(CS_CONFIG.textures.Aspect1to1Logo);
        BOSS_BAR_TEXTURE = new ResourceLocation(CS_CONFIG.textures.BossBarTexture);
        CUSTOM_PROGRESS_BAR_TEXTURE = new ResourceLocation(CS_CONFIG.textures.CustomBarTexture);
        CUSTOM_PROGRESS_BAR_BACKGROUND_TEXTURE = new ResourceLocation(CS_CONFIG.textures.CustomBarBackgroundTexture);
        BACKGROUND_TEXTURE = new ResourceLocation(CS_CONFIG.textures.BackgroundTexture);

        gifLogo = null;
        gifBackground = null;

        if (CS_CONFIG.logoStyle == SimpleSplashScreenConfig.LogoStyle.Mojang) {
            client.getTextureManager().register(MOJANG_STUDIOS_LOGO_LOCATION, new BlurredConfigTexture(MOJANG_TEXTURE));
        } else {
            client.getTextureManager().register(MOJANG_STUDIOS_LOGO_LOCATION, new EmptyTexture(EMPTY_TEXTURE));
        }

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

    // Render Custom Background
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V"))
    public void renderBackground(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        int maxX = this.minecraft.getWindow().getGuiScaledWidth();
        int maxY = this.minecraft.getWindow().getGuiScaledHeight();

        long l = Util.getMillis();
        if (this.fadeIn && (this.reload.isDone() || this.minecraft.screen != null) && this.fadeInStart == -1L) {
            this.fadeInStart = l;
        }

        float f = this.fadeOutStart > -1L ? (float)(l - this.fadeOutStart) / 1000.0F : -1.0F;
        float g = this.fadeInStart > -1L ? (float)(l - this.fadeInStart) / 500.0F : -1.0F;
        float o;
        int m;

        if (f >= 1.0F) {
            if (this.minecraft.screen != null) {
                this.minecraft.screen.render(matrixStack, 0, 0, partialTicks);
            }

            m = Mth.ceil((1.0F - Mth.clamp(f - 1.0F, 0.0F, 1.0F)) * 255.0F);
            fill(matrixStack, 0, 0, maxX, maxY, withAlpha(m));
            o = 1.0F - Mth.clamp(f - 1.0F, 0.0F, 1.0F);
        } else if (this.fadeIn) {
            if (this.minecraft.screen != null && g < 1.0F) {
                this.minecraft.screen.render(matrixStack, mouseX, mouseY, partialTicks);
            }

            m = Mth.ceil(Mth.clamp((double)g, 0.15D, 1.0D) * 255.0D);
            fill(matrixStack, 0, 0, maxX, maxY, withAlpha(m));
            o = Mth.clamp(g, 0.0F, 1.0F);
        } else {
            m = getBackgroundColor();
            float p = (float)(m >> 16 & 255) / 255.0F;
            float q = (float)(m >> 8 & 255) / 255.0F;
            float r = (float)(m & 255) / 255.0F;
            GlStateManager._clearColor(p, q, r, 1.0F);
            GlStateManager._clear(16384, Minecraft.ON_OSX);
            o = 1.0F;
        }

        if (CS_CONFIG.backgroundImage) {
            if (gifBackground != null) {
                gifBackground.renderNextFrame(matrixStack, maxX, maxY, o);
            } else {
                RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
                RenderSystem.enableBlend();
                RenderSystem.blendEquation(32774);
                RenderSystem.blendFunc(770, 1);
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, o);
                blit(matrixStack, 0, 0, 0, 0, 0, maxX, maxY, maxX, maxY);
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableBlend();
            }
        }
    }

    // Render Custom Logo
    @Inject(at = @At("TAIL"), method = "render")
    public void renderLogo(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        long l = Util.getMillis();
        if (this.fadeIn && (this.reload.isDone() || this.minecraft.screen != null) && this.fadeInStart == -1L) {
            this.fadeInStart = l;
        }
        float f = this.fadeOutStart > -1L ? (float)(l - this.fadeOutStart) / 1000.0F : -1.0F;
        float g = this.fadeInStart > -1L ? (float)(l - this.fadeInStart) / 500.0F : -1.0F;
        float o;

        if (f >= 1.0F) {
            o = 1.0F - Mth.clamp(f - 1.0F, 0.0F, 1.0F);
        } else if (this.fadeIn) {
            o = Mth.clamp(g, 0.0F, 1.0F);
        } else {
            o = 1.0F;
        }

        if (CS_CONFIG.logoStyle == SimpleSplashScreenConfig.LogoStyle.Aspect1to1) {
            fixForgeOverlay();
            if (CS_CONFIG.progressBarType != SimpleSplashScreenConfig.ProgressBarType.Logo) {
                renderLogo(matrixStack, o, 1);
            }
        }
    }

    // Modify Background Color
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/LoadingOverlay;fill(Lcom/mojang/blaze3d/vertex/PoseStack;IIIII)V"))
    private void modifyBackgroundColor(PoseStack matrixStack, int minX, int minY, int maxX, int maxY, int color) {
        long l = Util.getMillis();
        if (this.fadeIn && (this.reload.isDone() || this.minecraft.screen != null) && this.fadeInStart == -1L) {
            this.fadeInStart = l;
        }
        float f = this.fadeOutStart > -1L ? (float)(l - this.fadeOutStart) / 1000.0F : -1.0F;
        int m;
        m = Mth.ceil((1.0F - Mth.clamp(f - 1.0F, 0.0F, 1.0F)) * 255.0F);

        int clr = color;

        if (CS_CONFIG.backgroundImage) {
            clr = FastColor.ARGB32.color(0, 0, 0, 0);
        }
        else {
            clr = CS_CONFIG.backgroundColor | m << 24;
        }

        fill(matrixStack, minX, minY, maxX, maxY, clr);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/LoadingOverlay;drawProgressBar(Lcom/mojang/blaze3d/vertex/PoseStack;IIIIF)V"))
    private void renderProgressBar(LoadingOverlay resourceLoadProgressGui, PoseStack matrixStack, int x1, int y1, int x2, int y2, float opacity) {
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

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setOverlay(Lnet/minecraft/client/gui/screens/Overlay;)V"))
    public void setLoadingGui(Minecraft minecraft, Overlay loadingGuiIn) {
        this.minecraft.setOverlay(null);
        if (gifBackground != null) {
            gifBackground.unloadAll();
        }
        if (gifLogo != null) {
            gifLogo.unloadAll();
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/loading/ClientModLoader;renderProgressText()V"))
    private void renderProgressTextForge() {
        if (CS_CONFIG.showProgressText) {
            ClientModLoader.renderProgressText();
        }
    }

    private static int getBackgroundColor() {
        if (CS_CONFIG.backgroundImage) {
            return FastColor.ARGB32.color(0, 0, 0, 0);
        } else {
            return SimpleSplashScreen.CS_CONFIG.backgroundColor;
        }
    }

    private static int withAlpha(int alpha) {
        return getBackgroundColor() | alpha << 24;
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
        //blit(matrixStack, m - (s / 2), (r + s) - prog, s, s, 0, 512 - lastHeight, 512, 512, 512, 512);
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
