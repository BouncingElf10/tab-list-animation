package com.bouncingelf10.tla.config;

import com.bouncingelf10.tla.TabListAnimationClient;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.Screen;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public final class TlaConfigScreen {
	private static final String KEY = TabListAnimationClient.MOD_ID + ".";

	private final TlaConfig config = TlaConfig.HANDLER.instance();
	private final TlaConfig defaults = TlaConfig.HANDLER.defaults();

	public static Screen create(Screen parent) {
		return new TlaConfigScreen().build().generateScreen(parent);
	}

	private YetAnotherConfigLib build() {
		Option<Boolean> enabled = bool("enabled");
		Option<EasingType> openEasing = opt("openEasing", EnumDropdownControllerBuilder::create);
		Option<EasingType> closeEasing = opt("closeEasing", EnumDropdownControllerBuilder::create);
		List<Option<Double>> openCurve = List.of(bezier("openX1", false), bezier("openY1", true), bezier("openX2", false), bezier("openY2", true));
		List<Option<Double>> closeCurve = List.of(bezier("closeX1", false), bezier("closeY1", true), bezier("closeX2", false), bezier("closeY2", true));
		when(openEasing, e -> e == EasingType.CUSTOM, openCurve);
		when(closeEasing, e -> e == EasingType.CUSTOM, closeCurve);

		Option<Boolean> scale = bool("scaleEnabled");
		Option<Boolean> slide = bool("slideEnabled");
		Option<Boolean> rows = bool("rowsEnabled");
		Option<Boolean> blur = bool("blurEnabled");
		List<Option<?>> scaleOpts = List.of(intOpt("scaleFrom", 0, 150, 5, "%d%%"), enumOpt("scaleAxis", TlaConfig.ScaleAxis.class));
		List<Option<?>> slideOpts = List.of(enumOpt("slideDirection", TlaConfig.Direction.class), intOpt("slideDistance", 0, 300, 5, "%dpx"));
		Option<Double> rowDelay = opt("rowDelay", o -> DoubleSliderControllerBuilder.create(o).range(0.0, 20.0).step(0.5).formatValue(v -> Component.literal("%.1f%%".formatted(v))));
		List<Option<?>> rowOpts = List.of(rowDelay, enumOpt("rowDirection", TlaConfig.Direction.class), intOpt("rowDistance", 0, 100, 2, "%dpx"), bool("rowFade"));
		List<Option<?>> blurOpts = List.of(intOpt("blurStrength", 1, 10, 1, "%d"));
		when(scale, v -> v, scaleOpts);
		when(slide, v -> v, slideOpts);
		when(rows, v -> v, rowOpts);
		when(blur, v -> v, blurOpts);

		Option<Double> openDuration = opt("openDuration", this::seconds);
		Option<Double> closeDuration = opt("closeDuration", this::seconds);
		List<Option<?>> timing = List.of(enabled, enumOpt("keyBehaviour", TlaConfig.KeyBehaviour.class), openDuration, closeDuration, openEasing, closeEasing);

		return YetAnotherConfigLib.createBuilder()
				.title(Component.translatable(KEY + "title"))
				.category(ConfigCategory.createBuilder()
						.name(Component.translatable(KEY + "category.animation"))
						.group(group("timing", timing))
						.group(groupBuilder("open_curve", openCurve).collapsed(true).build())
						.group(groupBuilder("close_curve", closeCurve).collapsed(true).build())
						.build())
				.category(ConfigCategory.createBuilder()
						.name(Component.translatable(KEY + "category.effects"))
						.group(group("scale", concat(scale, scaleOpts)))
						.group(group("slide", concat(slide, slideOpts)))
						.group(group("fade", List.of(bool("fadeEnabled"))))
						.group(group("rows", concat(rows, rowOpts)))
						.group(group("blur", concat(blur, blurOpts)))
						.build())
				.save(TlaConfig.HANDLER::save)
				.build();
	}

	@SuppressWarnings("unchecked")
	private <T> Option<T> opt(String fieldName, Function<Option<T>, ControllerBuilder<T>> controller) {
		Field field;
		try {
			field = TlaConfig.class.getField(fieldName);
		} catch (NoSuchFieldException e) {
			throw new IllegalArgumentException(fieldName, e);
		}
		return Option.<T>createBuilder()
				.name(Component.translatable(KEY + "option." + fieldName))
				.description(OptionDescription.of(Component.translatable(KEY + "option." + fieldName + ".desc")))
				.binding((T) get(field, defaults), () -> (T) get(field, config), v -> set(field, config, v))
				.controller(controller)
				.build();
	}

	private static <T> void when(Option<T> parent, Predicate<T> test, List<? extends Option<?>> children) {
		parent.addEventListener((option, event) -> children.forEach(c -> c.setAvailable(test.test(option.pendingValue()))));
		children.forEach(c -> c.setAvailable(test.test(parent.pendingValue())));
	}

	private static OptionGroup.Builder groupBuilder(String name, List<? extends Option<?>> options) {
		OptionGroup.Builder group = OptionGroup.createBuilder()
				.name(Component.translatable(KEY + "group." + name))
				.description(OptionDescription.of(Component.translatable(KEY + "group." + name + ".desc")));
		options.forEach(group::option);
		return group;
	}

	private static OptionGroup group(String name, List<? extends Option<?>> options) {
		return groupBuilder(name, options).build();
	}

	private static List<Option<?>> concat(Option<?> head, List<Option<?>> tail) {
		return java.util.stream.Stream.concat(java.util.stream.Stream.of(head), tail.stream()).toList();
	}

	private Option<Boolean> bool(String field) {
		return opt(field, TickBoxControllerBuilder::create);
	}

	private <E extends Enum<E>> Option<E> enumOpt(String field, Class<E> type) {
		return opt(field, o -> EnumControllerBuilder.create(o).enumClass(type));
	}

	private Option<Integer> intOpt(String field, int min, int max, int step, String format) {
		return opt(field, o -> IntegerSliderControllerBuilder.create(o).range(min, max).step(step).formatValue(v -> Component.literal(format.formatted(v))));
	}

	private Option<Double> bezier(String field, boolean y) {
		return opt(field, o -> DoubleSliderControllerBuilder.create(o).range(y ? -1.0 : 0.0, y ? 2.0 : 1.0).step(0.01));
	}

	private ControllerBuilder<Double> seconds(Option<Double> o) {
		return DoubleSliderControllerBuilder.create(o).range(0.0, 2.0).step(0.05)
				.formatValue(v -> v == 0 ? Component.translatable(KEY + "instant") : Component.literal("%.2fs".formatted(v)));
	}

	private static Object get(Field field, TlaConfig from) {
		try {
			return field.get(from);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	private static void set(Field field, TlaConfig to, Object value) {
		try {
			field.set(to, value);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}
}
