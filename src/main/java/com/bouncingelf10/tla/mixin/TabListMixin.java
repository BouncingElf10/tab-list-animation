package com.bouncingelf10.tla.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Hud.class)
public class TabListMixin {
	@Unique
	private static final float DURATION = 0.2f;
	@Unique
	private float progress;
	@Unique
	private long lastNanos;

	@WrapOperation(method = "extractTabList", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z"))
	private boolean tla$keepOpen(KeyMapping key, Operation<Boolean> original) {
		boolean down = original.call(key);
		long now = System.nanoTime();
		float dt = Math.min((now - lastNanos) / 1e9f, 0.1f);
		lastNanos = now;
		progress = Math.clamp(progress + (down ? dt : -dt) / DURATION, 0f, 1f);
		return down || progress > 0;
	}

	@WrapOperation(method = "extractTabList", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;ILnet/minecraft/world/scores/Scoreboard;Lnet/minecraft/world/scores/Objective;)V"))
	private void tla$animate(PlayerTabOverlay tabList, GuiGraphicsExtractor graphics, int width, Scoreboard scoreboard, Objective objective, Operation<Void> original) {
		float t = 1 - (float) Math.pow(1 - progress, 3);
		graphics.pose().pushMatrix();
		graphics.pose().translate(width / 2f, 0);
		graphics.pose().scale(t, t);
		graphics.pose().translate(-width / 2f, 0);
		original.call(tabList, graphics, width, scoreboard, objective);
		graphics.pose().popMatrix();
	}
}
