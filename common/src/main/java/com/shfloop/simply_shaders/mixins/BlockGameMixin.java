package com.shfloop.simply_shaders.mixins;

import com.badlogic.gdx.Gdx;
import com.shfloop.simply_shaders.Shadows;
import com.shfloop.simply_shaders.SimplyShaders;
import finalforeach.cosmicreach.BlockGame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockGame.class)

public abstract class BlockGameMixin {
    private static float timeSinceResize;
    private static boolean needsResize;

    @Inject(method = "render()V", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/rendering/shaders/ChunkShader;reloadAllShaders()V"))
    private void injectBlockGameDepugShader(CallbackInfo ci) {
        Shadows.reloadShaders();
    }
    @Inject(method = "render()V", at = @At("TAIL"))
    private void renderFboResizeCheck(CallbackInfo ci) {
        //so resize doesnt spam delete and create framebuffers / textures
        if (needsResize ) {
            timeSinceResize += Gdx.graphics.getDeltaTime();
            if (timeSinceResize > 0.1) { // this could probably be even lesss atleast on my pc resize was called about every 0.02
                if (BlockGame.isFocused) {
                    needsResize = false;
                    if (Gdx.graphics.getWidth() == 0 && Gdx.graphics.getHeight() == 0) {
                        return;
                    }
                    if (SimplyShaders.holder != null) {
                        if (Gdx.graphics.getHeight() == SimplyShaders.holder.getHeight() &&  Gdx.graphics.getWidth() ==SimplyShaders.holder.getWidth()) {
                            //return if the graphics didnt change
                            return;
                        }
                    }
                    SimplyShaders.resize();
                } else {

                    //instead just set needs resized to false cause it should call resize once the window regains focus
                    needsResize = false;
                }
            }
        }
    }

    @Inject(method = "resize", at = @At("TAIL"))
    private void injectCaptureResize(CallbackInfo ci) {
       //resize gets called around every 30 ms when the window sizxe changes
       timeSinceResize = 0;
       needsResize = true;


    }
}
