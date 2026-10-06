package com.bouncingelf10.tla.config;

import com.bouncingelf10.tla.TabListAnimationClient;
import dev.isxander.yacl3.api.NameableEnum;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public class TlaConfig {
	public static final ConfigClassHandler<TlaConfig> HANDLER = ConfigClassHandler.createBuilder(TlaConfig.class)
			.id(TabListAnimationClient.id("config"))
			.serializer(handler -> GsonConfigSerializerBuilder.create(handler)
					.setPath(FabricLoader.getInstance().getConfigDir().resolve(TabListAnimationClient.MOD_ID + ".json5"))
					.setJson5(true)
					.build())
			.build();

	@SerialEntry public boolean enabled = true;
	@SerialEntry public KeyBehaviour keyBehaviour = KeyBehaviour.HOLD;
	@SerialEntry public double openDuration = 0.2;
	@SerialEntry public double closeDuration = 0.2;
	@SerialEntry public EasingType openEasing = EasingType.EASE_OUT_CUBIC;
	@SerialEntry public EasingType closeEasing = EasingType.EASE_IN_CUBIC;

	@SerialEntry public double openX1 = 0.34;
	@SerialEntry public double openY1 = 1.56;
	@SerialEntry public double openX2 = 0.64;
	@SerialEntry public double openY2 = 1.0;
	@SerialEntry public double closeX1 = 0.36;
	@SerialEntry public double closeY1 = 0.0;
	@SerialEntry public double closeX2 = 0.66;
	@SerialEntry public double closeY2 = -0.56;

	@SerialEntry public boolean scaleEnabled = true;
	@SerialEntry public int scaleFrom = 0;
	@SerialEntry public ScaleAxis scaleAxis = ScaleAxis.BOTH;

	@SerialEntry public boolean slideEnabled = false;
	@SerialEntry public Direction slideDirection = Direction.TOP;
	@SerialEntry public int slideDistance = 40;

	@SerialEntry public boolean fadeEnabled = false;

	@SerialEntry public boolean rowsEnabled = false;
	@SerialEntry public double rowDelay = 4;
	@SerialEntry public Direction rowDirection = Direction.LEFT;
	@SerialEntry public int rowDistance = 20;
	@SerialEntry public boolean rowFade = true;

	public interface Named extends NameableEnum {
		@Override
		default Component getDisplayName() {
			Enum<?> e = (Enum<?>) this;
			String fallback = Arrays.stream(e.name().split("_"))
					.map(w -> w.charAt(0) + w.substring(1).toLowerCase(Locale.ROOT))
					.collect(Collectors.joining(" "));
			return Component.translatableWithFallback(TabListAnimationClient.MOD_ID + ".enum." + e.getDeclaringClass().getSimpleName().toLowerCase(Locale.ROOT) + "." + e.name().toLowerCase(Locale.ROOT), fallback);
		}
	}

	public enum KeyBehaviour implements Named { HOLD, TOGGLE }

	public enum ScaleAxis implements Named {
		BOTH(true, true), HORIZONTAL(true, false), VERTICAL(false, true);

		public final boolean x, y;

		ScaleAxis(boolean x, boolean y) {
			this.x = x;
			this.y = y;
		}
	}

	public enum Direction implements Named {
		TOP(0, -1), BOTTOM(0, 1), LEFT(-1, 0), RIGHT(1, 0);

		public final int dx, dy;

		Direction(int dx, int dy) {
			this.dx = dx;
			this.dy = dy;
		}
	}
}
