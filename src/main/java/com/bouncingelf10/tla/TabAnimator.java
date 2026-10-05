package com.bouncingelf10.tla;

import com.bouncingelf10.tla.config.TlaConfig;
import dev.bouncingelf10.timelesslib.api.animation.Easing;
import dev.bouncingelf10.timelesslib.api.clock.TimeSources;
import org.joml.Matrix3x2fStack;

import java.util.function.Supplier;

public final class TabAnimator {
	public static final TabAnimator HUD = new TabAnimator(TlaConfig.HANDLER::instance);

	private final Supplier<TlaConfig> config;
	private double progress;
	private boolean opening;
	private long lastNanos = -1;
	private boolean toggled, wasDown;
	private Easing openEasing = Easing.LINEAR, closeEasing = Easing.LINEAR;

	public TabAnimator(Supplier<TlaConfig> config) {
		this.config = config;
	}

	public boolean resolveTarget(boolean keyDown) {
		boolean pressed = keyDown && !wasDown;
		wasDown = keyDown;
		if (config.get().keyBehaviour == TlaConfig.KeyBehaviour.HOLD) return keyDown;
		if (pressed) toggled = !toggled;
		return toggled;
	}

	public boolean update(boolean target) {
		TlaConfig c = config.get();
		openEasing = c.openEasing.toEasing(c.openX1, c.openY1, c.openX2, c.openY2);
		closeEasing = c.closeEasing.toEasing(c.closeX1, c.closeY1, c.closeX2, c.closeY2);

		long now = TimeSources.REAL_TIME.now();
		double dt = lastNanos < 0 ? 0 : Math.min((now - lastNanos) / 1e9, 0.1);
		lastNanos = now;

		if (!c.enabled) {
			opening = target;
			progress = target ? 1 : 0;
			return target;
		}
		opening = target;
		double duration = opening ? c.openDuration : c.closeDuration;
		progress = duration <= 0 ? (opening ? 1 : 0) : clamp01(progress + (opening ? dt : -dt) / duration);
		return target || progress > 0;
	}

	public double value() {
		return value(openEasing, closeEasing, opening, progress);
	}

	public void applyListTransform(Matrix3x2fStack pose, float width) {
		TlaConfig c = config.get();
		if (!c.enabled) return;
		double v = value();
		if (c.scaleEnabled) {
			float s = (float) Math.max(0, c.scaleFrom / 100.0 + (1 - c.scaleFrom / 100.0) * v);
			pose.translate(width / 2f, 0);
			pose.scale(c.scaleAxis.x ? s : 1, c.scaleAxis.y ? s : 1);
			pose.translate(-width / 2f, 0);
		}
	}

	public static double value(Easing open, Easing close, boolean opening, double p) {
		return opening ? open.apply(p) : 1 - close.apply(1 - p);
	}

	private static double clamp01(double x) {
		return x < 0 ? 0 : x > 1 ? 1 : x;
	}
}
