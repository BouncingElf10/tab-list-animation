package com.bouncingelf10.tla.config;

import dev.bouncingelf10.timelesslib.api.animation.Easing;

public enum EasingType implements TlaConfig.Named {
	LINEAR,
	EASE_IN_SINE, EASE_OUT_SINE, EASE_IN_OUT_SINE,
	EASE_IN_QUAD, EASE_OUT_QUAD, EASE_IN_OUT_QUAD,
	EASE_IN_CUBIC, EASE_OUT_CUBIC, EASE_IN_OUT_CUBIC,
	EASE_IN_QUART, EASE_OUT_QUART, EASE_IN_OUT_QUART,
	EASE_IN_QUINT, EASE_OUT_QUINT, EASE_IN_OUT_QUINT,
	EASE_IN_EXPO, EASE_OUT_EXPO, EASE_IN_OUT_EXPO,
	EASE_IN_CIRC, EASE_OUT_CIRC, EASE_IN_OUT_CIRC,
	EASE_IN_BACK, EASE_OUT_BACK, EASE_IN_OUT_BACK,
	EASE_IN_ELASTIC, EASE_OUT_ELASTIC, EASE_IN_OUT_ELASTIC,
	EASE_IN_BOUNCE, EASE_OUT_BOUNCE, EASE_IN_OUT_BOUNCE,
	CUSTOM;

	private Easing easing;

	static {
		for (EasingType type : values()) {
			if (type == CUSTOM) continue;
			try {
				type.easing = (Easing) Easing.class.getField(type.name()).get(null);
			} catch (ReflectiveOperationException e) {
				throw new IllegalStateException("TimelessLib has no easing " + type.name(), e);
			}
		}
	}

	public Easing toEasing(double x1, double y1, double x2, double y2) {
		return this == CUSTOM ? Easing.createBezier(x1, y1, x2, y2) : easing;
	}
}
