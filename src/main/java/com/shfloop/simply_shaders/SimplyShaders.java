package com.shfloop.simply_shaders;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.shfloop.simply_shaders.rendering.RenderFBO;
import dev.crmodders.cosmicquilt.api.entrypoint.ModInitializer;
import finalforeach.cosmicreach.chat.commands.Command;
import com.shfloop.simply_shaders.commands.CommandTime;

import org.lwjgl.opengl.GL32;
import org.lwjgl.system.MemoryUtil;
import org.quiltmc.loader.api.ModContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.FloatBuffer;

import static org.lwjgl.system.MemoryUtil.memFree;

public class SimplyShaders implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("SimplyShaders Mod");
	public static RenderFBO buffer ; //this might be a good way to go about this but im not really sure
    public static Mesh screenQuad;
    public static boolean inRender = false;

    public static FrameBuffer fbo;


	@Override
	public void onInitialize(ModContainer mod) {
		LOGGER.info("Simply Shaders Initialized!");
		Command.registerCommand(CommandTime::new, "time");
        System.out.println("IS IT RUNNING GL30" + Gdx.graphics.isGL30Available());
        //fbo = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
       // buildFBO(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        try {
            buffer = new RenderFBO(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        float[] verts = new float[20];
        int i = 0;

        verts[i++] = -1; // x1
        verts[i++] = -1; // y1
        verts[i++] = 0;
        verts[i++] = 0f; // u1
        verts[i++] = 0f; // v1

        verts[i++] = 1f; // x2
        verts[i++] = -1; // y2
        verts[i++] = 0;
        verts[i++] = 1f; // u2
        verts[i++] = 0f; // v2

        verts[i++] = 1f; // x3
        verts[i++] = 1f; // y3
        verts[i++] = 0;
        verts[i++] = 1f; // u3
        verts[i++] = 1f; // v3

        verts[i++] = -1; // x4
        verts[i++] = 1f; // y4
        verts[i++] = 0;
        verts[i++] = 0f; // u4
        verts[i++] = 1f; // v4

         screenQuad = new Mesh(true, 4,0,
                new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));
        screenQuad.setVertices(verts);


//        FloatBuffer verticesBuffer = MemoryUtil.memAllocFloat(quadVertices.length);
//        verticesBuffer.put(quadVertices).flip();
//
//        int quadVAO = GL32.glGenVertexArrays();
//        int quadVBO = GL32.glGenBuffers();
//        GL32.glBindVertexArray(quadVAO);
//        GL32.glBindBuffer(GL32.GL_ARRAY_BUFFER, quadVBO);
//        GL32.glBufferData(GL32.GL_ARRAY_BUFFER,verticesBuffer, GL32.GL_STATIC_DRAW);
//
//        memFree(verticesBuffer);
//
//        GL32.glVertexAttribPointer(0, 2, GL32.GL_FLOAT, GL32.GL_FALSE,0,0);
//        GL32.glBindBuffer(GL32.GL_ARRAY_BUFFER, 0);
//
//        GL32.glEnableVertexAttribArray(0);
//
//        GL32.glEnableVertexAttribArray(1);
//        GL32.glVertexAttribPointer(1, 2, GL32.GL_FLOAT, GL32.GL_FALSE, 0,0);
//
//        GL32.glBindVertexArray(0);
//        if (verticesBuffer != null) {
//            MemoryUtil.memFree(verticesBuffer);
//        }
    }

    //call this onInitialize and whenever the window resizes
    public void buildFBO(int width, int height) {
        if (fbo != null) fbo.dispose();

        GLFrameBuffer.FrameBufferBuilder framebb = new GLFrameBuffer.FrameBufferBuilder(width,height);
        framebb.addColorTextureAttachment(GL32.GL_RGBA8, GL32.GL_RGBA, GL32.GL_UNSIGNED_BYTE); //not sure which to use
        framebb.addColorTextureAttachment(GL32.GL_RGBA8, GL32.GL_RGBA, GL32.GL_UNSIGNED_BYTE);
        framebb.addDepthRenderBuffer(GL32.GL_DEPTH_COMPONENT24);

        fbo = framebb.build();

    }
    public static void resize(){
        if (buffer != null) {
            buffer.dispose();
            try {
                buffer = new RenderFBO(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }
    public static enum newShaderType {
        FRAG,
        VERT,
        IMPORTED
    }
}

