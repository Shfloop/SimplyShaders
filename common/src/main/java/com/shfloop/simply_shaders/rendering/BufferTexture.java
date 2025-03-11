package com.shfloop.simply_shaders.rendering;

import com.badlogic.gdx.graphics.Color;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;

import java.nio.ByteBuffer;

public class BufferTexture {
    private int id;
    private int width;
    private int height;
    private String uniformName;
    private int pixelFormat;
    private int internalFormat;
    public boolean clearTexture = true;


    private int attachmentNum;
    public Color clearColor = Color.CLEAR.cpy();
    public boolean isMipMapEnabled;

    public BufferTexture(String uniformName,int width, int height, int pixelFormat, int internalFormat, int attachmentNum, boolean isMipMapEnabled)  {
        this.id = -1;
        this.uniformName = uniformName;
        this.width = width;
        this.height = height;
        this.pixelFormat = pixelFormat;
        this.internalFormat = internalFormat;

        this.attachmentNum = attachmentNum;
        this.isMipMapEnabled = isMipMapEnabled;
         // could do the stuff in renderFbo here

//        GL20.glBindTexture(GL20.GL_TEXTURE_2D, this.id);
//        GL20.glTexImage2D(GL20.GL_TEXTURE_2D,0, GL32.GL_RGBA16F,this.width,this.height,0,pixel_format,GL20.GL_FLOAT, (ByteBuffer) null);
//        GL20.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MIN_FILTER, GL20.GL_LINEAR);
//        GL20.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAG_FILTER, GL20.GL_LINEAR);
//        GL20.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_S, GL20.GL_CLAMP_TO_BORDER);
//        GL20.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_T, GL20.GL_CLAMP_TO_BORDER);
        //GL20.glBindTexture(GL20.GL_TEXTURE_2D, 0);
    }
    public void genTexture() throws Exception {
        this.id =  GL20.glGenTextures();
        if (this.id == -1) {
            throw new Exception("texture doesnt exits cant get id");
        }
        GL20.glBindTexture(GL20.GL_TEXTURE_2D, this.id); //GL32.GL_RGBA16F
        GL20.glTexImage2D(GL20.GL_TEXTURE_2D,0, internalFormat,this.width,this.height,0,pixelFormat,GL20.GL_FLOAT, (ByteBuffer) null);
        GL20.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MIN_FILTER, GL20.GL_LINEAR);
        GL20.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAG_FILTER, GL20.GL_LINEAR);
        GL20.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_S, GL20.GL_CLAMP_TO_EDGE);
        GL20.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_T, GL20.GL_CLAMP_TO_EDGE);
    }
    public int getWidth() {
        return width;

    }
    public int getHeight() {
        return height;
    }

    public void dispose() {
        if (this.id == -1) return; //i dont think this is necasary as glDeleteTextures probalby checks for no id but idk
        GL20.glDeleteTextures(this.id);
    }
    public int getID() {return id;}
    public String getName() {return uniformName;}
    public int getInternalFormat() {return this.internalFormat;}
    public int getPixelFormat() {return this.pixelFormat;}

    public int getAttachmentNum() {
        return attachmentNum;
    }

    public void setAttachmentNum(int attachmentNum) {
        this.attachmentNum = attachmentNum;
    }
    public String toString() {
        String newString = "name: " + uniformName + " Width: " + this.width + " Height: " + height + " attach: " + attachmentNum + " id: " + id;
        return newString;
    }
}
