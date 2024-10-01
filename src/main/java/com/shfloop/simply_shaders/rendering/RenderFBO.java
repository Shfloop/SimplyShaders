package com.shfloop.simply_shaders.rendering;

import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.shfloop.simply_shaders.ShadowTexture;
import com.shfloop.simply_shaders.Shadows;
import com.shfloop.simply_shaders.mixins.GameShaderInterface;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;

import java.nio.Buffer;


public class RenderFBO {
    public static int[] lastDrawBuffers;
    public static final int[] allDrawBuffers = new int[] {GL32.GL_COLOR_ATTACHMENT0, GL32.GL_COLOR_ATTACHMENT1, GL32.GL_COLOR_ATTACHMENT2, GL32.GL_COLOR_ATTACHMENT3, GL32.GL_COLOR_ATTACHMENT4
            ,GL32.GL_COLOR_ATTACHMENT5, GL32.GL_COLOR_ATTACHMENT6, GL32.GL_COLOR_ATTACHMENT7};
    private final int fboHandle;
    private int WIDTH;
    private int HEIGHT;

    //TODO still need to get renderTextures to bind in all shaders
    public static BufferTexture[] renderTextures;//for now ill only add 8 textures to render too
//    public static TextureRegion fboTexture = new TextureRegion(fbo.getColorBufferTexture());

    public ShadowTexture depthTex0;
    public RenderFBO(int width, int height) throws Exception {

        this.WIDTH = width;
        this.HEIGHT = height;
        System.out.println("Creating RenderBuffer");
        renderTextures = new BufferTexture[8];
        fboHandle = Gdx.gl.glGenFramebuffer();

        Gdx.gl.glBindFramebuffer(GL32.GL_FRAMEBUFFER, fboHandle);


        for(int i =0; i< renderTextures.length; i++) {
            renderTextures[i] = new BufferTexture("colorTex" + Integer.toString(i), WIDTH, HEIGHT, GL32.GL_RGBA); //each texture is only 13 MB for 8 bit
            Gdx.gl.glFramebufferTexture2D(GL32.GL_FRAMEBUFFER, GL32.GL_COLOR_ATTACHMENT0 + i, GL20.GL_TEXTURE_2D, renderTextures[i].getID(), 0);
        }





        depthTex0 = new ShadowTexture(WIDTH,HEIGHT, GL20.GL_DEPTH_COMPONENT);
        Gdx.gl.glFramebufferTexture2D(GL32.GL_FRAMEBUFFER, GL32.GL_DEPTH_ATTACHMENT, GL20.GL_TEXTURE_2D, depthTex0.id, 0);



        lastDrawBuffers = allDrawBuffers;
        GL32.glDrawBuffers(lastDrawBuffers);
        //frameuffer fails if the game is minimized
        if (Gdx.gl.glCheckFramebufferStatus(GL32.GL_FRAMEBUFFER) != GL32.GL_FRAMEBUFFER_COMPLETE ) {
            throw new Exception("Could not create FrameBuffer");
        }

//        fboTexture = new TextureRegion(fbo.getColorBufferTexture());
        Gdx.gl.glBindFramebuffer(36160, 0);
        System.out.println("RenderBufferDone");
    }
    public int getFboHandle() {
        return fboHandle;
    }
    public void dispose() {

        Gdx.gl.glDeleteFramebuffer(fboHandle);
       for(BufferTexture tex: renderTextures) {
           tex.dispose();

       }
       renderTextures = null;
    }
    //instead of binding the same uniforms that dont change each render





}
