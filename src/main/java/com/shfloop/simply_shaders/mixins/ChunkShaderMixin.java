package com.shfloop.simply_shaders.mixins;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.math.Matrix4;
import com.shfloop.simply_shaders.Shadows;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.rendering.shaders.ChunkShader;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import org.lwjgl.opengl.GL20;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//FIXME the mixins dont initalize fast enough to actually change the chunk shader

@Mixin(ChunkShader.class)
public abstract class ChunkShaderMixin extends GameShader {
    public ChunkShaderMixin(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
    }
    //TODO i should add an entity shader mixin to add normals to entities

    //Not needed in .1.44

//    @Inject(method = "<init>(Ljava/lang/String;Ljava/lang/String;)V", at = @At("TAIL")) //, Shadows.normal_attrib
//    private void injectConstructor(CallbackInfo ci) { // changed to new way 1.4
//        VertexAttribute[] vertex = new VertexAttribute[]{Shadows.posAttrib, Shadows.lightingAttrib,  Shadows.uvIdxAttrib, Shadows.normal_attrib }; // only works if nobody else updates vertex Attributes might want to change
//        this.allVertexAttributesObj = new VertexAttributes(vertex);
//
//
//
//    }
    //Im not sure why this doesnt work but i whateves inject works but it crashes cause vertex buffer is too small
//    @Inject(method = "<init>(Ljava/lang/String;Ljava/lang/String;)V", at = @At(value ="INVOKE",
//            target ="Lcom/badlogic/gdx/graphics/VertexAttributes;<init>([Lcom/badlogic/gdx/graphics/VertexAttribute;)V", shift = At.Shift.AFTER))
//    private void testInject(CallbackInfo ci) {
//        VertexAttribute[] vertex = new VertexAttribute[]{Shadows.posAttrib, Shadows.lightingAttrib,  Shadows.uvIdxAttrib, Shadows.normal_attrib }; // only works if nobody else updates vertex Attributes might want to change
//        this.allVertexAttributesObj = new VertexAttributes(vertex);
//
//
//    }


    @Inject(method = "bind(Lcom/badlogic/gdx/graphics/Camera;)V", at = @At("TAIL"))//value = "INVOKE", target = "Lfinalforeach/cosmicreach/rendering/shaders/GameShader;bindOptionalTextureBuffer(Ljava/lang/String;,  Lfinalforeach/cosmicreach/rendering/TextureBuffer; I)V")) // Lfinalforeach/cosmicreach/rendering/shaders/GameShader;bindOptionalTextureBuffer(Ljava/lang/String;,  Lfinalforeach/cosmicreach/rendering/TextureBuffer; I)V
    private void injectShaderParam(CallbackInfo ci ) {
        if (Shadows.shaders_on && InGame.world != null) { //should find a better way to do this

            this.bindOptionalUniformMatrix("lightSpaceMatrix", Shadows.getCamera().combined);
            this.bindOptionalUniformi("shadowMap", Shadows.shadow_map.getDepthMapTexture().id); // i think i should try and change this so it matches how texture numbers are handled in chunk shader but idk
            //this.bindOptionalUniform3f("lightPos", Shadows.getCamera().position); // no longer used
            this.bindOptionalUniform3f("lightDir", Shadows.getCamera().direction);// to compare with normal

        }
    }
    private void bindOptionalUniformMatrix(String uniform_name, Matrix4 mat) {
        int u = this.shader.getUniformLocation(uniform_name);
        if (u != -1) {
            this.shader.setUniformMatrix(uniform_name,mat);

        }
    }

//may want to move these out of the mixin but i have no idea if its a bad thing
    private void bindOptionalUniformi(String uniform_name, int id) {
        int u = this.shader.getUniformLocation(uniform_name);
        if (u != -1) {
            Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0 + 3);//just for right now this works i thinki
            Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, Shadows.shadow_map.getDepthMapTexture().id);
            this.shader.setUniformi(u, 3);


        }
    }
}
