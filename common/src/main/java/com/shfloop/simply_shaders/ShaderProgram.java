package com.shfloop.simply_shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

public class ShaderProgram extends com.badlogic.gdx.graphics.glutils.ShaderProgram {

    public ShaderProgram(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
    }
    @Override
    public void setVertexAttribute(String name, int size, int type, boolean normalize, int stride, int offset) {
        int location = this.getAttributeLocation(name);
        if (location != -1) {
            this.setVertexAttribute(location, size, type, normalize, stride, offset);
        }
    }

    @Override
    public void setVertexAttribute(int location, int size, int type, boolean normalize, int stride, int offset) {
        GL20 gl = Gdx.gl20;
        if (type == 5124 && Gdx.gl30 == null) {
            type = 5126;
        }

        if (type == 5124 && Gdx.gl30 != null) {
            Gdx.gl30.glVertexAttribIPointer(location, size, type, stride, offset);
        } else {
            gl.glVertexAttribPointer(location, size, type, normalize, stride, offset);
        }

    }
}
