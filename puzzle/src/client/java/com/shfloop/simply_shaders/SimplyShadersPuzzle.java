package com.shfloop.simply_shaders;


import com.shfloop.simply_shaders.SimplyShaders;
import dev.puzzleshq.puzzleloader.cosmic.core.modInitialises.ClientModInit;

import static com.shfloop.simply_shaders.SimplyShaders.LOGGER;


public class SimplyShadersPuzzle implements ClientModInit {
    @Override
    public void onClientInit() {
        LOGGER.info("Block Switcher Puzzle Initialized!");

    }
}
