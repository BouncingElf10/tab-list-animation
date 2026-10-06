package com.bouncingelf10.tla;

import com.bouncingelf10.tla.config.TlaConfig;
import dev.bouncingelf10.timelesslib.api.animation.Easing;
import dev.bouncingelf10.timelesslib.api.clock.TimeSources;
import net.minecraft.util.ARGB;
import org.joml.Matrix3x2fStack;

import java.util.function.Supplier;

public final class TabAnimator {
	public static final TabAnimator HUD = new TabAnimator(TlaConfig.HANDLER::instance);
	public static TabAnimator drawing;

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
		if (target != opening) {
			double v = value();
			opening = target;
			progress = solveProgress(openEasing, closeEasing, opening, v, progress);
		}
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
		float inv = (float) (1 - v);
		if (c.slideEnabled) pose.translate(c.slideDirection.dx * c.slideDistance * inv, c.slideDirection.dy * c.slideDistance * inv);
		if (c.scaleEnabled) {
			float s = (float) Math.max(0, c.scaleFrom / 100.0 + (1 - c.scaleFrom / 100.0) * v);
			pose.translate(width / 2f, 0);
			pose.scale(c.scaleAxis.x ? s : 1, c.scaleAxis.y ? s : 1);
			pose.translate(-width / 2f, 0);
		}
	}

	public int tint(int argb) {
		TlaConfig c = config.get();
		float alpha = c.enabled && c.fadeEnabled ? (float) clamp01(value()) : 1;
		if (alpha >= 1) return argb;
		int out = ARGB.multiplyAlpha(argb, alpha);
		return ARGB.alpha(out) < 4 ? out & 0xFFFFFF : out;
	}

	public float alpha() {
		return ARGB.alpha(tint(0xFFFFFFFF)) / 255f;
	}

	public static double value(Easing open, Easing close, boolean opening, double p) {
		return opening ? open.apply(p) : 1 - close.apply(1 - p);
	}

	public static double solveProgress(Easing open, Easing close, boolean opening, double v, double hint) {
		int samples = 64;
		double best = hint, bestErr = Double.MAX_VALUE, bestRoot = -1;
		double prevP = 0, prevD = value(open, close, opening, 0) - v;
		for (int i = 0; i <= samples; i++) {
			double p = i / (double) samples;
			double d = value(open, close, opening, p) - v;
			if (Math.abs(d) < bestErr) {
				best = p;
				bestErr = Math.abs(d);
			}
			if (i > 0 && (d == 0 || (prevD < 0) != (d < 0))) {
				double root = bisect(open, close, opening, v, prevP, p, prevD);
				if (bestRoot < 0 || Math.abs(root - hint) < Math.abs(bestRoot - hint)) bestRoot = root;
			}
			prevP = p;
			prevD = d;
		}
		return bestRoot >= 0 ? bestRoot : best;
	}

	private static double bisect(Easing open, Easing close, boolean opening, double v, double lo, double hi, double loD) {
		for (int i = 0; i < 40; i++) {
			double mid = (lo + hi) / 2;
			double d = value(open, close, opening, mid) - v;
			if ((d < 0) == (loD < 0)) {
				lo = mid;
				loD = d;
			} else {
				hi = mid;
			}
		}
		return (lo + hi) / 2;
	}

	private static double clamp01(double x) {
		return x < 0 ? 0 : x > 1 ? 1 : x;
	}
}
