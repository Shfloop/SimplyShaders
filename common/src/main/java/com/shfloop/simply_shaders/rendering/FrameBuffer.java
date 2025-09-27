package com.shfloop.simply_shaders.rendering;

import com.badlogic.gdx.Gdx;
import com.shfloop.simply_shaders.ShadowTexture;
import com.shfloop.simply_shaders.SimplyShaders;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;

public class FrameBuffer {

    public int[] lastDrawBuffers;
    public int[] attachmentDrawBuffers;
    private final int fboHandle;
    public  boolean hasDepth;
    private final int MAX_ATTACHMENTS_NUM;

    private int height;
    private int width;
    public FrameBuffer(int[] attachmentDrawBuffers) {
        hasDepth = false;
        fboHandle = Gdx.gl.glGenFramebuffer();
        MAX_ATTACHMENTS_NUM = 8;
        this.attachmentDrawBuffers = attachmentDrawBuffers;




    }
    public void bind() {
        Gdx.gl.glBindFramebuffer(GL32.GL_FRAMEBUFFER, fboHandle);
    }

//    public void drawBuffers(int[] attachmentDrawBuffers) {
//        for (int bufNum: attachmentDrawBuffers) {
//            if (bufNum < GL32.GL_COLOR_ATTACHMENT0 || bufNum > GL32.GL_COLOR_ATTACHMENT0 + MAX_ATTACHMENTS_NUM) {
//                throw new RuntimeException("Buffer Num " + bufNum + "is out of range of attachments" );
//            }
//        }
//        this.attachmentDrawBuffers = attachmentDrawBuffers;
//        Gdx.gl.glBindFramebuffer(GL32.GL_FRAMEBUFFER, fboHandle);
//        Gdx.gl.glDraw
//    }
    public FrameBuffer(BufferTexture[] renderTextures,  ShadowTexture depthAttachment) throws Exception { //render textures needs to be sorted
        MAX_ATTACHMENTS_NUM = 8;
        fboHandle = Gdx.gl.glGenFramebuffer();


        Gdx.gl.glBindFramebuffer(GL32.GL_FRAMEBUFFER, fboHandle);
        this.attachmentDrawBuffers = new int[renderTextures.length];
        int index = 0;
        for(BufferTexture tex: renderTextures) {
            if (tex == null) {
                SimplyShaders.LOGGER.info("Skipping framebuffer Texture beacuse its null");
                continue;

            }

            int attachmentNum = tex.getAttachmentNum();
            if (attachmentNum < GL32.GL_COLOR_ATTACHMENT0 ||attachmentNum >= GL32.GL_COLOR_ATTACHMENT8) {
                throw new RuntimeException("Attachment not correct " + attachmentNum);
            }
            if (tex.getID() == -1) {
                throw new RuntimeException("ID is -1");
            }


            Gdx.gl.glFramebufferTexture2D(GL32.GL_FRAMEBUFFER, tex.getAttachmentNum(), GL20.GL_TEXTURE_2D, tex.getID(), 0);
            attachmentDrawBuffers[index++] = tex.getAttachmentNum();
            this.height = tex.getHeight();
            this.width = tex.getWidth();



        }
       

        if (depthAttachment != null) {
            Gdx.gl.glFramebufferTexture2D(GL32.GL_FRAMEBUFFER, GL32.GL_DEPTH_ATTACHMENT, GL20.GL_TEXTURE_2D, depthAttachment.id, 0);
            hasDepth = true;
        } else {
            hasDepth = false;
        }



        lastDrawBuffers = attachmentDrawBuffers;
        GL32.glDrawBuffers(lastDrawBuffers);

        if (Gdx.gl.glCheckFramebufferStatus(GL32.GL_FRAMEBUFFER) != GL32.GL_FRAMEBUFFER_COMPLETE ) {
            throw new Exception("Could not create FrameBuffer");
        }

//        fboTexture = new TextureRegion(fbo.getColorBufferTexture());
        Gdx.gl.glBindFramebuffer(36160, 0);

    }
    public int getFboHandle() {
        return fboHandle;
    }
    public void dispose() {

        Gdx.gl.glDeleteFramebuffer(fboHandle);
    }
    public int getHeight() {
        return this.height;
    }
    public int getWidth() {
        return this.width;
    }
    public void setHeight(int height) {
        this.height = height;
    }
    public void setWidth(int width) {
        this.width = width;
    }

}
