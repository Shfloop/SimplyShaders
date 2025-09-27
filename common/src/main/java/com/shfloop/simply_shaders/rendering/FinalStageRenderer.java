package com.shfloop.simply_shaders.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.shfloop.simply_shaders.GameShaderInterface;
import com.shfloop.simply_shaders.SimplyShaders;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL32C;
import org.lwjgl.opengl.GL43;

public class FinalStageRenderer {


    private BufferTexture[] readTextures;
    IntArray mipMappedTexIdx;
    FinalShader finalShader;
    RenderTextureHolder holder;
    BufferTexture[] clearDisabledCopiedTextures;
    public FinalStageRenderer(FinalShader shader, boolean[] flippedBuffers, RenderTextureHolder holder, IntArray mipMappedTexIdx) {

        this.holder = holder;
        this.mipMappedTexIdx = mipMappedTexIdx;
        boolean[] readFromAlt = new boolean[flippedBuffers.length];
        System.arraycopy(flippedBuffers,0,readFromAlt, 0, flippedBuffers.length);

        int [] shaderReadTextures = ((GameShaderInterface)shader).getShaderInputBuffers(); //TODO currently input buffers is just pingponged buffers
        if (shaderReadTextures == null) {
            shaderReadTextures = new int[1];
            //SimplyShaders.LOGGER.info("SHADER READ TEXTURE NULL");
            //will get the default value 0
        }
        readTextures = new BufferTexture[shaderReadTextures.length];
        for (int x = 0; x <shaderReadTextures.length; x++) {
            int drawBufferNum = shaderReadTextures[x];
            readTextures[x] = holder.getBufferTexture(readFromAlt[drawBufferNum], drawBufferNum );// i can use the same thing because this getRenderTexture will get the alt texture if true and with readsFromAlt true means the alt textrure
            //SimplyShaders.LOGGER.info("FINAL Texture {}, id {} fromAlt? {}",drawBufferNum, readTextures[x].getID(), readFromAlt[drawBufferNum]);
        }
        finalShader = shader;

        //textures that need to be copied from allt to main if they arent cleared

        //weird way about getting the values

        Array<BufferTexture> temp = new Array<>(8);
        for (int i = 0; i < flippedBuffers.length; i++) {
            //i is the drawbuffer
            boolean isFlipped = flippedBuffers[i];
            if (!isFlipped) {
                continue; // if its not flipped continue
            }
            BufferTexture altTex = holder.getBufferTexture(true, i);

            if (!altTex.clearTexture) {
                temp.add(altTex);
                //SimplyShaders.LOGGER.info("COPYING ALT TO MAIN FOR TEX {}", altTex);
                //if the texture has clearing false then we want to use this data for the next frame so it needs to be copied to main
                //another way to avoid copying is to have a duplicate of everything (composite staeg, final stage etc with duplicate framebuffers but have the buffers be aligned so the ones that would be copied are flipped

            }
        }
        this.clearDisabledCopiedTextures = new BufferTexture[temp.size];
        System.arraycopy(temp.items,0,this.clearDisabledCopiedTextures, 0, temp.size);







    }
    public void render(Camera worldCamera) {

        Gdx.gl.glViewport(0,0,Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glBindFramebuffer(GL32.GL_FRAMEBUFFER, 0);

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);

        FinalShader finalShader = FinalShader.DEFAULT_FINAL_SHADER;
        finalShader.bind(worldCamera, readTextures);
        SimplyShaders.screenQuad.render(finalShader.shader, GL20.GL_TRIANGLE_FAN); //as long as this is in the pool of shaders to get updated with colertexture spots i dont need to bind textures in shader
        finalShader.unbind();

        //reset mipmapped textuers format
        if (mipMappedTexIdx.size > 0) {
            Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
            for(int i = 0; i < mipMappedTexIdx.size; i++) {
                Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, mipMappedTexIdx.get(i)); //will actually need the bufferTexture
                    GL32C.glTexParameteri(GL20.GL_TEXTURE_2D,GL20.GL_TEXTURE_MIN_FILTER,GL32C.GL_LINEAR); //TODO this needs to be GL_NEAREST if the texture isnt a float buffer

            }
        }

        // copy alt to main if the buffer clear is false
        //fuck it use gl4
        for (BufferTexture altTex: this.clearDisabledCopiedTextures) {
            int mainTex = holder.getRenderTexture(false, altTex.getAttachmentNum() - GL32.GL_COLOR_ATTACHMENT0); //FIXME should change to drawbuffer numebring
            GL43.glCopyImageSubData(altTex.getID(),GL43.GL_TEXTURE_2D,0,0, 0, 0,
                    mainTex, GL43.GL_TEXTURE_2D, 0, 0, 0, 0,
                    altTex.getWidth(), altTex.getHeight(), 1);
        }


    }
}
