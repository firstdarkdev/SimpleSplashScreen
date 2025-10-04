package com.hypherionmc.simplesplashscreen.client.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.neoforged.fml.earlydisplay.DisplayWindow;
import net.neoforged.neoforge.client.loading.NeoForgeLoadingOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author HypherionSA
 * NEIN! NeoForge Loading Overlay. You no like customization, me no like you
 */
@Mixin(NeoForgeLoadingOverlay.class)
public class MixinForgeOverlay extends LoadingOverlay {

    public MixinForgeOverlay(Minecraft mc, ReloadInstance reloader, Consumer<Optional<Throwable>> errorConsumer, DisplayWindow displayWindow) {
        super(mc, reloader, errorConsumer, false);
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void cancelForgeLoading(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        ci.cancel();
        super.render(graphics, mouseX, mouseY, partialTick);
    }

}
