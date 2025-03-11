package com.shfloop.simply_shaders.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.shfloop.simply_shaders.GameShaderInterface;
import com.shfloop.simply_shaders.ShadowTexture;
import com.shfloop.simply_shaders.SimplyShaders;
import com.shfloop.simply_shaders.pack_loading.ShaderPackLoader;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.world.Sky;
import finalforeach.cosmicreach.world.Zone;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

public class RenderTextureHolder {
    //needs to make all the appropraite sized render textuers and deal with swapping them between framebuffers


    private HashMap<String, Integer> mainUniformTexIds = new HashMap<>();
    private HashMap<String, Integer> altUniformTexIds = new HashMap<>();
    private  BufferTexture[] alternateTextures;
    public BufferTexture[] mainTextures;

    public static int MAX_NUM_RENDER_TEXTURES = 8;
    public final int NUM_RENDER_TEXTUERS;
    private Array<FrameBuffer> framebuffers;

    private static final float[] temporaryColor = {0,0,0,0};
    private static final Color skyColor = new Color(0,0,0,0); //needs to update every clear cycle in clearTExtures
    private static final Color transparent = new Color(0,0,0,0);

    private static final float[] WHITE = {1.0f,1.0f,1.0f,1.0f};
    public static FrameBuffer boundFrameBuffer = null;
    public ShadowTexture depthTexture; //if the new framebuffer to be created has a width and height = to the window than it will add a depth buffer
    private final int[] attachmentMapping = {-1,-1,-1,-1,-1,-1,-1,-1}; //maximum number of attachments
    public final boolean[] flippedBuffers = new boolean[8]; //this keeps track of each texture in the main renderFbo used during terrain passes
    public FrameBuffer baseGameFrameBuffer;
    private Array<FrameBuffer> clearFrameBuffers;

    //only set the buffer to true if it the buffer does do clearing
    //For now ill have to rely on the shaderpack not reding/writing to teporal texes in non fullscreen passes, because the tex could end up in alternate tex and not be able to be rendered to



    //at the end of render a buffer currently in framebuffer alternate, would end up in renderTextures this is not good because it would get cleared but uniforms would be set to this instead of the texture activly getting rendered to in main buffer during terrain stages
    // i only want to set this if the buffer is part of the terrain shaders because if somebody wanted to temporaly use the textre
    //if at the end the texture with the data is the one in swap buffer storage there is no way to render to that during terrain passes it would still work for composite renders if i didnt reset it
    //i only need to reset flipped buffers if its a rendertarget during non fullscreen passes

    //solution to this is to have a secondary a and b fbo along witht he main render texture fbo
    //how would this solve theissue
    //read from a write to b
    //read from b write to a
    //so instead of calling drawbuffers iris just has a fbo for each shader stage whitht the coresponding texture bound to it




    //I THINK I GET IT
    //EVERY singly shader stage has a seperate FBO with teh corresponding texture for that stage even base game stages
    //but every one has the same depth texture attached so they can all be bound seperatly and render to the same depth texture
    //but how if one texture is the alternate and oneis the main how could you render to both so if you ping pong once
    //and its a temporal texture b has the correct data to keep but you start by rendering to a it just couldnt work in a terrain shader maybe
    //
    //
    //texture can be an attachment to multiple FBOs'

    //curent issue is if one buffer ping pongs than what happens to the other buffers that didnt ping pong
    //iris seems to make an a and b framebuffer for each fullscreen pass the framebuffer has the respective a and b textures bound as attachments
    //the uniforms are decided by whatever was rendered last
    //
//    public RenderTextureHolder(BufferTexture[] unSortedTextures) { //should probably change this to somethign that holds the data for the texture
//        //i need to make one of the framebuffers the main framebuffer that has a depth attachmetn
//        if (MAX_NUM_RENDER_TEXTURES < unSortedTextures.length) {
//            for (BufferTexture tex: unSortedTextures) { //should no longer be needed
//                tex.dispose();
//            }
//            throw new RuntimeException(" RENDER TEXTUIRES EXCEEDS 8");
//        }
//        NUM_RENDER_TEXTUERS = unSortedTextures.length;
////        renderTextures = new BufferTexture[NUM_RENDER_TEXTUERS];
//        alternateTextures = new BufferTexture[NUM_RENDER_TEXTUERS];
//        // swapBufferStorage = new BufferTexture[NUM_RENDER_TEXTUERS];
//
//
//
//
//        //sort them so texture of the same height and width are next to each other
//        Arrays.sort(unSortedTextures, Comparator.comparingInt(BufferTexture::getWidth).thenComparingInt(BufferTexture::getHeight).thenComparingInt(BufferTexture::getAttachmentNum));
//
//        //doesnt matter what order they are in just that each texture of similar size is next to the same size
//        //need to find how many framebuffers i need to make
//
//        //create the swap textures as a deep copy of the
//        BufferTexture[] unsortedSwapTexs = new BufferTexture[NUM_RENDER_TEXTUERS];
//        for (int i = 0; i < NUM_RENDER_TEXTUERS; i++) {
//            //the swap texture cant be set to mipMap linear if i never generate mip maps for it
//            //I belive the textures can be swapped around though so that
//            unsortedSwapTexs[i] = new BufferTexture(unSortedTextures[i].getName(), unSortedTextures[i].getWidth(), unSortedTextures[i].getHeight(), unSortedTextures[i].getPixelFormat(), unSortedTextures[i].getInternalFormat(), unSortedTextures[i].getAttachmentNum(),unSortedTextures[i].isMipMapEnabled);
//        }
//
//
//
//        int j =0;
//        //int testIdx = 0;
//
//
//        for (int i = 0; i < NUM_RENDER_TEXTUERS;i ++) {
//
//            int testIdx = i;
//            if (testIdx + 1< NUM_RENDER_TEXTUERS) {
//                if (unSortedTextures[i].getWidth() == unSortedTextures[i + 1].getWidth() && unSortedTextures[i].getHeight() == unSortedTextures[i + 1].getHeight()) {
//                    //if both textures have the same dimensions they should go in the same FB
//
//                    continue;
//                }
//
//            }
//
//            //create the framebuffer
//
//
//            // repeat until the end of the textuers
//
////            framebuffers[framebufferSize] = new FrameBuffer();
////            framebufferSize++;unsortedSwapTexs[j].setFrameBufferIdx(framebufferSize - 1); //set teh swap texture framebuffer idx
//
//            int startIdx = j;
//            //i is the index of the last texture that is in the framebuffer
//            while (j <= i) {
//                try {
//                    unSortedTextures[j ].genTexture();
//                    unsortedSwapTexs[j ].genTexture();
//                    mainUniformTexIds.put(unSortedTextures[j].getName(), unSortedTextures[j].getID());
//                    altUniformTexIds.put(unsortedSwapTexs[j].getName(), unsortedSwapTexs[j].getID());
//
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//                unSortedTextures[j].setFrameBufferIdx(framebufferSize ); //set teh render texture framebuffer idx
//                unsortedSwapTexs[j].setFrameBufferIdx(framebufferSize + 1);
//
//
//
//                j++;
//            }
//
//            try {
//
//                 //cannot forget to reset this between framebuffer creation or any fb > window size will also get depth
//                //can be i cause its inclusive in this case
//                //there should only be a single framebuffer with the size of Gdx.graphics.getWidth unless im stupid and my code completly doesnt work
//                if (unSortedTextures[i].getWidth() == Gdx.graphics.getWidth() && unSortedTextures[i].getHeight() == Gdx.graphics.getHeight()) {
//                    //create the framebuffer with a depth atachment
//                    if (depthTexture != null) {
//                        throw new RuntimeException("DEPTH TEXTURE IS NOT NULL AND TRYING TO OVERWRITE");
//                    }
//                    depthTexture = new ShadowTexture(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), GL20.GL_DEPTH_COMPONENT);
//                    framebuffers[framebufferSize] = new FrameBuffer(unSortedTextures,startIdx, i, depthTexture);
//                    framebufferSize++;
//                }else {
//                    framebuffers[framebufferSize] = new FrameBuffer(unSortedTextures,startIdx, i, null);
//                    framebufferSize++;
//                }
//
//                framebuffers[framebufferSize] = new FrameBuffer(unsortedSwapTexs,startIdx, i, null);//swap buffer doesnt need a depthAttachment should onyl be used in composite passes
//                framebufferSize++;
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//
//
//
//        }
//        Arrays.sort(unSortedTextures, Comparator.comparingInt(BufferTexture::getAttachmentNum));
//        Arrays.sort(unsortedSwapTexs, Comparator.comparingInt(BufferTexture::getAttachmentNum));
//        this.renderTextures = unSortedTextures;
//        this.swapBufferStorage = unsortedSwapTexs;
//        //generate attachment mappings
//        for (int i = 0; i < this.NUM_RENDER_TEXTUERS; i++) {
//            this.attachmentMapping[this.renderTextures[i].getAttachmentNum() - GL32.GL_COLOR_ATTACHMENT0] = i;
//        }
//        System.arraycopy(this.renderTextures, 0, this.uniformTextures, 0, this.renderTextures.length);
//
//        //add clear Color
//
//        if (ShaderPackLoader.shaderPackOn) {
//            SimplyShaders.LOGGER.info("beginning clear Texture Write");
//            for (int i =0; i < ShaderPackLoader.packSettings.disableBufferClearing.size; i++) {
//
//                int bufferNum = ShaderPackLoader.packSettings.disableBufferClearing.get(i);
//                SimplyShaders.LOGGER.info("Clear disabled for bufferNum {}", bufferNum );
//                bufferNum = this.findTextureIdxFromAttachmentNum(bufferNum);
//                if (bufferNum >= 0 && bufferNum < renderTextures.length) {
//                    renderTextures[bufferNum].clearTexture = false;
//                    swapBufferStorage[bufferNum].clearTexture = false;
//                    SimplyShaders.LOGGER.info("Tex Clear Actually disabled {} : {}", bufferNum, renderTextures[bufferNum].getAttachmentNum());
//                } else {
//                    SimplyShaders.LOGGER.info("buffer Num  {}, is out of range ", bufferNum);
//                }
//            }
//        }
//
//        //ClearColor
//        //tmp until i support pack specific colors
//        setTextureClearColor(skyColor, 0); //FIXME this doesnt take into acount the mappigns renderTextuers isnt  length 8
//
//        setTextureClearColor(Color.WHITE, 1); //if it doesnt exist setTextureClearColor will just return
//            for (int i = 2; i < NUM_RENDER_TEXTUERS; i++) {
//
//                setTextureClearColor(transparent, i);
//            }
//
//
//
//
//
//    }

    public RenderTextureHolder(BufferTexture[] textures) {
        this.framebuffers = new Array<>();

        if (MAX_NUM_RENDER_TEXTURES < textures.length) {
            for (BufferTexture tex: textures) { //should no longer be needed
                tex.dispose();
            }
            throw new RuntimeException(" RENDER TEXTUIRES EXCEEDS 8");
        }
        NUM_RENDER_TEXTUERS = textures.length;
        SimplyShaders.LOGGER.info("NUM_RENDER_TEXTUERS {}", NUM_RENDER_TEXTUERS);
//        renderTextures = new BufferTexture[NUM_RENDER_TEXTUERS];
        alternateTextures = new BufferTexture[NUM_RENDER_TEXTUERS];
        mainTextures = textures;



        try {
            depthTexture = new ShadowTexture(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), GL20.GL_DEPTH_COMPONENT);
        } catch ( Exception e) {
            throw new RuntimeException(e);
        }




        // swapBufferStorage = new BufferTexture[NUM_RENDER_TEXTUERS];
        for (int i = 0; i < NUM_RENDER_TEXTUERS; i++) {
            //the swap texture cant be set to mipMap linear if i never generate mip maps for it
            //I belive the textures can be swapped around though so that
            alternateTextures[i] = new BufferTexture(mainTextures[i].getName(), mainTextures[i].getWidth(), mainTextures[i].getHeight(), mainTextures[i].getPixelFormat(), mainTextures[i].getInternalFormat(), mainTextures[i].getAttachmentNum(),mainTextures[i].isMipMapEnabled);
        }

        try {
            for (int i = 0; i < NUM_RENDER_TEXTUERS; i++) {
                mainTextures[i].genTexture();
                alternateTextures[i].genTexture();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //FIXME this needs to be redone once i redo the base game framebuffer creation / handling
        for (int i = 0; i < this.NUM_RENDER_TEXTUERS; i++) {
            this.attachmentMapping[this.mainTextures[i].getAttachmentNum() - GL32.GL_COLOR_ATTACHMENT0] = i;
        }




        if (ShaderPackLoader.shaderPackOn) {
            SimplyShaders.LOGGER.info("beginning clear Texture Write");
            for (int i =0; i < ShaderPackLoader.packSettings.disableBufferClearing.size; i++) {

                int bufferNum = ShaderPackLoader.packSettings.disableBufferClearing.get(i);
                SimplyShaders.LOGGER.info("Clear disabled for bufferNum {}", bufferNum );
                bufferNum = this.findTextureIdxFromAttachmentNum(bufferNum);
                if (bufferNum >= 0 && bufferNum < alternateTextures.length) {
                    mainTextures[bufferNum].clearTexture = false;
                    alternateTextures[bufferNum].clearTexture = false;
                    SimplyShaders.LOGGER.info("Tex Clear Actually disabled {} : {}", bufferNum, mainTextures[bufferNum].getAttachmentNum());
                } else {
                    SimplyShaders.LOGGER.info("buffer Num  {}, is out of range mapping {}, \nmainTexes {} ", bufferNum, attachmentMapping, mainTextures);
                }
            }
        }

        setTextureClearColor(skyColor, 0); //FIXME this doesnt take into acount the mappigns renderTextuers isnt  length 8

        setTextureClearColor(Color.WHITE, 1); //if it doesnt exist setTextureClearColor will just return
        for (int i = 2; i < NUM_RENDER_TEXTUERS; i++) {

            setTextureClearColor(transparent, i);
        }


        BufferTexture[] temp1 = new BufferTexture[NUM_RENDER_TEXTUERS];
        int sortedTexturesSize = 0;

        for (int i = 0; i < NUM_RENDER_TEXTUERS; i++) {
            if (mainTextures[i].clearTexture) {
                temp1[sortedTexturesSize++]= mainTextures[i];
            }
        }
        BufferTexture[] sortedTextures = new BufferTexture[sortedTexturesSize];
        System.arraycopy(temp1,0,sortedTextures,0,sortedTexturesSize);
        Arrays.sort(sortedTextures, Comparator.comparingInt(BufferTexture::getWidth).thenComparingInt(BufferTexture::getHeight));
        this.clearFrameBuffers = new Array<>();
        int j = -1;
        boolean addedDepth = false;
        for (int i = 0; i < sortedTexturesSize;i ++) {

            int testIdx = i;

            if (testIdx + 1 < sortedTexturesSize) {
                if (sortedTextures[i].getWidth() == sortedTextures[i + 1].getWidth() && sortedTextures[i].getHeight() == sortedTextures[i + 1].getHeight()) {
                    //if both textures have the same dimensions they should go in the same FB

                    continue;
                }
            }

            BufferTexture[] temp = new BufferTexture[i -j];
            System.arraycopy(sortedTextures, j + 1, temp,0,i-j);

            try {
                ShadowTexture depthTex;
                SimplyShaders.LOGGER.info("creating clearFramebuffer with {} \nMain textures {} \n MAIN MAIN{}", Arrays.toString(temp), Arrays.toString(sortedTextures), mainTextures.length);
                if (temp[0].getWidth() == Gdx.graphics.getWidth() && temp[0].getHeight() == Gdx.graphics.getHeight()) {
                    depthTex = depthTexture;
                    addedDepth = true;
                } else {
                    depthTex = null;
                }
                FrameBuffer fb  = new FrameBuffer(temp,depthTex);

                framebuffers.add(fb);
                clearFrameBuffers.add(fb);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            j = i;


        }
        if (!addedDepth) {
            throw new RuntimeException(" NO DEPTH BUFFER ADDED TO CLEAR FrameBuffers ie no framebuffres match gdx height/widht");
        }



        //needs to create teh main framebuffre
        IntArray baseDrawBuffers = ShaderPackLoader.baseGameDrawbuffers;
        if(baseDrawBuffers == null) {
            baseDrawBuffers = new IntArray(2);
            baseDrawBuffers.add(GL32.GL_COLOR_ATTACHMENT0); //FIXME
        }
        BufferTexture[] tmpMain = new BufferTexture[baseDrawBuffers.size];
        //copy over the textures needed for the base game framebuffer






        for (int i = 0; i < tmpMain.length; i++) {
            tmpMain[i] = mainTextures[this.attachmentMapping[baseDrawBuffers.get(i) - GL32.GL_COLOR_ATTACHMENT0]];// FIXME i need to fix GameShader giving values in Attachment range
        }
        try {

            baseGameFrameBuffer = new FrameBuffer(tmpMain,depthTexture);
            framebuffers.add(baseGameFrameBuffer);
//            clearFrameBuffer = new FrameBuffer(textures,depthTexture); //problem here is i need a secondary clear buffer if the texture is a different size
//            framebuffers.add(clearFrameBuffer);
        } catch (Exception e) {
            //probably dispose the framebuffer
            throw new RuntimeException(e);
        }






        //needs to create shadow framebuffer currently done in shadows but should be moved




    }

    /**
     * @param writeToAlt array of bools which is used to get either the main tex or alt tex
     * @param drawBuffers array of stage Drawbuffers 0-16 / max_render_targets not in GL_ATTACHMENT range
     * @return framebuffer reference which is managed by RenderTextureHolder //might want to manage in CompositeRenderer
     * //uses the size from the given drawBuffer Textures
     */
    public FrameBuffer createColorFramebuffer(boolean[] writeToAlt, int[] drawBuffers, GameShader shader) {

        int[] attachmentDrawBuffers = new int[drawBuffers.length];
        int height = -1;
        int width = -1;
        for (int i = 0; i < drawBuffers.length; i++) {
            attachmentDrawBuffers[i] = i + GL32.GL_COLOR_ATTACHMENT0;
        }


        FrameBuffer fb = new FrameBuffer(attachmentDrawBuffers);

        Gdx.gl.glBindFramebuffer(GL32.GL_FRAMEBUFFER,fb.getFboHandle());
        for (int i = 0; i < drawBuffers.length; i++) {
            int drawBuffer = drawBuffers[i];
            BufferTexture tex = getBufferTexture(writeToAlt[drawBuffer],drawBuffer);
            int newHeight = tex.getHeight();
            int newWidth = tex.getWidth();
            if ((height > 0 && newHeight != height) || (width > 0 && newWidth != width)) {
                throw new RuntimeException("TEX scales dont match prevoius");
            }
            SimplyShaders.LOGGER.info("adding attachment {} {}",tex.getID(), tex.getName());
            Gdx.gl.glFramebufferTexture2D(GL32.GL_FRAMEBUFFER, i + GL32.GL_COLOR_ATTACHMENT0, GL20.GL_TEXTURE_2D, tex.getID(), 0);
            height =newHeight;
            width = newWidth;
        }
        fb.setHeight(height);
        fb.setWidth(width);
        GL32.glDrawBuffers(attachmentDrawBuffers);
        //AHAHAHAHA I need to overwrite the gameShader buisness because its still there
        ((GameShaderInterface)shader).setEnableDrawBuffers(false);
        SimplyShaders.LOGGER.info("DRAW BUFFERS {}", attachmentDrawBuffers);
        if (Gdx.gl.glCheckFramebufferStatus(GL32.GL_FRAMEBUFFER) != GL32.GL_FRAMEBUFFER_COMPLETE ) {
            throw new RuntimeException("Could not create Color FrameBuffer " + Arrays.toString(drawBuffers));
        }
        Gdx.gl.glBindFramebuffer(GL32.GL_FRAMEBUFFER, 0);
        framebuffers.add(fb);
        return fb;


    }

    public int getRenderTexture(boolean writeToAlt, int drawBuffer) {
        return writeToAlt ? getAltTex(drawBuffer).getID() : getMainTex(drawBuffer).getID();
    }
    public BufferTexture getBufferTexture(boolean useAltBuf, int drawBuffer) {
        return useAltBuf ? getAltTex(drawBuffer) : getMainTex(drawBuffer);
    }
    public BufferTexture getAltTex(int drawBuffer) {
        int texLocation = attachmentMapping[drawBuffer];
        if (texLocation < 0 || texLocation > mainTextures.length) {
            SimplyShaders.LOGGER.info("UH OH, texLoc {}, drawBuffer {}, attachmentMap {}", texLocation, drawBuffer, attachmentMapping);
        }
        return alternateTextures[texLocation];
    }
    public BufferTexture getMainTex(int drawBuffer) {
        int texLocation = attachmentMapping[drawBuffer];
        if (texLocation < 0 || texLocation > mainTextures.length) {
            SimplyShaders.LOGGER.info("UH OH, texLoc {}, drawBuffer {}, attachmentMap {}", texLocation, drawBuffer, attachmentMapping);
        }
        return mainTextures[texLocation];
    }
    private void setTextureClearColor(Color color , int bufNum) {
        int texNum = attachmentMapping[bufNum];
        if (texNum >= 0 && texNum < NUM_RENDER_TEXTUERS) {
            mainTextures[texNum].clearColor = color;
            alternateTextures[texNum].clearColor = color;
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


        if (mainTextures != null) {
            for(BufferTexture tex: mainTextures) {
                tex.dispose();

            }
        }
        if (alternateTextures != null) {
            for(BufferTexture tex: alternateTextures) { //Todo instead of creating a bunch of unused textures make renderFbo reload when the shaderpack loads so i can set swap to only have the needed textures
                //would need to null check if i only make needed textures
                tex.dispose();
            }
        }

        alternateTextures = null;
        mainTextures = null;

    }

    public void clearTextures(Zone playerZone) {//FIXME naive approach but just to get things working

        Sky sky = Sky.getCurrentSky(playerZone);

                skyColor.set(sky.currentSkyColor);
        for (FrameBuffer buf: this.clearFrameBuffers.iterator()) {
            this.bindFrameBuffer(buf);
            Gdx.gl.glViewport(0,0, buf.getWidth(),buf.getHeight());
            if (buf.hasDepth) {
                GL32.glClearBufferfv(GL32.GL_DEPTH, 0, WHITE); //clear the depth tex always
            }
            for (int attachment: buf.attachmentDrawBuffers) {
                int drawBuffer = attachment - GL32.GL_COLOR_ATTACHMENT0;
                BufferTexture tex = this.getBufferTexture(false, drawBuffer);
                if (!tex.clearTexture) {
                    continue;
                }

                Color c = tex.clearColor;
                temporaryColor[0] = c.r;
                temporaryColor[1] = c.g;
                temporaryColor[2] = c.b;
                temporaryColor[3] = c.a;


                GL32.glClearBufferfv(GL32.GL_COLOR,drawBuffer, temporaryColor);
            }
        }
        Gdx.gl.glBindFramebuffer(GL32.GL_FRAMEBUFFER,0);



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



    public void setCompositeViewPort(float viewportScale) {
        //needs to change viewPort based on the previous viewport resolution


        Gdx.gl.glViewport(0,0,(int)  (viewportScale * Gdx.graphics.getWidth()),(int) (viewportScale * Gdx.graphics.getHeight()));


    }




    public int findTextureIdxFromAttachmentNum(int attachmentNum) {

        return this.attachmentMapping[attachmentNum];
    }
}
