package com.shfloop.simply_shaders.mixins;

import com.shfloop.simply_shaders.SimplyShaders;
import finalforeach.cosmicreach.gamestates.InGame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGame.class)
public class InGameMixinPuzzle {
    @Inject(method = "loadWorld(Lfinalforeach/cosmicreach/world/World;)V", at =@At("TAIL"))
    private void injectloadWorld(CallbackInfo ci) {

        if (SimplyShaders.screenQuad == null) {
            SimplyShaders.genMesh();
        }

//        if (SimplyShaders.buffer!= null) { //move this shit to loadWorld
//            SimplyShaders.buffer.dispose(); // it should already be disposed but just to be sure
//        }
//        try {
//            SimplyShaders.buffer = new RenderFBO(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
//            //RenderFBO.bindRenderTextures();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
        SimplyShaders.initTextureHolder();
    }
}
