package com.shfloop.simply_shaders.mixins;

import com.badlogic.gdx.graphics.Camera;
import com.shfloop.simply_shaders.Shadows;
import com.llamalad7.mixinextras.sugar.Local;
import finalforeach.cosmicreach.rendering.ChunkBatch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static finalforeach.cosmicreach.rendering.ChunkBatch.lastBoundShader;

@Mixin(ChunkBatch.class)
public class ChunkBatchMixin {
    @Inject(method = "render(Lfinalforeach/cosmicreach/world/Zone;Lcom/badlogic/gdx/graphics/Camera;)V", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/rendering/meshes/IGameMesh;bind(Lcom/badlogic/gdx/graphics/glutils/ShaderProgram;)V", shift = At.Shift.BEFORE))
    private void shadowInject(CallbackInfo ci, @Local Camera worldCamera) { // just gonna have a exit early for shadow chunk not sure how i can easily implement a shadow pass vert and frag shader to work with transparent blocks
        //loses about 50fps
        if (Shadows.shadowPass) {
            if (lastBoundShader != GameShaderInterface.getShader().get(6)) { //SUPER UGLY BUT IDK
                lastBoundShader = GameShaderInterface.getShader().get(6);
                //Todo maybe make sure these get added to the array last and just dynamically get the position so future shaders added bu the game dont break the mod

                lastBoundShader.bind(worldCamera); // who knowws if this is going to work cause i sure dont // i could just make this perminent sun_camera
                //a little stupid cause bind a diferent shader before and this will overwrite it but it works without overwriting the method


                //getting rid of normal translation in shadowpass vertex shader stops the glass from being transparent

            }
        }
    }
}
