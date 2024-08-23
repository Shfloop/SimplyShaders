package com.shfloop.simply_shaders.rendering;

import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.shfloop.simply_shaders.Shadows;
import com.shfloop.simply_shaders.mixins.GameShaderInterface;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;

import java.nio.Buffer;


public class RenderFBO {
    private final int fboHandle;
    private int WIDTH = 800;
    private int HEIGHT = 600;
    private static BufferTexture[] renderTextures = new BufferTexture[2];
//    public static TextureRegion fboTexture = new TextureRegion(fbo.getColorBufferTexture());
    public BufferTexture attachment0;
    private BufferTexture attachment1;
    public RenderFBO() throws Exception {
        System.out.println("Creating RenderBuffer");
        fboHandle = Gdx.gl.glGenFramebuffer();
        attachment0 = new BufferTexture("colorTex0",WIDTH, HEIGHT, GL32.GL_RGBA);
        //FIXME temporary
        renderTextures[0] = attachment0;

        attachment1 = new BufferTexture("colorTex1",WIDTH,HEIGHT, GL32.GL_RGBA);
        renderTextures[1] = attachment1;
        Gdx.gl.glBindFramebuffer(GL32.GL_FRAMEBUFFER, fboHandle);
        Gdx.gl.glFramebufferTexture2D(GL32.GL_FRAMEBUFFER, GL32.GL_COLOR_ATTACHMENT0, GL20.GL_TEXTURE_2D, attachment0.getID(), 0);
        Gdx.gl.glFramebufferTexture2D(GL32.GL_FRAMEBUFFER, GL32.GL_COLOR_ATTACHMENT1, GL20.GL_TEXTURE_2D, attachment1.getID(), 0);
    //TODO probably need a depth texture as well

       int[] drawBuffers = {GL32.GL_COLOR_ATTACHMENT0,GL32.GL_COLOR_ATTACHMENT1};
       GL32.glDrawBuffers(drawBuffers);

       int renderBuffer = Gdx.gl.glGenRenderbuffer();
       Gdx.gl.glBindRenderbuffer(GL32.GL_RENDERBUFFER, renderBuffer);
       Gdx.gl.glRenderbufferStorage(GL32.GL_RENDERBUFFER,GL32.GL_DEPTH24_STENCIL8, WIDTH,HEIGHT);
       Gdx.gl.glBindRenderbuffer(GL32.GL_RENDERBUFFER, 0); //unbind renderbuffer
       Gdx.gl.glFramebufferRenderbuffer(GL32.GL_FRAMEBUFFER, GL32.GL_DEPTH_STENCIL_ATTACHMENT, GL32.GL_RENDERBUFFER, renderBuffer);


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
        //TODO change this to renderTextures
        //make renderTextures Null after
        this.attachment0.dispose();
        this.attachment1.dispose();
    }
    //instead of binding the same uniforms that dont change each render
    //bind them only once when framebuffer is created (also after shaders are created/reloaded)
    //should happen on window resize / shaderReload(startup)
    public static void bindRenderTextures() {

        //only needs to happen when renderFBO is created (when reload is called/resize )
        System.out.println("Binding render textures");
        Array<GameShader>  allShaders = GameShaderInterface.getShader();
        //TODO get rid of vanilla shaders cause they arent used
        PerspectiveCamera temp = new PerspectiveCamera();
        for (GameShader shader: allShaders) {
            //Todo find ouy why i cant use a orthographic camera
            shader.bind( temp);//need some type of camera
            //should also add shadowTextures to this
            //Todo shadow map bind needs to be created first
            //shader.bindOptionalInt("shadowMap", Shadows.shadow_map.getDepthMapTexture().id);
            //bind all render textures
            for (BufferTexture tex: renderTextures) {
                //get name and id
                shader.bindOptionalInt(tex.getName(), tex.getID());
            }
            shader.unbind();

        }
        System.out.println("Finished binding render textures");
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

/*
i can create the renderbuffer whenever the shaders are done being created (InGame;Create())
//i dont want to recreate the shaders everytime the the screen is resized
//the framebuffer doesnt need to be recreated when shaders reload
//after shaders init. anytime scrveern resized, but the static bindRenderTextures can be called after every shader reload
so i  think i can create framebuffer when ingame creates and dispose when ingame disposed and than rereate it each time screen resizes

 */