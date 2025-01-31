package com.shfloop.simply_shaders.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;

import com.shfloop.simply_shaders.GameShaderInterface;
import com.shfloop.simply_shaders.Shadows;
import com.shfloop.simply_shaders.SimplyShaders;
import com.shfloop.simply_shaders.pack_loading.ShaderPackLoader;
import finalforeach.cosmicreach.rendering.shaders.ChunkShader;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.settings.GraphicsSettings;
import finalforeach.cosmicreach.util.Identifier;
import finalforeach.cosmicreach.world.Sky;
import org.lwjgl.opengl.GL32;


public class CompositeShader extends GameShader {


    private float shaderStageScale ;

    public CompositeShader(Identifier vertexShader, Identifier fragmentShader) {
        super(vertexShader,fragmentShader);
        this.allVertexAttributesObj = new VertexAttributes(new VertexAttribute[]{VertexAttribute.Position(), VertexAttribute.TexCoords(0) });

        //drawbuffers should be created becuse super calls reload()



    }
    //when bindDrawBuffers() is called to flip any buffers that are reading and writing in the same shader
    //this needs to be called after to setup up the uniform texture for the next shader pass
    //a future optimization is this only needs to happen after a swap its not needed between two swaps but it doesnt hurt just duplicates the opperation
    public void unbind() {
        this.resetUniformBuffers();
    }

    public void bind(Camera worldCamera) {

        SimplyShaders.holder.setCompositeViewPort(this.shaderStageScale);
        this.bindFrameBuffers(); //call before super because this will bindthe right framebuffer for the shaderstage
        super.bind(worldCamera);
        int texNum= 0;


        //may want to go back with set strings, not sure whats better
        for (BufferTexture tex: SimplyShaders.holder.uniformTextures) {
            texNum= this.bindOptionalTextureI(tex.getName(), tex.getID(),texNum);
        }


        texNum= this.bindOptionalTexture("noiseTex", ChunkShader.noiseTex, texNum);
        texNum= this.bindOptionalTextureI("depthTex0", SimplyShaders.holder.depthTexture.id, texNum);
        if (Shadows.initalized) {
            texNum= this.bindOptionalTextureI("shadowMap", Shadows.shadow_map.getDepthMapTexture().id, texNum);
            this.bindOptionalUniform3f("lightDir", Shadows.getCamera().direction);
        }

        Sky sky = Sky.currentSky;
        this.bindOptionalUniform3f("skyAmbientColor", sky.currentAmbientColor);
        this.bindOptionalInt("renderFar", GraphicsSettings.renderDistanceInChunks.getValue() * 16); //chunks are 16 blocks wide
        this.bindOptionalUniform3f("cameraDirection", worldCamera.direction); //i might be able to find this in final shader
        this.bindOptionalMatrix4("invProjView", worldCamera.invProjectionView);

        this.bindOptionalMatrix4("u_projViewTrans", worldCamera.combined);
        //this.bindOptionalInt("renderNear", GraphicsSettings.renderDistanceInChunks.getValue() * 32);
        this.bindOptionalMatrix4("u_proj", worldCamera.projection);
        this.bindOptionalMatrix4("u_view", worldCamera.view);
        this.bindOptionalFloat("frameTimeCounter", (float) Gdx.graphics.getFrameId() );
        this.bindOptionalFloat("viewWidth", Gdx.graphics.getWidth());
        this.bindOptionalFloat("viewHeight", Gdx.graphics.getHeight());
        this.bindOptionalUniform3f("previousCameraPosition", Shadows.previousCameraPosition);
        this.bindOptionalMatrix4("u_projPrev", Shadows.previousProjection);
        this.bindOptionalMatrix4("u_viewPrev", Shadows.previousView);
        this.bindOptionalMatrix4("u_projInverse", worldCamera.projection.cpy().inv());
        this.bindOptionalMatrix4("u_viewInverse", worldCamera.view.cpy().inv());






    }
    private int bindOptionalTextureI(String uniform_name, int id,int texNum) {
        int u = this.shader.getUniformLocation(uniform_name);

        if (u != -1) {
            Gdx.gl.glActiveTexture(org.lwjgl.opengl.GL20.GL_TEXTURE0 + texNum);//just for right now this works i thinki
            Gdx.gl.glBindTexture(org.lwjgl.opengl.GL20.GL_TEXTURE_2D, id);
            this.shader.setUniformi(u, texNum);
            return texNum + 1;
        } else {
            return texNum;
        }
    }
    private void resetUniformBuffers() {
        int[] shaderInputBuffers = ((GameShaderInterface)this).getShaderInputBuffers();
        if (shaderInputBuffers != null) {
            for (int pingPongBufferNum: shaderInputBuffers) {

                SimplyShaders.holder.undoUniformPingPong(pingPongBufferNum);
            }//should swap the textuers before i call glDrawBuffers i think not really sure if i have to
        }
    }
    @Override
    public void reload() {
        super.reload();
        if (ShaderPackLoader.packSettings == null) {
            this.shaderStageScale = 1.0f;
            return ;
        }
        int [] shaderDrawBuffers = ((GameShaderInterface)(this)).getShaderDrawBuffers();
        float testingScale =  ShaderPackLoader.packSettings.bufferTexturesScale.getOrDefault("colorTex"+ (shaderDrawBuffers[0] - GL32.GL_COLOR_ATTACHMENT0), 1.0f);
        for (int i = 1; i< shaderDrawBuffers.length; i++) {
            float newScale =  ShaderPackLoader.packSettings.bufferTexturesScale.getOrDefault("colorTex"+ (shaderDrawBuffers[i] - GL32.GL_COLOR_ATTACHMENT0), 1.0f);
            if (testingScale != newScale) {
                throw new RuntimeException("SHADER DRAW BUFFERS SCALE DONT MATCH");
            }

        }
        this.shaderStageScale = testingScale;
        SimplyShaders.LOGGER.info("SHADER STAGE SCALE SET TO {}", testingScale);
    }
    //Needs to happen before super.bind because super.bind will call GLDrawbuffers and the right Framebuffer needs to be set
    private void bindFrameBuffers() {
        //bind the appropriate outbuffers based on what the shader loaded from file
        //i can do this at the start no problem
        int[] shaderInputBuffers = ((GameShaderInterface)this).getShaderInputBuffers();
        if (shaderInputBuffers != null) {
            for (int pingPongBufferNum: shaderInputBuffers) {

                SimplyShaders.holder.pingPongBuffer(pingPongBufferNum);
            }//should swap the textuers before i call glDrawBuffers i think not really sure if i have to
        }








    }


}
