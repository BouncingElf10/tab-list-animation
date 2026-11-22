package com.bouncingelf10.tla.mixin;

import com.bouncingelf10.tla.TabAnimator;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@ModifyExpressionValue(method = "processBlurEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;getMenuBackgroundBlurriness()I"))
	private int tla$tabBlurRadius(int menuBlurRadius) {
		return TabAnimator.HUD.consumeBlurRadius(menuBlurRadius);
	}
}
