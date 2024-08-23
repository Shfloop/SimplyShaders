package com.shfloop.simply_shaders.mixins;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix4;
import com.shfloop.simply_shaders.Shadows;
import com.shfloop.simply_shaders.rendering.FinalShader;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.rendering.shaders.ChunkShader;
import finalforeach.cosmicreach.rendering.shaders.EntityShader;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import org.lwjgl.opengl.GL20;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameShader.class)
public abstract class GameShaderMixin   {


    @Inject(method = "initShaders()V", at = @At("HEAD")) // making it head should populate the mods assets with the replacement shaders
    static private void addShadowShaders(CallbackInfo ci) throws Exception {
        Shadows.initShadowShaders();//need these to be called first so it can create files if the need arisees

    }
    @Inject(method = "initShaders()V", at = @At("Tail")) // making it head should populate the mods assets with the replacement shaders
    static private void addShadowPassShaders(CallbackInfo ci) {
        //Instead i should just make a shader pack and have this be in it
        new ChunkShader("InternalShader/internal.shadowpass.vert.glsl","InternalShader/internal.shadowpass.frag.glsl");
        new EntityShader("InternalShader/internal.shadowEntity.vert.glsl","InternalShader/internal.shadowEntity.frag.glsl");
        ChunkShader.DEFAULT_BLOCK_SHADER = new ChunkShader("InternalShader/internal.chunk.vert.glsl", "InternalShader/internal.chunk.frag.glsl");
        new FinalShader("InternalShader/internal.final.vert.glsl", "InternalShader/internal.final.frag.glsl");
    }



//    @Inject(method = "bind(Lcom/badlogic/gdx/graphics/Camera;)V", at = @At("TAIL"))//value = "INVOKE", target = "Lfinalforeach/cosmicreach/rendering/shaders/GameShader;bindOptionalTextureBuffer(Ljava/lang/String;,  Lfinalforeach/cosmicreach/rendering/TextureBuffer; I)V")) // Lfinalforeach/cosmicreach/rendering/shaders/GameShader;bindOptionalTextureBuffer(Ljava/lang/String;,  Lfinalforeach/cosmicreach/rendering/TextureBuffer; I)V
//    private void injectShaderParam(CallbackInfo ci ) {
//        if (Shadows.shaders_on && InGame.world != null) { //should find a better way to do this
//
//            this.bindOptionalUniformMatrix("lightSpaceMatrix", Shadows.getCamera().combined);
//            this.bindOptionalUniformi("shadowMap", Shadows.shadow_map.getDepthMapTexture().id); // i think i should try and change this so it matches how texture numbers are handled in chunk shader but idk
//            ((GameShader)(Object)this).bindOptionalUniform3f("lightPos", Shadows.getCamera().position); // think this is what i need
//            ((GameShader)(Object)this).bindOptionalUniform3f("lightDir", Shadows.getCamera().direction);// to compare with normal
//
//        }
//    }
//    private void bindOptionalUniformMatrix(String uniform_name, Matrix4 mat) {
//        int u = ((GameShader)(Object)this).shader.getUniformLocation(uniform_name);
//        if (u != -1) {
//            ((GameShader)(Object)this).shader.setUniformMatrix(uniform_name,mat);
//
//        }
//    }
//
//    //may want to move these out of the mixin but i have no idea if its a bad thing
//    private void bindOptionalUniformi(String uniform_name, int id) {
//        int u = ((GameShader)(Object)this).shader.getUniformLocation(uniform_name);
//        if (u != -1) {
//            Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0 + 3);//just for right now this works i thinki
//            Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, Shadows.shadow_map.getDepthMapTexture().id);
//            ((GameShader)(Object)this).shader.setUniformi(u, 3);
//
//
//        }
//    }



}
