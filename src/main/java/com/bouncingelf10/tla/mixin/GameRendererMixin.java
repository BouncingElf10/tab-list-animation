package com.bouncingelf10.tla.mixin;

import com.bouncingelf10.tla.TabAnimator;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@ModifyArg(method = "render", index = 5, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlobalSettingsUniform;update(IIDJFILnet/minecraft/world/phys/Vec3;Z)V"))
	private int tla$tabBlurRadius(int menuBlurRadius) {
		return TabAnimator.HUD.consumeBlurRadius(menuBlurRadius);
	}
}
