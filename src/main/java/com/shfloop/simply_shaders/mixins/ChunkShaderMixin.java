package com.shfloop.simply_shaders.mixins;

import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.llamalad7.mixinextras.sugar.Local;
import com.shfloop.simply_shaders.Shadows;
import com.shfloop.simply_shaders.SimplyShaders;
import com.shfloop.simply_shaders.rendering.RenderFBO;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.rendering.shaders.ChunkShader;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;


@Mixin(ChunkShader.class)
public abstract class ChunkShaderMixin extends GameShader {
    private String shaderType;
    private Matrix4 prevCombinedMatrix = new Matrix4();

    //TODO i should add an entity shader mixin to add normals to entities




    @Inject(method = "bind(Lcom/badlogic/gdx/graphics/Camera;)V", at = @At("TAIL"))//value = "INVOKE", target = "Lfinalforeach/cosmicreach/rendering/shaders/GameShader;bindOptionalTextureBuffer(Ljava/lang/String;,  Lfinalforeach/cosmicreach/rendering/TextureBuffer; I)V")) // Lfinalforeach/cosmicreach/rendering/shaders/GameShader;bindOptionalTextureBuffer(Ljava/lang/String;,  Lfinalforeach/cosmicreach/rendering/TextureBuffer; I)V
    private void injectShaderParam(CallbackInfo ci ,@Local int texNum, @Local Camera worldCamera) {



        if (Shadows.shaders_on && InGame.getWorld() != null) { //should find a better way to do this
            int newTexNum = texNum;
            this.bindOptionalUniformMatrix("lightSpaceMatrix", Shadows.getCamera().combined);
            //FIXME This is redundant i only need to bind the shadowmap texture once when the shader is created because it doesnchange
           newTexNum = this.bindOptionalTextureI("shadowMap", Shadows.shadow_map.getDepthMapTexture().id, newTexNum); // i think i should try and change this so it matches how texture numbers are handled in chunk shader but idk
            //this.bindOptionalUniform3f("lightPos", Shadows.getCamera().position); // no longer used
            this.bindOptionalUniform3f("lightDir", Shadows.getCamera().direction);// to compare with normal
            this.bindOptionalUniformMatrix("u_projViewTransPrev", prevCombinedMatrix);
//            this.bindOptionalFloat("frameTimeCounter", (float) Gdx.graphics.getFrameId() );
//            this.bindOptionalFloat("viewWidth", Gdx.graphics.getWidth());
//            this.bindOptionalFloat("viewHeight", Gdx.graphics.getHeight());
            prevCombinedMatrix.set(worldCamera.combined);
        }
    }
    private void bindOptionalUniformMatrix(String uniform_name, Matrix4 mat) {
        int u = this.shader.getUniformLocation(uniform_name);
        if (u != -1) {
            this.shader.setUniformMatrix(uniform_name,mat);

        }
    }

//may want to move these out of the mixin but i have no idea if its a bad thing
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
