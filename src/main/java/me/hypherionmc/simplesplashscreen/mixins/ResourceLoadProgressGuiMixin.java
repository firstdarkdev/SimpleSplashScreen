package me.hypherionmc.simplesplashscreen.mixins;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.hypherionmc.simplesplashscreen.CustomSplashScreen;
import me.hypherionmc.simplesplashscreen.config.CustomSplashScreenConfig;
import me.hypherionmc.simplesplashscreen.textures.BlurredConfigTexture;
import me.hypherionmc.simplesplashscreen.textures.ConfigTexture;
import me.hypherionmc.simplesplashscreen.textures.EmptyTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.LoadingGui;
import net.minecraft.client.gui.ResourceLoadProgressGui;
import net.minecraft.resources.IAsyncReloader;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.function.Consumer;

import static net.minecraft.client.gui.AbstractGui.blit;
import static net.minecraft.client.gui.AbstractGui.fill;

@Mixin(ResourceLoadProgressGui.class)
public class ResourceLoadProgressGuiMixin {

    @Shadow @Final private static ResourceLocation MOJANG_LOGO_TEXTURE;
    @Shadow @Final private Minecraft mc;
    @Shadow @Final private boolean reloading;
    @Shadow private float progress;
    @Shadow private long fadeOutStart = -1L;
    @Shadow private long fadeInStart = -1L;
    @Shadow @Final private IAsyncReloader asyncReloader;
    @Shadow @Final private Consumer<Optional<Throwable>> completedCallback;

    private static final CustomSplashScreenConfig CS_CONFIG = CustomSplashScreen.CS_CONFIG;

    private static final ResourceLocation EMPTY_TEXTURE = new ResourceLocation("empty.png");
    private static final ResourceLocation MOJANG_TEXTURE = new ResourceLocation(CS_CONFIG.textures.MojangLogo);
    private static final ResourceLocation ASPECT_1to1_TEXTURE = new ResourceLocation(CS_CONFIG.textures.Aspect1to1Logo);
    private static final ResourceLocation BOSS_BAR_TEXTURE = new ResourceLocation(CS_CONFIG.textures.BossBarTexture);
    private static final ResourceLocation CUSTOM_PROGRESS_BAR_TEXTURE = new ResourceLocation(CS_CONFIG.textures.CustomBarTexture);
    private static final ResourceLocation CUSTOM_PROGRESS_BAR_BACKGROUND_TEXTURE = new ResourceLocation(CS_CONFIG.textures.CustomBarBackgroundTexture);
    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(CS_CONFIG.textures.BackgroundTexture);


    @Inject(at = @At("TAIL"), method = "loadLogoTexture", cancellable = true)
    private static void init(Minecraft client, CallbackInfo ci) {
        if (CS_CONFIG.logoStyle == CustomSplashScreenConfig.LogoStyle.Mojang) {
            client.getTextureManager().loadTexture(MOJANG_LOGO_TEXTURE, new BlurredConfigTexture(MOJANG_TEXTURE));
        }
        else {
            client.getTextureManager().loadTexture(MOJANG_LOGO_TEXTURE, new EmptyTexture(EMPTY_TEXTURE));
        }
        client.getTextureManager().loadTexture(ASPECT_1to1_TEXTURE, new ConfigTexture(ASPECT_1to1_TEXTURE));
        client.getTextureManager().loadTexture(BACKGROUND_TEXTURE, new ConfigTexture(BACKGROUND_TEXTURE));

        client.getTextureManager().loadTexture(CUSTOM_PROGRESS_BAR_TEXTURE, new ConfigTexture(CUSTOM_PROGRESS_BAR_TEXTURE));
        client.getTextureManager().loadTexture(CUSTOM_PROGRESS_BAR_BACKGROUND_TEXTURE, new ConfigTexture(CUSTOM_PROGRESS_BAR_BACKGROUND_TEXTURE));

        ci.cancel();
    }

    @Inject(at = @At("TAIL"), method = "render", cancellable = false)
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        int i = this.mc.getMainWindow().getScaledWidth();
        int j = this.mc.getMainWindow().getScaledHeight();
        long k = Util.milliTime();
        if (this.reloading && (this.asyncReloader.asyncPartDone() || this.mc.currentScreen != null) && this.fadeInStart == -1L) {
            this.fadeInStart = k;
        }

        float f = this.fadeOutStart > -1L ? (float)(k - this.fadeOutStart) / 1000.0F : -1.0F;
        float f1 = this.fadeInStart > -1L ? (float)(k - this.fadeInStart) / 500.0F : -1.0F;
        float f2;

        // Render our Custom Color
        if (f >= 1.0F) {
            if (this.mc.currentScreen != null) {
                this.mc.currentScreen.render(matrixStack, 0, 0, partialTicks);
            }

            int l = MathHelper.ceil((1.0F - MathHelper.clamp(f1 - 1.0F, 0.0F, 1.0F)) * 255.0F);
            fill(matrixStack, 0, 0, i, j, withAlpha(l));
            f2 = 1.0F - MathHelper.clamp(f - 1.0F, 0.0F, 1.0F);
        } else if (this.reloading) {
            if (this.mc.currentScreen != null && f1 < 1.0F) {
                this.mc.currentScreen.render(matrixStack, mouseX, mouseY, partialTicks);
            }
            int l = MathHelper.ceil((1.0F - MathHelper.clamp(f1 - 1.0F, 0.0F, 1.0F)) * 255.0F);
            fill(matrixStack, 0, 0, i, j, withAlpha(l));
            f2 = MathHelper.clamp(f1, 0.0F, 1.0F);
        } else {
            fill(matrixStack, 0, 0, i, j, withAlpha(255));
            f2 = 1.0F;
        }

        int j2 = (int)((double)this.mc.getMainWindow().getScaledWidth() * 0.5D);
        int i1 = (int)((double)this.mc.getMainWindow().getScaledHeight() * 0.5D);
        double d0 = Math.min((double)this.mc.getMainWindow().getScaledWidth() * 0.75D, (double)this.mc.getMainWindow().getScaledHeight()) * 0.25D;
        int j1 = (int)(d0 * 0.5D);
        double d1 = d0 * 4.0D;
        int k1 = (int)(d1 * 0.5D);

        if (CS_CONFIG.backgroundImage) {
            int l = MathHelper.ceil((1.0F - MathHelper.clamp(f1 - 1.0F, 0.0F, 1.0F)) * 255.0F);
            this.mc.getTextureManager().bindTexture(BACKGROUND_TEXTURE);
            //RenderSystem.enableBlend();
            //RenderSystem.alphaFunc(516, 0.0F);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, l);
            blit(matrixStack, 0, 0, 0, 0, 0, i, j, j, i);
            //RenderSystem.defaultBlendFunc();
            //RenderSystem.defaultAlphaFunc();
            //RenderSystem.disableBlend();
        }

        this.mc.getTextureManager().bindTexture(CS_CONFIG.logoStyle == CustomSplashScreenConfig.LogoStyle.Aspect1to1 ? ASPECT_1to1_TEXTURE : MOJANG_LOGO_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.alphaFunc(516, 0.0F);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, f2);

        if (CS_CONFIG.logoStyle == CustomSplashScreenConfig.LogoStyle.Aspect1to1) {
            blit(matrixStack, j2 - (k1 / 2), j1, k1, k1, 0, 0, 512, 512, 512, 512);
        } else if (CS_CONFIG.logoStyle == CustomSplashScreenConfig.LogoStyle.Mojang) {
            blit(matrixStack, j2 - k1, i1 - j1, k1, (int)d0, -0.0625F, 0.0F, 120, 60, 120, 120);
            blit(matrixStack, j2, i1 - j1, k1, (int)d0, 0.0625F, 60.0F, 120, 60, 120, 120);
        }

        RenderSystem.defaultBlendFunc();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.disableBlend();

        int l1 = (int)((double)this.mc.getMainWindow().getScaledHeight() * 0.8325D);
        float f3 = this.asyncReloader.estimateExecutionSpeed();
        this.progress = MathHelper.clamp(this.progress * 0.95F + f3 * 0.050000012F, 0.0F, 1.0F);

        if (CS_CONFIG.showProgressText) {
            net.minecraftforge.fml.client.ClientModLoader.renderProgressText();
        }

        if (f < 1.0F) {
            this.func_238629_a_(matrixStack, i / 2 - k1, l1 - 5, i / 2 + k1, l1 + 5, 1.0F - MathHelper.clamp(f, 0.0F, 1.0F), null);
        }

        if (f >= 2.0F) {
            this.mc.setLoadingGui((LoadingGui)null);
        }

        if (this.fadeOutStart == -1L && this.asyncReloader.fullyDone() && (!this.reloading || f1 >= 2.0F)) {
            this.fadeOutStart = Util.milliTime(); // Moved up to guard against inf loops caused by callback
            try {
                this.asyncReloader.join();
                this.completedCallback.accept(Optional.empty());
            } catch (Throwable throwable) {
                this.completedCallback.accept(Optional.of(throwable));
            }

            if (this.mc.currentScreen != null) {
                this.mc.currentScreen.init(this.mc, this.mc.getMainWindow().getScaledWidth(), this.mc.getMainWindow().getScaledHeight());
            }
        }
    }

    private static int getBackgroundColor() {
        if (CS_CONFIG.backgroundImage) {
            return ColorHelper.PackedColor.packColor(0, 0, 0, 0);
        }
        else {
            return CustomSplashScreen.CS_CONFIG.backgroundColor;
        }
    }

    private static int withAlpha(int alpha) {
        return getBackgroundColor() | alpha << 24;
    }

    @Inject(at = @At("TAIL"), method = "func_238629_a_", cancellable = false)
    private void func_238629_a_(MatrixStack matrixStack, int x1, int y1, int x2, int y2, float opacity, CallbackInfo ci) {
        int i = MathHelper.ceil((float)(x2 - x1 - 2) * this.progress);

        // Bossbar Progress Bar
        if (CustomSplashScreen.CS_CONFIG.progressBarType == CustomSplashScreenConfig.ProgressBarType.BossBar) {
            this.mc.getTextureManager().bindTexture(BOSS_BAR_TEXTURE);

            int overlay = 0;

            if (CustomSplashScreen.CS_CONFIG.bossBarType == CustomSplashScreenConfig.BossBarType.NOTCHED_6) {overlay = 93;}
            else if (CustomSplashScreen.CS_CONFIG.bossBarType == CustomSplashScreenConfig.BossBarType.NOTCHED_10) {overlay = 105;}
            else if (CustomSplashScreen.CS_CONFIG.bossBarType == CustomSplashScreenConfig.BossBarType.NOTCHED_12) {overlay = 117;}
            else if (CustomSplashScreen.CS_CONFIG.bossBarType == CustomSplashScreenConfig.BossBarType.NOTCHED_20) {overlay = 129;}

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
        if (CustomSplashScreen.CS_CONFIG.progressBarType == CustomSplashScreenConfig.ProgressBarType.Custom) {
            int customWidth = CustomSplashScreen.CS_CONFIG.customProgressBarMode == CustomSplashScreenConfig.ProgressBarMode.Linear ? x2 - x1 : i;
            if (CS_CONFIG.customProgressBarBackground) {
                this.mc.getTextureManager().bindTexture(CUSTOM_PROGRESS_BAR_BACKGROUND_TEXTURE);
                blit(matrixStack, x1, y1, 0, 0, 6, x2 - x1, y2 - y1, 10, x2-x1);
            }
            this.mc.getTextureManager().bindTexture(CUSTOM_PROGRESS_BAR_TEXTURE);
            blit(matrixStack, x1, y1, 0, 0, 6, i, y2 - y1, 10, customWidth);
        }

        // Vanilla / With Color progress bar
        if (CustomSplashScreen.CS_CONFIG.progressBarType == CustomSplashScreenConfig.ProgressBarType.Vanilla) {
            int j = Math.round(opacity * 255.0F);
            int k = CustomSplashScreen.CS_CONFIG.progressBarColor | 255 << 24;
            int kk = CustomSplashScreen.CS_CONFIG.progressFrameColor | 255 << 24;
            fill(matrixStack, x1 + 2, y1 + 2, x1 + i, y2 - 2, k);
            fill(matrixStack, x1 + 1, y1, x2 - 1, y1 + 1, kk);
            fill(matrixStack, x1 + 1, y2, x2 - 1, y2 - 1, kk);
            fill(matrixStack, x1, y1, x1 + 1, y2, kk);
            fill(matrixStack, x2, y1, x2 - 1, y2, kk);
        }
    }
}
