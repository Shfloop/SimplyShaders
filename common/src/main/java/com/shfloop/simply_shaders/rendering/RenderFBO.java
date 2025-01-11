package com.shfloop.simply_shaders.rendering;

import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.utils.IntArray;
import com.shfloop.simply_shaders.ShadowTexture;
import com.shfloop.simply_shaders.SimplyShaders;
import com.shfloop.simply_shaders.pack_loading.ShaderPackLoader;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;


public class RenderFBO {
    public static int[] lastDrawBuffers;
    public static final int[] allDrawBuffers = new int[] {GL32.GL_COLOR_ATTACHMENT0, GL32.GL_COLOR_ATTACHMENT1, GL32.GL_COLOR_ATTACHMENT2, GL32.GL_COLOR_ATTACHMENT3, GL32.GL_COLOR_ATTACHMENT4
            ,GL32.GL_COLOR_ATTACHMENT5, GL32.GL_COLOR_ATTACHMENT6, GL32.GL_COLOR_ATTACHMENT7};
    private final int fboHandle;
    private int WIDTH;
    private int HEIGHT;
    private static float previousViewportScale = 1.0f;

    //TODO still need to get renderTextures to bind in all shaders
    private static BufferTexture[] renderTextures;//for now ill only add 8 textures to render too
//    public static TextureRegion fboTexture = new TextureRegion(fbo.getColorBufferTexture());
    // i need a a second array to use as the uniform and hold the secondary texture if needed
    // i need to create the secondary texture on renderFBO create when shaderpack reloads/loaded
    // but only some of the shaders need to swap so for most of it the secondary array will be just pointers to renderTextures
    // have 3 arrays one to store the secondary texture
    //one that  just has pointers to the textures to be used in the uniform
    //and renderTextures which is used to render to
    //when a ping pong buffer is needed put replace the renderTextures one with the one from the secondary storage and replace the secondaryt storage place with the one rfom the uniform array
    //the uniform array can act as a tmp array for switching the too while also keeping the current texture to be used as a uniform

    public static BufferTexture[] uniformTextures; // this needs to be a copy of renderTextures on renderFBO create
    private static BufferTexture[] swapBufferStorage; // created on renderFbo start should create a identical shader to the cooresponding colorTex if any shader in the pack require ping ponging
    // this relies on renderFBO to be created on shaderpack load which im not sure if it is

    public ShadowTexture depthTex0;
    public RenderFBO(int width, int height, IntArray disabledBufferClearing) throws Exception {
        this(width,height);

    }
    public RenderFBO(int width, int height) throws Exception {

        this.WIDTH = width;
        this.HEIGHT = height;
        System.out.println("Creating RenderBuffer");
        int numRenderTextures = 8;
        renderTextures = new BufferTexture[numRenderTextures]; //these all need to be the same
        uniformTextures = new BufferTexture[numRenderTextures];
        swapBufferStorage = new BufferTexture[numRenderTextures];
        fboHandle = Gdx.gl.glGenFramebuffer();


        Gdx.gl.glBindFramebuffer(GL32.GL_FRAMEBUFFER, fboHandle);

        float[] textureScale = new float[8];
        for(int i =0; i< textureScale.length; i++) {
            String name = "colorTex" + i;
            if (ShaderPackLoader.packSettings != null) {
                textureScale[i] = ShaderPackLoader.packSettings.bufferTexturesScale.getOrDefault(name, 1.0f);
            } else {
                textureScale[i] = 1.0f;
            }

        }


        for(int i =0; i< renderTextures.length; i++) {
            renderTextures[i] = new BufferTexture("colorTex" + Integer.toString(i), WIDTH, HEIGHT, GL32.GL_RGBA, textureScale[i]); //each texture is only 13 MB for 8 bit
            uniformTextures[i] = renderTextures[i];
            Gdx.gl.glFramebufferTexture2D(GL32.GL_FRAMEBUFFER, GL32.GL_COLOR_ATTACHMENT0 + i, GL20.GL_TEXTURE_2D, renderTextures[i].getID(), 0);
        }

        for(int i =0; i< swapBufferStorage.length; i++) {
            swapBufferStorage[i] = new BufferTexture("colorTex" + Integer.toString(i), WIDTH, HEIGHT, GL32.GL_RGBA, textureScale[i]); //each texture is only 13 MB for 8 bit

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
        if (ShaderPackLoader.shaderPackOn) {
            for( int i =0; i < ShaderPackLoader.packSettings.disableBufferClearing.size;i++) {

                int bufferNum =ShaderPackLoader.packSettings.disableBufferClearing.get(i);
                SimplyShaders.LOGGER.info("NO CLEAR FOR BUFFER: {}",bufferNum);
                if(bufferNum>= 0 && bufferNum <8) {

                    renderTextures[bufferNum].clearTexture = false;
                    swapBufferStorage[bufferNum].clearTexture = false;

                }
            }
        }


        System.out.println("RenderBufferDone");
    }
    public int getFboHandle() {
        return fboHandle;
    }
    public void dispose() {

        Gdx.gl.glDeleteFramebuffer(fboHandle);
        uniformTextures = null; // should always be a copy of the renderTextures so just set to null and dispose of renderTexture objects
       for(BufferTexture tex: renderTextures) {
           tex.dispose();

       }
       for(BufferTexture tex: swapBufferStorage) { //Todo instead of creating a bunch of unused textures make renderFbo reload when the shaderpack loads so i can set swap to only have the needed textures
           //would need to null check if i only make needed textures
           tex.dispose();
       }
       swapBufferStorage = null;
       renderTextures = null;

    }
    //instead of binding the same uniforms that dont change each render

    //resposible each bind call a shader might need both textures so it needs to swap the drawable and what texture it receives for the uniform
    public void pingPongBuffer(int bufferNum) {
        //swap the texture num

        if (bufferNum < 0 || bufferNum >= renderTextures.length) {
            throw new RuntimeException("GameShader gave bad number for ping pong buffer");
        }
        BufferTexture temp = renderTextures[bufferNum];
        uniformTextures[bufferNum] = temp; //set the uniform bufferNum to the current renderTexture
        renderTextures[bufferNum] = swapBufferStorage[bufferNum]; //get the alternate buffer to render to
        swapBufferStorage[bufferNum] = temp;
        //  not sure if optifine copies the buffre first but im gonna assume that the shader is going to write over the entire buffer when ping ponging so there shouldnt be a need to
        //Currently this should always be called when renderFbo is bound

        //swap the old renderTexture witht the one from storage
        Gdx.gl.glFramebufferTexture2D(GL32.GL_FRAMEBUFFER, GL32.GL_COLOR_ATTACHMENT0 + bufferNum, GL20.GL_TEXTURE_2D, renderTextures[bufferNum].getID(), 0);



        if (Gdx.gl.glCheckFramebufferStatus(GL32.GL_FRAMEBUFFER) != GL32.GL_FRAMEBUFFER_COMPLETE ) {
            throw new RuntimeException("Could not create FrameBuffer during PingPong texture swap");
        }




        //todo maybe make renderFBO responsible for binding renderfbo so it knows when its bound and can check
        //in the meantime i have any gameshader without ping ponging set the array to null so this wont be called even with final shader
        //because i think if final does call this it could cause wierd things cause framebuffer 0 is bound
        //finalShader could call this but it doesnt have any drawbuffers so if they add that to final they deserve to get a crash
    }

    //need to reset the swapped buffer to the write buffer so the next program will work if it doesnt swap buffers as well
    public void undoUniformPingPong(int pingPongBufferNum) {
        uniformTextures[pingPongBufferNum] = renderTextures[pingPongBufferNum];
        //uniform should only have copies of stuff in render textures or swap bufer storage
    }
    public BufferTexture getTexture(int num) {
        return renderTextures[num];
    }
    public BufferTexture getSwapTexture(int num) {
        return swapBufferStorage[num];
    }
    //used for knowing which buffers to clear during begininng of render passes so i dont clear the entire swap buffer storage if its not necessary

    public void setCompositeViewPort(float viewportScale) {
        //needs to change viewPort based on the previous viewport resolution
        if (previousViewportScale == viewportScale) {
            return;
        }
        previousViewportScale = viewportScale;
        Gdx.gl.glViewport(0,0,(int)  (viewportScale * Gdx.graphics.getWidth()),(int) (viewportScale * Gdx.graphics.getHeight()));

    }
}
