package com.shfloop.simply_shaders.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;

import com.shfloop.simply_shaders.GameShaderInterface;
import com.shfloop.simply_shaders.Shadows;
import com.shfloop.simply_shaders.SimplyShaders;
import finalforeach.cosmicreach.rendering.shaders.ChunkShader;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.settings.GraphicsSettings;
import finalforeach.cosmicreach.util.Identifier;
import finalforeach.cosmicreach.world.Sky;





public class CompositeShader extends GameShader {


    public CompositeShader(Identifier vertexShader, Identifier fragmentShader) {
        super(vertexShader,fragmentShader);
        this.allVertexAttributesObj = new VertexAttributes(new VertexAttribute[]{VertexAttribute.Position(), VertexAttribute.TexCoords(0) });


    }

    public void bind(Camera worldCamera) {


        this.bindDrawBuffers();
        super.bind(worldCamera);
        int texNum= 0;


        //may want to go back with set strings, not sure whats better
        for (BufferTexture tex: RenderFBO.uniformTextures) {
            texNum= this.bindOptionalTextureI(tex.getName(), tex.getID(),texNum);
        }


        texNum= this.bindOptionalTexture("noiseTex", ChunkShader.noiseTex, texNum);
        texNum= this.bindOptionalTextureI("depthTex0", SimplyShaders.buffer.depthTex0.id, texNum);
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

                SimplyShaders.buffer.undoUniformPingPong(pingPongBufferNum);
            }//should swap the textuers before i call glDrawBuffers i think not really sure if i have to
        }
    }
    private void bindDrawBuffers() {
        //bind the appropriate outbuffers based on what the shader loaded from file
        //i can do this at the start no problem
        int[] shaderInputBuffers = ((GameShaderInterface)this).getShaderInputBuffers();
        if (shaderInputBuffers != null) {
            for (int pingPongBufferNum: shaderInputBuffers) {

                SimplyShaders.buffer.pingPongBuffer(pingPongBufferNum);
            }//should swap the textuers before i call glDrawBuffers i think not really sure if i have to
        }








    }


}