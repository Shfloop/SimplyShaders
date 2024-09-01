package com.shfloop.simply_shaders;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.shfloop.simply_shaders.rendering.RenderFBO;
import dev.crmodders.cosmicquilt.api.entrypoint.ModInitializer;

import org.lwjgl.opengl.GL32;
import org.quiltmc.loader.api.ModContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

