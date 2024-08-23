package com.shfloop.simply_shaders.rendering;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.shfloop.simply_shaders.Shadows;
import com.shfloop.simply_shaders.SimplyShaders;
import finalforeach.cosmicreach.rendering.shaders.GameShader;

public class FinalShader extends GameShader {
    public FinalShader(String vertexShader, String fragmentShader) {
        super(vertexShader,fragmentShader);
        this.allVertexAttributesObj = new VertexAttributes(new VertexAttribute[]{VertexAttribute.Position(), VertexAttribute.TexCoords(0) });
    }
    public void bind(Camera worldCamera) {
        super.bind(worldCamera);
        int texId;
        if (Shadows.shaders_on) {
            texId = Shadows.shadow_map.getDepthMapTexture().id;
        } else {
            texId = SimplyShaders.buffer.attachment0.getID();
        }
        this.bindOptionalInt(SimplyShaders.buffer.attachment0.getName(),texId);
    }
}
