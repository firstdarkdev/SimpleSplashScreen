package me.hypherionmc.simplesplashscreen.client.mixins;

import me.hypherionmc.simplesplashscreen.SimpleSplashScreen;
import net.minecraftforge.fml.client.EarlyLoaderGUI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EarlyLoaderGUI.class)
public abstract class EarlyLoaderGuiMixin {

    @Shadow protected abstract void renderMessages();

    @Inject(method = "renderFromGUI", at = @At("HEAD"), cancellable = true, remap = false)
    private void renderFromGUI(CallbackInfo ci) {
        if (SimpleSplashScreen.CS_CONFIG.showProgressText) {
            renderMessages();
        } else {
            ci.cancel();
        }
    }

}
