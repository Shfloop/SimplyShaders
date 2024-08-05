package com.shfloop.simply_shaders;

import dev.crmodders.cosmicquilt.api.entrypoint.ModInitializer;
import finalforeach.cosmicreach.world.Sky;
import org.quiltmc.loader.api.ModContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimplyShaders implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Example Mod");

	@Override
	public void onInitialize(ModContainer mod) {
		LOGGER.info("Simply Shaders Initialized!");

		//Sky.skyChoices.set(2, new DynamicSkyRewrite("Dynamic_Sky"));


	}
}

