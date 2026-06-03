package com.devorcification.mixin;

import com.devorcification.Devorcification;
import com.devorcification.render.ShaderManager;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class ShaderUniformMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void devorcification$logState(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        if (!ShaderManager.enabled()) return;
        if (ShaderManager.masterIntensity > 0.5f) {
            Devorcification.LOGGER.debug("[Devorcification Shader] master={} vignette={} chroma={} grain={} fog={}",
                ShaderManager.masterIntensity,
                ShaderManager.effectiveVignette(),
                ShaderManager.effectiveChromatic(),
                ShaderManager.effectiveGrain(),
                ShaderManager.effectiveFog());
        }
    }
}
