package com.shfloop.simply_shaders.mixins;

import finalforeach.cosmicreach.rendering.shaders.ChunkShader;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.rendering.shaders.SkyStarShader;
import finalforeach.cosmicreach.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyStarShader.class)
public  abstract class StarShaderMixin extends GameShader {

    public StarShaderMixin(Identifier vertexShader, Identifier fragmentShader) {
        super(vertexShader, fragmentShader);
    }

    @Inject(method = "bind", at = @At("TAIL"))
    private void addUniformsToBind(CallbackInfo ci) {
        int texNum = 0;
        texNum = this.bindOptionalTexture("noiseTex", ChunkShader.noiseTex, texNum);
    }
}
