package com.shfloop.simply_shaders.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.shfloop.simply_shaders.Shadows;
import com.shfloop.simply_shaders.SimplyShaders;
import finalforeach.cosmicreach.rendering.shaders.ChunkShader;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.settings.GraphicsSettings;
import finalforeach.cosmicreach.world.Sky;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import java.util.Arrays;

public class FinalShader extends GameShader {
    public static FinalShader DEFAULT_FINAL_SHADER;
    private final boolean isComposite;

    public FinalShader(String vertexShader, String fragmentShader, boolean isComposite) {
        super(vertexShader,fragmentShader);
        this.allVertexAttributesObj = new VertexAttributes(new VertexAttribute[]{VertexAttribute.Position(), VertexAttribute.TexCoords(0) });

        this.isComposite = isComposite;
    }
    public void bind(Camera worldCamera) {
        super.bind(worldCamera);
        int texNum= 0;

        //this.bindOptionalTextureI("colorTex0", SimplyShaders.fbo.getTextureAttachments().get(0).getTextureObjectHandle(), texNum);




//        texNum= this.bindOptionalTextureI("colorTex0", SimplyShaders.buffer.attachment0.getID(),texNum); //this should also change based on shader
//        texNum= this.bindOptionalTextureI("colorTex1", SimplyShaders.buffer.attachment1.getID(),texNum);
//        texNum= this.bindOptionalTextureI("colorTex2", SimplyShaders.buffer.attachment2.getID(),texNum);
//        texNum= this.bindOptionalTextureI("colorTex3", SimplyShaders.buffer.attachment3.getID(),texNum);
//        texNum= this.bindOptionalTextureI("colorTex4", SimplyShaders.buffer.attachment4.getID(),texNum);
        //may want to go back with set strings, not sure whats better
        for (BufferTexture tex: RenderFBO.renderTextures) {
            texNum= this.bindOptionalTextureI(tex.getName(), tex.getID(),texNum);
        }


        texNum= this.bindOptionalTexture("noiseTex", ChunkShader.noiseTex, texNum);
        texNum= this.bindOptionalTextureI("depthTex0", SimplyShaders.buffer.depthTex0.id, texNum);
        Sky sky = Sky.currentSky;
        this.bindOptionalUniform3f("skyAmbientColor", sky.currentAmbientColor);
        this.bindOptionalInt("renderFar",GraphicsSettings.renderDistanceInChunks.getValue() * 32); //i think chunks are still 32
        //this.bindOptionalInt("renderNear", GraphicsSettings.renderDistanceInChunks.getValue() * 32);

        //Todo add near and far for render distance dependent fog

//        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
//        Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, texId);

                //this.bindOptionalInt(SimplyShaders.buffer.attachment0.getName(),texId);
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

}
