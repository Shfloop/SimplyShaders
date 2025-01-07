package com.shfloop.simply_shaders;


import finalforeach.cosmicreach.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Constants {

    public static final String MOD_ID = "simply_shaders_puzzle";
    public static final Identifier MOD_NAME = Identifier.of(MOD_ID, "SimplyShadersPuzzle");
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static final int NUM_FLOATS_PER_FACE_UVTEXBUFF = 12; //Puzzle Specific


}
