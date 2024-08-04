package com.shfloop.simply_shaders;

import dev.crmodders.cosmicquilt.api.entrypoint.ModInitializer;
import org.quiltmc.loader.api.ModContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimplyShaders implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Example Mod");

	@Override
	public void onInitialize(ModContainer mod) {
		LOGGER.info("Simply Shaders Initialized!");


	}
}

