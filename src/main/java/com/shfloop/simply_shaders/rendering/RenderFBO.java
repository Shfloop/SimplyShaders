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
    public static int[] allDrawBuffers;
    private final int fboHandle;
    private int WIDTH;
    private int HEIGHT;
    private static BufferTexture[] renderTextures = new BufferTexture[8];//for now ill only add 8 textures to render too
//    public static TextureRegion fboTexture = new TextureRegion(fbo.getColorBufferTexture());
    public BufferTexture attachment0;
    public BufferTexture attachment1;
    public BufferTexture attachment2;
    public BufferTexture attachment3;
    public BufferTexture attachment4;
    public ShadowTexture depthTex0;
    public RenderFBO(int width, int height) throws Exception {

        this.WIDTH = width;
        this.HEIGHT = height;
        System.out.println("Creating RenderBuffer");
        fboHandle = Gdx.gl.glGenFramebuffer();
        attachment0 = new BufferTexture("colorTex0",WIDTH, HEIGHT, GL32.GL_RGBA);
        //FIXME temporary
        renderTextures[0] = attachment0;

        attachment1 = new BufferTexture("colorTex1",WIDTH,HEIGHT, GL32.GL_RGBA);
        renderTextures[1] = attachment1;
        attachment2 = new BufferTexture("colorTex2",WIDTH,HEIGHT, GL32.GL_RGBA);
        renderTextures[2] = attachment2;
        attachment3 = new BufferTexture("colorTex3", WIDTH,HEIGHT, GL32.GL_RGBA);
        renderTextures[3] = attachment3;
        attachment4 = new BufferTexture("colorTex4", WIDTH,HEIGHT, GL32.GL_RGBA);
        renderTextures[3] = attachment4;
        Gdx.gl.glBindFramebuffer(GL32.GL_FRAMEBUFFER, fboHandle);
        Gdx.gl.glFramebufferTexture2D(GL32.GL_FRAMEBUFFER, GL32.GL_COLOR_ATTACHMENT0, GL20.GL_TEXTURE_2D, attachment0.getID(), 0);
        Gdx.gl.glFramebufferTexture2D(GL32.GL_FRAMEBUFFER, GL32.GL_COLOR_ATTACHMENT1, GL20.GL_TEXTURE_2D, attachment1.getID(), 0);
        Gdx.gl.glFramebufferTexture2D(GL32.GL_FRAMEBUFFER, GL32.GL_COLOR_ATTACHMENT2, GL20.GL_TEXTURE_2D, attachment2.getID(), 0);
        Gdx.gl.glFramebufferTexture2D(GL32.GL_FRAMEBUFFER, GL32.GL_COLOR_ATTACHMENT3, GL20.GL_TEXTURE_2D, attachment3.getID(), 0);
        Gdx.gl.glFramebufferTexture2D(GL32.GL_FRAMEBUFFER, GL32.GL_COLOR_ATTACHMENT4, GL20.GL_TEXTURE_2D, attachment4.getID(), 0);
    //TODO probably need a depth texture as well



        depthTex0 = new ShadowTexture(WIDTH,HEIGHT, GL20.GL_DEPTH_COMPONENT);
        Gdx.gl.glFramebufferTexture2D(GL32.GL_FRAMEBUFFER, GL32.GL_DEPTH_ATTACHMENT, GL20.GL_TEXTURE_2D, depthTex0.id, 0);
        lastDrawBuffers = new int[]{GL32.GL_COLOR_ATTACHMENT0, GL32.GL_COLOR_ATTACHMENT1, GL32.GL_COLOR_ATTACHMENT2, GL32.GL_COLOR_ATTACHMENT3, GL32.GL_COLOR_ATTACHMENT4};
//       GL32.glDrawBuffers(drawBuffers);
        allDrawBuffers = new int[]{GL32.GL_COLOR_ATTACHMENT0, GL32.GL_COLOR_ATTACHMENT1, GL32.GL_COLOR_ATTACHMENT2, GL32.GL_COLOR_ATTACHMENT3, GL32.GL_COLOR_ATTACHMENT4};

        //get rid of renderbuffer and instead use a depth texture so i can sample it after in final
//       int renderBuffer = Gdx.gl.glGenRenderbuffer();
//       Gdx.gl.glBindRenderbuffer(GL32.GL_RENDERBUFFER, renderBuffer);
//       Gdx.gl.glRenderbufferStorage(GL32.GL_RENDERBUFFER,GL32.GL_DEPTH24_STENCIL8, WIDTH,HEIGHT);
//       Gdx.gl.glBindRenderbuffer(GL32.GL_RENDERBUFFER, 0); //unbind renderbuffer
//       Gdx.gl.glFramebufferRenderbuffer(GL32.GL_FRAMEBUFFER, GL32.GL_DEPTH_STENCIL_ATTACHMENT, GL32.GL_RENDERBUFFER, renderBuffer);

        //GL32.glDrawBuffer(GL32.GL_COLOR_ATTACHMENT0);

        GL32.glDrawBuffers(lastDrawBuffers);
        //GL20.glReadBuffer(GL20.GL_NONE);
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
        this.attachment2.dispose();
        this.attachment3.dispose();
        this.attachment4.dispose();
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




it sounds like each textures is its own framebuffer?

 */