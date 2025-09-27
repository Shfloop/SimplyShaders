package com.shfloop.simply_shaders;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;

import java.nio.ByteBuffer;

public class ShadowTexture  { //look into using GLTexture
    public int id;
    public int width;
    public int height;

    public ShadowTexture(int width, int height, int pixel_format) throws Exception {

        this.id =  GL20.glGenTextures();//ChunkShader.DEFAULT_BLOCK_SHADER.getUniformLocation("shadowMap");
        if (this.id == -1) {
            throw new Exception("Shadow map doesnt exits cant get id");
        }
        // System.out.println("SHADOWMAP LOCATION " + this.id);
        this.width = width;
        this.height = height;


        GL20.glBindTexture(GL20.GL_TEXTURE_2D, this.id);
        GL20.glTexImage2D(GL20.GL_TEXTURE_2D,0, GL20.GL_DEPTH_COMPONENT24,this.width,this.height,0,pixel_format, GL32.GL_FLOAT, (ByteBuffer) null);
        GL20.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MIN_FILTER, GL20.GL_NEAREST);
        GL20.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAG_FILTER, GL20.GL_NEAREST);
        GL20.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_S, GL20.GL_CLAMP_TO_BORDER);
        GL20.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_T, GL20.GL_CLAMP_TO_BORDER);
        float[] border_color = {1.0f,1.0f,1.0f,1.0f};
        GL20.glTexParameterfv(GL20.GL_TEXTURE_2D, GL11.GL_TEXTURE_BORDER_COLOR,border_color);

    }
    public int getWidth() {
        return width;

    }
    public int getHeight() {
        return height;
    }

    public void cleanup() {
        GL20.glDeleteTextures(this.id);
    }
}
