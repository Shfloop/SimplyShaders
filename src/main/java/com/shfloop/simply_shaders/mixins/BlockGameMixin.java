package com.shfloop.simply_shaders.mixins;

import com.shfloop.simply_shaders.Shadows;
import finalforeach.cosmicreach.BlockGame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockGame.class)

public class BlockGameMixin {

    @Inject(method = "render()V", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/rendering/shaders/ChunkShader;reloadAllShaders()V"))
    private void injectBlockGameDepugShader(CallbackInfo ci) {
        //should inject before reload all shaders so i just need to copy the files over
        Shadows.reloadShaders();
    }
}
