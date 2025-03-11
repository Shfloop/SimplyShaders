package com.shfloop.simply_shaders;

import com.badlogic.gdx.utils.IntArray;
import com.shfloop.simply_shaders.pack_loading.ShaderDirectives;

public interface GameShaderInterface {

     int[] getShaderInputBuffers();
     void setShaderInputBuffers(int[] arr);
     int[] getShaderDrawBuffers();
     void setShaderDrawBuffers(int[] arr);
     IntArray getShaderMipMapEnabled();

     ShaderDirectives getShaderDirectives();


}
