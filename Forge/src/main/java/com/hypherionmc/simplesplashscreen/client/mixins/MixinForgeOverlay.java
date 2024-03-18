package com.hypherionmc.simplesplashscreen.client.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraftforge.client.loading.ForgeLoadingOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author HypherionSA
 * NEIN! Forge Loading Overlay. You no like customization, me no like you
 */
@Mixin(ForgeLoadingOverlay.class)
public class MixinForgeOverlay extends LoadingOverlay {

    public MixinForgeOverlay(Minecraft p_96172_, ReloadInstance p_96173_, Consumer<Optional<Throwable>> p_96174_, boolean p_96175_) {
        super(p_96172_, p_96173_, p_96174_, p_96175_);
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void cancelForgeLoading(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        ci.cancel();
        super.render(graphics, mouseX, mouseY, partialTick);
    }

}
