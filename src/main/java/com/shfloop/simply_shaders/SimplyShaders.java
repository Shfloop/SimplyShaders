package com.shfloop.simply_shaders;

import dev.crmodders.cosmicquilt.api.entrypoint.ModInitializer;
import finalforeach.cosmicreach.chat.commands.Command;
import com.shfloop.simply_shaders.commands.CommandTime;
import finalforeach.cosmicreach.world.Sky;
import org.quiltmc.loader.api.ModContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimplyShaders implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("SimplyShaders Mod");

	@Override
	public void onInitialize(ModContainer mod) {
		LOGGER.info("Simply Shaders Initialized!");
		Command.registerCommand(CommandTime::new, "time");
	}
}

