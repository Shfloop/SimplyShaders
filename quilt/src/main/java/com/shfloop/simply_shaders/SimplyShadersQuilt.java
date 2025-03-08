package com.shfloop.simply_shaders;

import dev.crmodders.cosmicquilt.api.entrypoint.ModInitializer;
import org.quiltmc.loader.api.ModContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimplyShadersQuilt implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Simply Shaders QUILT");

	@Override
	public void onInitialize(ModContainer mod) {
		LOGGER.info("Simply Shaders QUILT Initialized!");
		SimplyShaders.initializeBuffer();
	}
}

