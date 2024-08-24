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
import org.lwjgl.opengl.GL30;

public class FinalShader extends GameShader {
    public FinalShader(String vertexShader, String fragmentShader) {
        super(vertexShader,fragmentShader);
        this.allVertexAttributesObj = new VertexAttributes(new VertexAttribute[]{VertexAttribute.Position(), VertexAttribute.TexCoords(0) });
    }
    public void bind(Camera worldCamera) {
        super.bind(worldCamera);
        int texNum= 0;

        //this.bindOptionalTextureI("colorTex0", SimplyShaders.fbo.getTextureAttachments().get(0).getTextureObjectHandle(), texNum);


        this.bindOptionalTextureI("colorTex0", SimplyShaders.buffer.attachment0.getID(),texNum);
        this.bindOptionalTextureI("colorTex1", SimplyShaders.buffer.attachment1.getID(),texNum);
        this.bindOptionalTexture("noiseTex", ChunkShader.noiseTex, texNum);
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
