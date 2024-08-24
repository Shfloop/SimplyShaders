package com.shfloop.simply_shaders.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer;
import org.lwjgl.opengl.GL32;
//import com.badlogic.gdx.graphics.glutils.GLFrameBuffer.FrameBufferBuilder;
//com.badlogic.gdx.graphics.glutils.GLFrameBuffer.;


public class MRTFBO {
    private int WIDTH;
    private int HEIGHT;

    public MRTFBO() {


        this.WIDTH = Gdx.graphics.getWidth();
        this.HEIGHT = Gdx.graphics.getHeight();

        GLFrameBuffer.FrameBufferBuilder framebb = new GLFrameBuffer.FrameBufferBuilder(WIDTH,HEIGHT);
        framebb.addColorTextureAttachment(GL32.GL_RGBA8, GL32.GL_RGBA, GL32.GL_UNSIGNED_BYTE); //not sure which to use
        framebb.addDepthRenderBuffer(GL32.GL_DEPTH_COMPONENT24);





    }
}
