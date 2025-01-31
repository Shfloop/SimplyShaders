package com.shfloop.simply_shaders.rendering;

import com.badlogic.gdx.Gdx;
import com.shfloop.simply_shaders.ShadowTexture;
import com.shfloop.simply_shaders.SimplyShaders;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;

public class FrameBuffer {
    public  int[] lastDrawBuffers;
    public  final int[] allDrawBuffers;
    private final int fboHandle;
    public final boolean hasDepth;

    public FrameBuffer(BufferTexture[] renderTextures, int startIdx, int endIdx, ShadowTexture depthAttachment) throws Exception { //render textures needs to be sorted
        fboHandle = Gdx.gl.glGenFramebuffer();


        Gdx.gl.glBindFramebuffer(GL32.GL_FRAMEBUFFER, fboHandle);
        this.allDrawBuffers = new int[endIdx + 1 - startIdx];
        for(int i = 0; i < allDrawBuffers.length; i++) {
            int offset = i + startIdx;
            int attachmentNum = renderTextures[offset].getAttachmentNum();
            if (attachmentNum < GL32.GL_COLOR_ATTACHMENT0 ||attachmentNum >= GL32.GL_COLOR_ATTACHMENT8) {
                throw new RuntimeException("Attachment not correct " + attachmentNum);
            }
            if (renderTextures[offset].getID() == -1) {
                throw new RuntimeException("ID is -1");
            }


            Gdx.gl.glFramebufferTexture2D(GL32.GL_FRAMEBUFFER, renderTextures[offset].getAttachmentNum(), GL20.GL_TEXTURE_2D, renderTextures[offset].getID(), 0);
            allDrawBuffers[i] = renderTextures[offset].getAttachmentNum();



        }
       

        if (depthAttachment != null) {
            Gdx.gl.glFramebufferTexture2D(GL32.GL_FRAMEBUFFER, GL32.GL_DEPTH_ATTACHMENT, GL20.GL_TEXTURE_2D, depthAttachment.id, 0);
            hasDepth = true;
        } else {
            hasDepth = false;
        }



        lastDrawBuffers = allDrawBuffers;
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

}
