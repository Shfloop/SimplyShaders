package com.shfloop.simply_shaders;

import com.github.puzzle.core.loader.launch.provider.mod.entrypoint.impls.ClientModInitializer;
import com.shfloop.simply_shaders.SimplyShaders;

import static com.shfloop.simply_shaders.SimplyShaders.LOGGER;


public class SimplyShadersPuzzle implements ClientModInitializer {
    @Override
    public void onInit() {
        LOGGER.info("Block Switcher Puzzle Initialized!");

    }
}
