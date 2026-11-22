package com.bouncingelf10.tla.config;

import com.bouncingelf10.tla.TabAnimator;
import dev.isxander.yacl3.gui.image.ImageRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class TabPreviewRenderer implements ImageRenderer {
	private static final int W = 256, H = 144;
	private static final String[] NAMES = {"Notch", "jeb_", "Alex", "Steve", "Dinnerbone", "Kai", "Noor", "Sunny"};
	private static final int[] PINGS = {5, 5, 4, 5, 3, 5, 2, 4};
	private static final ResourceLocation HOTBAR = ResourceLocation.withDefaultNamespace("hud/hotbar");

	private final TabAnimator animator;
	private boolean open = true;
	private int waitTicks;

	public TabPreviewRenderer(TlaConfig config) {
		this.animator = new TabAnimator(() -> config);
	}

	public void restart() {
		animator.reset();
		open = true;
		waitTicks = 0;
	}

	@Override
	public void tick() {
		if (waitTicks > 0) {
			if (--waitTicks == 0) open = !open;
		} else if (animator.settled(open)) {
			waitTicks = open ? 25 : 12;
		}
	}

	@Override
	public int render(GuiGraphics graphics, int x, int y, int renderWidth, float tickDelta) {
		int renderHeight = renderWidth * H / W;
		float scale = renderWidth / (float) W;

		graphics.enableScissor(x, y, x + renderWidth, y + renderHeight);
		graphics.pose().pushPose();
		graphics.pose().translate(x, y, 0);
		graphics.pose().scale(scale, scale, 1);

		graphics.fillGradient(0, 0, W, H, 0xFF78A7FF, 0xFFC3D9FF);
		graphics.blitSprite(RenderType::guiTextured, HOTBAR, (W - 182) / 2, H - 23, 182, 22);

		if (animator.update(open)) {
			graphics.pose().pushPose();
			animator.applyListTransform(graphics.pose(), W);
			drawTabList(graphics);
			graphics.pose().popPose();
		}

		graphics.pose().popPose();
		graphics.disableScissor();
		return renderHeight;
	}

	private void drawTabList(GuiGraphics g) {
		Font font = Minecraft.getInstance().font;
		Component header = Component.literal("Tab List Animation").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
		Component footer = Component.literal("play.example.net").withStyle(ChatFormatting.GRAY);

		int nameWidth = 0;
		for (String name : NAMES) nameWidth = Math.max(nameWidth, font.width(name));
		int rowWidth = Math.max(9 + nameWidth + 1 + 13, Math.max(font.width(header), font.width(footer)));
		int left = (W - rowWidth) / 2;
		int top = 10;

		animator.beginList();
		g.fill(left - 1, top - 1, left + rowWidth + 1, top + 9, animator.tint(0x80000000));
		drawText(g, font, header, (W - font.width(header)) / 2, top);
		top += 10;

		g.fill(left - 1, top - 1, left + rowWidth + 1, top + NAMES.length * 9, animator.tint(0x80000000));
		for (int i = 0; i < NAMES.length; i++) {
			int rowY = top + i * 9;
			animator.beginRow(g.pose());
			g.fill(left, rowY, left + rowWidth, rowY + 8, animator.tint(0x20FFFFFF));
			UUID uuid = UUID.nameUUIDFromBytes(NAMES[i].getBytes(StandardCharsets.UTF_8));
			PlayerFaceRenderer.draw(g, DefaultPlayerSkin.get(uuid).texture(), left, rowY, 8, true, NAMES[i].equals("Dinnerbone"), animator.tint(0xFFFFFFFF));
			drawText(g, font, Component.literal(NAMES[i]), left + 9, rowY);
			g.blitSprite(RenderType::guiTextured, ResourceLocation.withDefaultNamespace("icon/ping_" + PINGS[i]), left + rowWidth - 11, rowY, 10, 8, ARGB.white(animator.alpha()));
		}
		animator.endRow(g.pose());

		top += NAMES.length * 9 + 1;
		g.fill(left - 1, top - 1, left + rowWidth + 1, top + 9, animator.tint(0x80000000));
		drawText(g, font, footer, (W - font.width(footer)) / 2, top);
	}

	private void drawText(GuiGraphics g, Font font, Component text, int x, int y) {
		int color = animator.tintText(0xFFFFFFFF);
		if (color != 0) g.drawString(font, text, x, y, color);
	}

	@Override
	public void close() { }
}
