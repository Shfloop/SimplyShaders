package com.shfloop.simply_shaders.mixins;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.llamalad7.mixinextras.sugar.Local;
import com.shfloop.simply_shaders.Shadows;
import finalforeach.cosmicreach.rendering.BatchedZoneRenderer;
import finalforeach.cosmicreach.rendering.ChunkBatch;
import org.lwjgl.opengl.GL45;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BatchedZoneRenderer.class)
public class BatchedZoneRendererMixin {
    @Shadow @Final private IntMap<Boolean> layerWritesToDepth;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/rendering/BatchedZoneRenderer;requestMeshes()V"))
    private void injectSwitchToFrontFace(CallbackInfo ci) {
        if(Shadows.shadowPass) {
            //Gdx.gl.glCullFace(GL20.GL_FRONT);
            //only like 30 fps drop in a test might have worse cases but im not sure how else to fix shadows
            Gdx.gl.glDisable(GL20.GL_CULL_FACE);
        }

    }
    @Unique
    private boolean copiedDepthTex = false;
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/badlogic/gdx/graphics/GL20;glDepthMask(Z)V",shift = At.Shift.AFTER))
    private void copyDepthTexAndEnableDepthMask(CallbackInfo ci, @Local(ordinal = 0) int layerNum) {

        if (!copiedDepthTex && !layerWritesToDepth.get(layerNum,true)) {
            copiedDepthTex = true;
//            GL45.glMemoryBarrier(GL45.GL_TEXTURE_FETCH_BARRIER_BIT);
//            //might not need this instead use
//            GL45.glCopyImageSubData();// but i need to bind teh second texture to a framebuffre i think;
        }
        Gdx.gl.glDepthMask(true);// i alwasy want transparent geometry to write to depth tex; i copy out the pre translucent depth to a seperate texture

    }
}
