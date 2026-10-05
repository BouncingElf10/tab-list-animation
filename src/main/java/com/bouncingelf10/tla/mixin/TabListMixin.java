package com.bouncingelf10.tla.mixin;

import com.bouncingelf10.tla.TabAnimator;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Hud.class)
public class TabListMixin {
	@WrapOperation(method = "extractTabList", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z"))
	private boolean tla$keepOpen(KeyMapping key, Operation<Boolean> original) {
		return TabAnimator.HUD.update(original.call(key));
	}

	@WrapOperation(method = "extractTabList", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;ILnet/minecraft/world/scores/Scoreboard;Lnet/minecraft/world/scores/Objective;)V"))
	private void tla$animate(PlayerTabOverlay tabList, GuiGraphicsExtractor graphics, int width, Scoreboard scoreboard, Objective objective, Operation<Void> original) {
		graphics.pose().pushMatrix();
		TabAnimator.HUD.applyListTransform(graphics.pose(), width);
		original.call(tabList, graphics, width, scoreboard, objective);
		graphics.pose().popMatrix();
	}
}
