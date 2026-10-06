package com.bouncingelf10.tla.mixin;

import com.bouncingelf10.tla.TabAnimator;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.resources.Identifier;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {
	@Unique private static final String FILL = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V";
	@Unique private static final String RENDER = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;ILnet/minecraft/world/scores/Scoreboard;Lnet/minecraft/world/scores/Objective;)V";

	@Unique
    private static int tla$tint(int color) {
		TabAnimator a = TabAnimator.drawing;
		return a == null ? color : a.tint(color);
	}

	@ModifyArg(method = RENDER, index = 4, at = @At(value = "INVOKE", target = FILL, ordinal = 2))
	private int tla$fadeRows(int color) {
		return tla$tint(color);
	}

	@ModifyArg(method = RENDER, index = 4,
			slice = @Slice(to = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;getBackgroundColor(I)I")),
			at = @At(value = "INVOKE", target = FILL))
	private int tla$fadeBackgrounds(int color) {
		return tla$tint(color);
	}

	@ModifyArg(method = RENDER, index = 4,
			slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;extractPingIcon(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIILnet/minecraft/client/multiplayer/PlayerInfo;)V")),
			at = @At(value = "INVOKE", target = FILL))
	private int tla$fadeFooter(int color) {
		return tla$tint(color);
	}

	@ModifyArg(method = {RENDER, "extractTablistScore", "extractTablistHearts"}, index = 4, require = 1,
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"))
	private int tla$fadeText(int color) {
		return tla$tint(color);
	}

	@ModifyArg(method = RENDER, index = 4,
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)V"))
	private int tla$fadeLines(int color) {
		return tla$tint(color);
	}

	@ModifyArg(method = RENDER, index = 7,
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/PlayerFaceExtractor;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/resources/Identifier;IIIZZI)V"))
	private int tla$fadeFace(int color) {
		return tla$tint(color);
	}

	@WrapOperation(method = {"extractPingIcon", "extractTablistHearts"}, require = 1,
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"))
	private void tla$fadeSprites(GuiGraphicsExtractor graphics, RenderPipeline pipeline, Identifier sprite, int x, int y, int w, int h, Operation<Void> original) {
		TabAnimator a = TabAnimator.drawing;
		float alpha = a == null ? 1 : a.alpha();
		if (alpha >= 1) original.call(graphics, pipeline, sprite, x, y, w, h);
		else graphics.blitSprite(pipeline, sprite, x, y, w, h, alpha);
	}
}
