package com.bouncingelf10.tla;

import com.bouncingelf10.tla.config.TlaConfig;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TabListAnimationClient implements ClientModInitializer {
	public static final String MOD_ID = "tab-list-animation";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		TlaConfig.HANDLER.load();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
