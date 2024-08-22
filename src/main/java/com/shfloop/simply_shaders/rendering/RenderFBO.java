package com.shfloop.simply_shaders.rendering;

import com.badlogic.gdx.Gdx;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;


public class RenderFBO {
    private final int fboHandle;
    private int WIDTH = 800;
    private int HEIGHT = 600;
    private BufferTexture attachment0;
    private BufferTexture attachment1;
    public RenderFBO() throws Exception {
        fboHandle = Gdx.gl.glGenFramebuffer();
        attachment0 = new BufferTexture(WIDTH, HEIGHT, GL32.GL_RGBA);
        attachment1 = new BufferTexture(WIDTH,HEIGHT, GL32.GL_RGBA);
        Gdx.gl.glBindFramebuffer(GL32.GL_FRAMEBUFFER, fboHandle);
        Gdx.gl.glFramebufferTexture2D(GL32.GL_FRAMEBUFFER, GL32.GL_COLOR_ATTACHMENT0, GL20.GL_TEXTURE_2D, attachment0.getID(), 0);
        Gdx.gl.glFramebufferTexture2D(GL32.GL_FRAMEBUFFER, GL32.GL_COLOR_ATTACHMENT1, GL20.GL_TEXTURE_2D, attachment1.getID(), 0);
    //TODO probably need a depth texture as well
       int[] drawBuffers = {GL32.GL_COLOR_ATTACHMENT0,GL32.GL_COLOR_ATTACHMENT1};
       GL32.glDrawBuffers(drawBuffers);

        if (Gdx.gl.glCheckFramebufferStatus(GL32.GL_FRAMEBUFFER) != GL32.GL_FRAMEBUFFER_COMPLETE ) {
            throw new Exception("Could not create FrameBuffer");
        }


        Gdx.gl.glBindFramebuffer(36160, 0);
    }
    public int getFboHandle() {
        return fboHandle;
    }
    public void dispose() {

        Gdx.gl.glDeleteFramebuffer(fboHandle);
        this.attachment0.dispose();
        this.attachment1.dispose();
    }

}
//the framebuffer needs to be created and all textures need to be created 0-7
//
//then i need to go thorugh all the shaders (preferabbly after created but before its unbound) and bind optional the texture id to the shader so it can use it
//default the shader will outColor to the first attachment of the framebuffer
//so every shader can stay the same and than final just needs to take the first framebuffer attachment and show it to the actuall buffer
//
//
//each time ht escreen resizsd i need to delete the framebuffer and attachments and recreate it (textures mught not be the saem ) so i need to gothrough each shader and bind optional the new textuer id
//
//so for final fsh and vsh just needds to take whatever textures it wants to and
//
//i would probably want to see how many textures the program is actually using so im not allocating 70MB of Vram to the textures when only a couple might be used
