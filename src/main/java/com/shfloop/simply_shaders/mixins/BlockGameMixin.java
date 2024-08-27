package com.shfloop.simply_shaders.mixins;

import com.badlogic.gdx.Gdx;
import com.shfloop.simply_shaders.Shadows;
import com.shfloop.simply_shaders.SimplyShaders;
import com.shfloop.simply_shaders.rendering.RenderFBO;
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
        //should inject before reload all shaders so i just need to copy the files over
        Shadows.reloadShaders();
    }
    @Inject(method = "render()V", at = @At("TAIL"))
    private void renderFboResizeCheck(CallbackInfo ci) {
        //so resize doesnt spam delete and create framebuffers / textures
        if (needsResize ) {
            timeSinceResize += Gdx.graphics.getDeltaTime();
            if (timeSinceResize > 0.1) { // this could probably be even lesss atleast on my pc resize was called about every 0.02
                needsResize = false;
                SimplyShaders.resize();
            }
        }
    }

    @Inject(method = "resize", at = @At("TAIL"))
    private void injectCaptureResize(CallbackInfo ci) {
       //resize gets called around every 30 ms when the window sizxe changes
        //set a variable to current resize time and in my framebuffer check current time and if time difference is greater than 40ms go ahead with the update

       timeSinceResize = 0;
       needsResize = true;


    }
}
