package com.shfloop.simply_shaders.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.shfloop.simply_shaders.ShadowTexture;
import com.shfloop.simply_shaders.SimplyShaders;
import com.shfloop.simply_shaders.pack_loading.ShaderPackLoader;
import finalforeach.cosmicreach.world.Sky;
import finalforeach.cosmicreach.world.Zone;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;

import java.util.Arrays;
import java.util.Comparator;

public class RenderTextureHolder {
    //needs to make all the appropraite sized render textuers and deal with swapping them between framebuffers


    public  BufferTexture[] uniformTextures; // this needs to be a copy of renderTextures on renderFBO create
    private  BufferTexture[] swapBufferStorage;
    private  BufferTexture[] renderTextures;
    public static int MAX_NUM_RENDER_TEXTURES = 8;
    public final int NUM_RENDER_TEXTUERS;
    private FrameBuffer[] framebuffers;

    private static final float[] temporaryColor = {0,0,0,0};
    private static final Color skyColor = new Color(0,0,0,0); //needs to update every clear cycle in clearTExtures
    private static final Color transparent = new Color(0,0,0,0);

    private static final float[] WHITE = {1.0f,1.0f,1.0f,1.0f};
    public static FrameBuffer boundFrameBuffer = null;
    public ShadowTexture depthTexture; //if the new framebuffer to be created has a width and height = to the window than it will add a depth buffer
    private final int[] attachmentMapping = {-1,-1,-1,-1,-1,-1,-1,-1}; //maximum number of attachments
    public RenderTextureHolder(BufferTexture[] unSortedTextures) { //should probably change this to somethign that holds the data for the texture
        //i need to make one of the framebuffers the main framebuffer that has a depth attachmetn
        if (MAX_NUM_RENDER_TEXTURES < unSortedTextures.length) {
            for (BufferTexture tex: unSortedTextures) { //should no longer be needed
                tex.dispose();
            }
            throw new RuntimeException(" RENDER TEXTUIRES EXCEEDS 8");
        }
        NUM_RENDER_TEXTUERS = unSortedTextures.length;
//        renderTextures = new BufferTexture[NUM_RENDER_TEXTUERS];
        uniformTextures = new BufferTexture[NUM_RENDER_TEXTUERS];
        // swapBufferStorage = new BufferTexture[NUM_RENDER_TEXTUERS];

        framebuffers = new FrameBuffer[NUM_RENDER_TEXTUERS * 2]; //just allocate enough space for each rendertexture to have a framebuffer and a copy framebuffer for each swap texture
        int framebufferSize = 0;


        //sort them so texture of the same height and width are next to each other
        Arrays.sort(unSortedTextures, Comparator.comparingInt(BufferTexture::getWidth).thenComparingInt(BufferTexture::getHeight).thenComparingInt(BufferTexture::getAttachmentNum));

        //doesnt matter what order they are in just that each texture of similar size is next to the same size
        //need to find how many framebuffers i need to make

        //create the swap textures as a deep copy of the
        BufferTexture[] unsortedSwapTexs = new BufferTexture[NUM_RENDER_TEXTUERS];
        for (int i = 0; i < NUM_RENDER_TEXTUERS; i++) {
            //the swap texture cant be set to mipMap linear if i never generate mip maps for it
            //I belive the textures can be swapped around though so that
            unsortedSwapTexs[i] = new BufferTexture(unSortedTextures[i].getName(), unSortedTextures[i].getWidth(), unSortedTextures[i].getHeight(), unSortedTextures[i].getPixelFormat(), unSortedTextures[i].getInternalFormat(), unSortedTextures[i].getAttachmentNum(),unSortedTextures[i].isMipMapEnabled);
        }



        int j =0;
        //int testIdx = 0;


        for (int i = 0; i < NUM_RENDER_TEXTUERS;i ++) {

            int testIdx = i;
            if (testIdx + 1< NUM_RENDER_TEXTUERS) {
                if (unSortedTextures[i].getWidth() == unSortedTextures[i + 1].getWidth() && unSortedTextures[i].getHeight() == unSortedTextures[i + 1].getHeight()) {
                    //if both textures have the same dimensions they should go in the same FB

                    continue;
                }

            }

            //create the framebuffer


            // repeat until the end of the textuers

//            framebuffers[framebufferSize] = new FrameBuffer();
//            framebufferSize++;unsortedSwapTexs[j].setFrameBufferIdx(framebufferSize - 1); //set teh swap texture framebuffer idx

            int startIdx = j;
            //i is the index of the last texture that is in the framebuffer
            while (j <= i) {
                try {
                    unSortedTextures[j ].genTexture();
                    unsortedSwapTexs[j ].genTexture();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                unSortedTextures[j].setFrameBufferIdx(framebufferSize ); //set teh render texture framebuffer idx
                unsortedSwapTexs[j].setFrameBufferIdx(framebufferSize + 1);



                j++;
            }

            try {

                 //cannot forget to reset this between framebuffer creation or any fb > window size will also get depth
                //can be i cause its inclusive in this case
                //there should only be a single framebuffer with the size of Gdx.graphics.getWidth unless im stupid and my code completly doesnt work
                if (unSortedTextures[i].getWidth() == Gdx.graphics.getWidth() && unSortedTextures[i].getHeight() == Gdx.graphics.getHeight()) {
                    //create the framebuffer with a depth atachment
                    if (depthTexture != null) {
                        throw new RuntimeException("DEPTH TEXTURE IS NOT NULL AND TRYING TO OVERWRITE");
                    }
                    depthTexture = new ShadowTexture(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), GL20.GL_DEPTH_COMPONENT);
                    framebuffers[framebufferSize] = new FrameBuffer(unSortedTextures,startIdx, i, depthTexture);
                    framebufferSize++;
                }else {
                    framebuffers[framebufferSize] = new FrameBuffer(unSortedTextures,startIdx, i, null);
                    framebufferSize++;
                }

                framebuffers[framebufferSize] = new FrameBuffer(unsortedSwapTexs,startIdx, i, null);//swap buffer doesnt need a depthAttachment should onyl be used in composite passes
                framebufferSize++;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }



        }
        Arrays.sort(unSortedTextures, Comparator.comparingInt(BufferTexture::getAttachmentNum));
        Arrays.sort(unsortedSwapTexs, Comparator.comparingInt(BufferTexture::getAttachmentNum));
        this.renderTextures = unSortedTextures;
        this.swapBufferStorage = unsortedSwapTexs;
        //generate attachment mappings
        for (int i = 0; i < this.NUM_RENDER_TEXTUERS; i++) {
            this.attachmentMapping[this.renderTextures[i].getAttachmentNum() - GL32.GL_COLOR_ATTACHMENT0] = i;
        }
        System.arraycopy(this.renderTextures, 0, this.uniformTextures, 0, this.renderTextures.length);

        //add clear Color

        if (ShaderPackLoader.shaderPackOn) {
            for (int i =0; i < ShaderPackLoader.packSettings.disableBufferClearing.size; i++) {
                int bufferNum = ShaderPackLoader.packSettings.disableBufferClearing.get(i);
                if (bufferNum >= 0 && bufferNum < renderTextures.length) {
                    renderTextures[bufferNum].clearTexture = false;
                    swapBufferStorage[bufferNum].clearTexture = false;
                }
            }
        }

        //ClearColor
        //tmp until i support pack specific colors
        setTextureClearColor(skyColor, 0);

        setTextureClearColor(Color.WHITE, 1); //if it doesnt exist setTextureClearColor will just return
            for (int i = 2; i < NUM_RENDER_TEXTUERS; i++) {

                setTextureClearColor(transparent, i);
            }





    }
    private void setTextureClearColor(Color color , int texNum) {
        if (texNum >= 0 && texNum < NUM_RENDER_TEXTUERS) {
            renderTextures[texNum].clearColor = color;
            uniformTextures[texNum].clearColor = color;
        }
    }
    public void dispose() {
        if (this.framebuffers != null) {
            for (FrameBuffer buf: this.framebuffers) {
                //buf can be null
                if (buf != null) {
                    buf.dispose();
                }

            }
        }
        if (depthTexture != null) {
            depthTexture.cleanup();
            depthTexture = null;
        }

        uniformTextures = null; // should always be a copy of the renderTextures so just set to null and dispose of renderTexture objects
        if (renderTextures != null) {
            for(BufferTexture tex: renderTextures) {
                tex.dispose();

            }
        }
        if (swapBufferStorage != null) {
            for(BufferTexture tex: swapBufferStorage) { //Todo instead of creating a bunch of unused textures make renderFbo reload when the shaderpack loads so i can set swap to only have the needed textures
                //would need to null check if i only make needed textures
                tex.dispose();
            }
        }

        swapBufferStorage = null;
        renderTextures = null;

    }
    private int bindClearFramebuffer(int lastBufIdx, int texNum) {
        int bufIdx = this.renderTextures[texNum].getFrameBufferIdx();
        if (bufIdx != lastBufIdx) {
            Gdx.gl.glBindFramebuffer(GL32.GL_FRAMEBUFFER, this.framebuffers[bufIdx].getFboHandle());
            return bufIdx;
        }
        return lastBufIdx;
    }
    public void clearTextures(Zone playerZone) {//FIXME naive approach but just to get things working

        int lastBufIdx = -1;
//        FrameBuffer buf = this.framebuffers[this.renderTextures[0].getFrameBufferIdx()];
//        Gdx.gl.glBindFramebuffer(GL32.GL_FRAMEBUFFER, buf.getFboHandle());
       lastBufIdx= this.bindClearFramebuffer(lastBufIdx,0); // texNum 0 might not be the colorTex-0 if a pack decides to change all the shaders for normal game passes

        Sky sky = Sky.getCurrentSky(playerZone);

        skyColor.set(sky.currentSkyColor);


        GL32.glClearBufferfv(GL32.GL_DEPTH, 0, WHITE); //clear the depth tex
        for (int i = 0; i < this.NUM_RENDER_TEXTUERS; i++) {
            if (!this.renderTextures[i].clearTexture) {
                continue;
            }
            Color c = this.renderTextures[i].clearColor;
            temporaryColor[0] = c.r;
            temporaryColor[1] = c.g;
            temporaryColor[2] = c.b;
            temporaryColor[3] = c.a;
            lastBufIdx =this.bindClearFramebuffer(lastBufIdx, i);
            GL32.glDrawBuffers(this.renderTextures[i].getAttachmentNum());
            //this.framebuffers[bufIdx].lastDrawBuffers = this.framebuffers[bufIdx].allDrawBuffers;
            GL32.glClearColor(c.r,c.g,c.b,c.a);
            GL32.glClear(GL32.GL_COLOR_BUFFER_BIT);
            //GL32.glClearBufferfv(GL32.GL_COLOR, i, temporaryColor); //this clears the index of the number of attachments not the attachment number specifically so 0 would clear the first attachment which could be atachment6

        }



    }

    public FrameBuffer[] getFramebuffers() {
        return framebuffers;
    }
    public void bindFrameBuffer(int frameBufferIdx) {
        boundFrameBuffer = this.framebuffers[frameBufferIdx];
        Gdx.gl.glBindFramebuffer(GL32.GL_FRAMEBUFFER, boundFrameBuffer.getFboHandle());

    }
    public void bindFrameBuffer(FrameBuffer fb) { //dont do this if the framebuffer isnt managed only be used with framebufferrs in rendertextureholder
        boundFrameBuffer = fb;
        Gdx.gl.glBindFramebuffer(GL32.GL_FRAMEBUFFER, boundFrameBuffer.getFboHandle());
    }
    public void unBindFrameBuffer() {
        boundFrameBuffer = null;
        Gdx.gl.glBindFramebuffer(GL32.GL_FRAMEBUFFER, 0);
    }
    //bufferNUm doesnt coesnt match up with renderTextures becasue rendertextures only has the length of buffers in use but this calles the attachment number
    //
    public void pingPongBuffer(int attachmentNum) { // can be called multiple times per pass for each buffer that needs to pong



        int bufferNum = this.attachmentMapping[attachmentNum];


        if (bufferNum < 0 || bufferNum >= renderTextures.length) {
            SimplyShaders.LOGGER.info("MAPPINGS {} \nrenderTextures {}",this.attachmentMapping, this.renderTextures);
            throw new RuntimeException("GameShader gave bad number for ping pong buffer");
        }
        BufferTexture temp = renderTextures[bufferNum];
        uniformTextures[bufferNum] = temp; //set the uniform bufferNum to the current renderTexture
        renderTextures[bufferNum] = swapBufferStorage[bufferNum]; //get the alternate buffer to render to
        swapBufferStorage[bufferNum] = temp;
        FrameBuffer newBuf = this.framebuffers[renderTextures[bufferNum].getFrameBufferIdx()];
        if(boundFrameBuffer != newBuf) {
            bindFrameBuffer(newBuf);
        }

    }
    public void undoUniformPingPong(int attachmentNum) { //dont need to check heare cause this will be called after pingpong anyway
        int bufferNum = this.attachmentMapping[attachmentNum];
        uniformTextures[bufferNum] = renderTextures[bufferNum];
        //uniform should only have copies of stuff in render textures or swap bufer storage
    }

    public void setCompositeViewPort(float viewportScale) {
        //needs to change viewPort based on the previous viewport resolution


        Gdx.gl.glViewport(0,0,(int)  (viewportScale * Gdx.graphics.getWidth()),(int) (viewportScale * Gdx.graphics.getHeight()));


    }

    public BufferTexture getRenderTexture(int i) {
        return this.renderTextures[i];
    }

    public BufferTexture[] getRenderTextures() {
        return renderTextures;
    }
    public BufferTexture[] getSwapTextures() {
        return swapBufferStorage;
    }
}
