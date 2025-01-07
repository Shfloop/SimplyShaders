package com.shfloop.simply_shaders.rendering;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;

import java.nio.ByteBuffer;

public class BufferTexture {
    private int id;
    private int width;
    private int height;
    private String uniformName;
    public BufferTexture(String uniformName,int width, int height, int pixel_format) throws Exception {
        this.id =  GL20.glGenTextures();
        if (this.id == -1) {
            throw new Exception("Shadow map doesnt exits cant get id");
        }
        this.uniformName = uniformName;
        this.width = width;
        this.height = height;


        GL20.glBindTexture(GL20.GL_TEXTURE_2D, this.id);
        GL20.glTexImage2D(GL20.GL_TEXTURE_2D,0, GL32.GL_RGBA16F,this.width,this.height,0,pixel_format,GL20.GL_FLOAT, (ByteBuffer) null);
        GL20.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MIN_FILTER, GL20.GL_LINEAR);
        GL20.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAG_FILTER, GL20.GL_LINEAR);
        GL20.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_S, GL20.GL_CLAMP_TO_BORDER);
        GL20.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_T, GL20.GL_CLAMP_TO_BORDER);
        //GL20.glBindTexture(GL20.GL_TEXTURE_2D, 0);
    }
    public int getWidth() {
        return width;

    }
    public int getHeight() {
        return height;
    }

    public void dispose() {
        GL20.glDeleteTextures(this.id);
    }
    public int getID() {return id;}
    public String getName() {return uniformName;}
}
