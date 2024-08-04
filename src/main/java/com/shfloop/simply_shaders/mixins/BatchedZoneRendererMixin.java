package com.shfloop.simply_shaders.mixins;

import finalforeach.cosmicreach.rendering.BatchedZoneRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BatchedZoneRenderer.class)
public abstract class BatchedZoneRendererMixin {
    @Inject(method = "render(Lfinalforeach/cosmicreach/world/Zone;Lcom/badlogic/gdx/graphics/Camera;)V", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/rendering/BatchedZoneRenderer;addMeshDatasToChunkBatches()V", shift = At.Shift.BY, by = 3))// max shift of 5 so just after null assignment then
    //i think shift needs to be 3 cause thers  two bytecode instructions  for ChunkBatch.lastBoundShader = null;
//doesnt work very well with cubes
    //shadows do not form on sharp edges when it should be in shadow
    private void injectShadowPassRender(CallbackInfo ci) {


//        if (Shadows.shadowPass) {
//            Gdx.gl.glCullFace(GL20.GL_FRONT); //try this
//            //then we want to bind the custome shadowvsh shaderprogram
//        }
    }

}
