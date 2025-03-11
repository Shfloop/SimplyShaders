package com.shfloop.simply_shaders.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.shfloop.simply_shaders.GameShaderInterface;
import com.shfloop.simply_shaders.SimplyShaders;
import com.shfloop.simply_shaders.pack_loading.PackDirectives;
import com.shfloop.simply_shaders.pack_loading.ShaderDirectives;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL32C;

//deals with any fullscreen pass
//give it the array of CompositeShaders
//intended to be used with deferred pre and post
//i need a shared list of flipped buffers 0-16 which are the rendertargets or 0-8 for right now
//
public class CompositeStageRenderer {
    //need to add teh shadowTextures but this might be doable outside of init
    //
    Array<Pass> passes = new Array<>();
    RenderTextureHolder holder ;
    public CompositeStageRenderer( CompositeShader[] shaders, BufferTexture[] shadowTextures, boolean[] flippedBuffers, PackDirectives packDirectives, RenderTextureHolder holder, IntArray mipMappedTexIdx ) {
        this.holder = holder;
        for (int i = 0; i < shaders.length; i++) {
            CompositeShader shader = shaders[i];
            //ShaderDirectives shaderDirectives = ((GameShaderInterface) shader).getShaderDirectives(); //TODO
            //int[] drawBuffers = shaderDirectives.getDrawBuffers();
            int[]shaderDrawBuffers = ((GameShaderInterface)shader).getShaderDrawBuffers(); //THIS IS IN ATTACHMENT NUMBERS
            //flipped buffers points to the last stages renderTargets if true the last stage wrote to alt so this stage should read from alt
            int [] drawBuffers = new int[shaderDrawBuffers.length];
            ;
            //we should set the passes texture uniforms here // so there should eb a String[] with the texture names and we need to add the bufferTexture int associated
            boolean[] readFromAlt = new boolean[flippedBuffers.length];
                    System.arraycopy(flippedBuffers,0,readFromAlt, 0, flippedBuffers.length);


                    //TEMPORARY UNTIL I SORT OUT DRAWBUFFER IN GAMESHADERMIXIN
            //the shader drawBufer gets overwritten with the first few color attachments so its not reliable on resize
            //so instead we need to keep drawbuffers final in shader and just have a boolean which controls wheter the shader calls drawbuffers;
            for(int x = 0; x < shaderDrawBuffers.length; x++) { // edit and copy the attachments to buffer num
                drawBuffers[x] = shaderDrawBuffers[x] - GL32.GL_COLOR_ATTACHMENT0;
                flippedBuffers[drawBuffers[x]] ^=true;
                SimplyShaders.LOGGER.info("WRITE TO ALT {} for buf {}", flippedBuffers[drawBuffers[x]], drawBuffers[x]);
            }

           //use flipped buffer to get the renderTargets and create the framebufer here
            FrameBuffer fb = holder.createColorFramebuffer(flippedBuffers,drawBuffers,shader); //flipped buffers doesnt need to be copied becuse it is just used to find the appropriate Textures


            //how do we know which to take the uniform from
            // if flipped buffers is all false than the last stage wrote to main
            //if flipped buffers is true than the previous stage wrote to alt so the uniform should be the texture alt

            //pass needs the shader width / height, scale, framebuffer, drawbuffers, copyOfFlipped Textures post flip so flipped textures can be used to get what this stage should render to
            //needs the CompositeShader, buffers to mipmap,


            //edit the textureuniforms to point to the values provided by readFromAlt
            //get all the passes uniforms names ie colorTex1 etc
            //need an attachments map

            int [] shaderReadTextures = ((GameShaderInterface)shader).getShaderInputBuffers(); //TODO currently input buffers is just pingponged buffers
            BufferTexture [] readTextures = new BufferTexture[shaderReadTextures.length];
            for (int x = 0; x <shaderReadTextures.length; x++) {
                int drawBufferNum = shaderReadTextures[x];
                readTextures[x] = holder.getBufferTexture(readFromAlt[drawBufferNum], drawBufferNum );// i can use the same thing because this getRenderTexture will get the alt texture if true and with readsFromAlt true means the alt textrure
                SimplyShaders.LOGGER.info("Read Texture {}, id {} fromAlt? {}",drawBufferNum, readTextures[x].getID(), readFromAlt[drawBufferNum]);
            }

            //set the pass mip maps and get the coorisponding alt or main texture
            IntArray mipMapEnabledBuffers = ((GameShaderInterface)shader).getShaderMipMapEnabled();
            BufferTexture[] mipMapTexes = new BufferTexture[mipMapEnabledBuffers.size];
            for(int x = 0; x< mipMapTexes.length;x++) {
                int drawBufferNum = mipMapEnabledBuffers.get(x);
                mipMapTexes[x] = holder.getBufferTexture(readFromAlt[drawBufferNum],mipMapEnabledBuffers.get(x) );
                if(!mipMappedTexIdx.contains(mipMapTexes[x].getAttachmentNum())) {
                    mipMappedTexIdx.add(mipMapTexes[x].getID());
                    SimplyShaders.LOGGER.info("MIP MAP {}, id {} fromAlt? {}",drawBufferNum, mipMapTexes[x].getID(), readFromAlt[drawBufferNum]);
                }
            }






            Pass pass = new Pass();
            pass.drawBuffers = drawBuffers;
            pass.readTextures = readTextures;
            pass.stageReadsFromAlt = readFromAlt;
            pass.frameBuffer = fb;
            pass.shader = shaders[i];
            pass.mipMapTexes = mipMapTexes;
            pass.width = fb.getWidth();
            pass.height= fb.getHeight();



            passes.add(pass);
        }
    }
    //temporaraly need worldCamera if im using the GameShaderBind
    public void render(Camera worldCamera) {
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST); // disable it so screen quad doesnt get removed
        //i think i walso want to disable gl blend
        Gdx.gl.glDisable(GL20.GL_BLEND); //gl blend is enabled in batched zone renderer
        for (Pass pass: passes) {
            Gdx.gl.glViewport(0,0,pass.width,pass.height);
            if (pass.mipMapTexes.length > 0) {
                Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
                for (BufferTexture tex: pass.mipMapTexes) {
                    Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, tex.getID());
                    GL32C.glGenerateMipmap(GL20.GL_TEXTURE_2D);
                    GL32C.glTexParameteri(GL20.GL_TEXTURE_2D,GL20.GL_TEXTURE_MIN_FILTER,GL32C.GL_LINEAR_MIPMAP_LINEAR); //TODO this needs to be GL_NEAREST if the texture isnt a float buffer
                }
            }
            holder.bindFrameBuffer(pass.frameBuffer);
            pass.shader.bind(worldCamera, pass.readTextures);
            SimplyShaders.screenQuad.render(pass.shader.shader, GL20.GL_TRIANGLE_FAN);
            //no need to unbind the shader


        }
    }
    private class Pass {
        int[] drawBuffers; //not in GL_ATTACHMENT numbers is 0-16
        //drawbuffers will use
        int width;
        int height;
        float scale;
        FrameBuffer frameBuffer;

        //have whatever uniforms for this pass be added to this (including textures)
        //so when setting up pass add the Texture name and the texNum coorisponding to alt and main deppending
        //CustomUniforms uniforms;
        CompositeShader shader;
        boolean[] stageReadsFromAlt; //needed for mipmapping because we want to generate the mipMap for whatever the stage reads from
        BufferTexture[] mipMapTexes;

        //use this until i get proper uniforms in
        BufferTexture[] readTextures;

    }
}
