package com.shfloop.simply_shaders;

import com.badlogic.gdx.utils.IntArray;

public interface GameShaderInterface {

     int[] getShaderInputBuffers();
     void setShaderInputBuffers(int[] arr);
     int[] getShaderDrawBuffers();
     IntArray getShaderMipMapEnabled();


}
