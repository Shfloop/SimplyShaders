package com.shfloop.simply_shaders;





import dev.puzzleshq.puzzleloader.loader.mod.entrypoint.client.ClientModInit;

import static com.shfloop.simply_shaders.SimplyShaders.LOGGER;


public class SimplyShadersPuzzle implements ClientModInit {
    @Override
    public void onClientInit() {
        LOGGER.info("Block Switcher Puzzle Initialized!");

        SimplyShaders.initializeBuffer(); //i dont think this is running
    }
}
