package com.bouncingelf10.tla.compat;

import com.bouncingelf10.tla.config.TlaConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuCompat implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return TlaConfigScreen::create;
	}
}
