package com.shfloop.simply_shaders;


import com.shfloop.simply_shaders.rendering.RenderFBO;
import dev.crmodders.cosmicquilt.api.entrypoint.ModInitializer;
import finalforeach.cosmicreach.chat.commands.Command;
import com.shfloop.simply_shaders.commands.CommandTime;

import org.lwjgl.opengl.GL32;
import org.quiltmc.loader.api.ModContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimplyShaders implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("SimplyShaders Mod");
	public static RenderFBO buffer ; //this might be a good way to go about this but im not really sure

    public static int QuadVAO;
	@Override
	public void onInitialize(ModContainer mod) {
		LOGGER.info("Simply Shaders Initialized!");
		Command.registerCommand(CommandTime::new, "time");
        try {
            buffer = new RenderFBO();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        final float[] quadVertices = { // vertex attributes for a quad that fills the entire screen in Normalized Device Coordinates.
                // positions   // texCoords
                -1.0f,  1.0f,  0.0f, 1.0f,
                -1.0f, -1.0f,  0.0f, 0.0f,
                1.0f, -1.0f,  1.0f, 0.0f,

                -1.0f,  1.0f,  0.0f, 1.0f,
                1.0f, -1.0f,  1.0f, 0.0f,
                1.0f,  1.0f,  1.0f, 1.0f
        };
        FloatBuffer
         int quadVAO = GL32.glGenVertexArrays();
        int quadVBO = GL32.glGenBuffers();
       GL32.glBindVertexArray(quadVAO);
        GL32.glBindBuffer(GL32.GL_ARRAY_BUFFER, quadVBO);
        GL32.glBufferData(GL32.GL_ARRAY_BUFFER,quadVertices, GL32.GL_STATIC_DRAW);
        GL32.glEnableVertexAttribArray(0);
        GL32.glVertexAttribPointer(0, 2, GL32.GL_FLOAT, GL32.GL_FALSE);
        GL32.glEnableVertexAttribArray(1);
        GL32.glVertexAttribPointer(1, 2, GL32.GL_FLOAT, GL32.GL_FALSE, 4 * sizeof(float), (void*)(2 * sizeof(float)));
        GL32.glBindVertexArray(0);
    }
}

