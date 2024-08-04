package com.shfloop.simply_shaders.mixins;

import com.badlogic.gdx.graphics.Camera;
import com.shfloop.simply_shaders.Shadows;
import com.llamalad7.mixinextras.sugar.Local;
import finalforeach.cosmicreach.rendering.entities.EntityModelInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityModelInstance.class)
public class EntityModelInstanceMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/rendering/shaders/GameShader;bind(Lcom/badlogic/gdx/graphics/Camera;)V", shift = At.Shift.AFTER))
    private void injectEntityShadowPassRender(CallbackInfo ci,@Local Camera worldCamera) {

        //definitly need a better way to do this
        if (Shadows.shadowPass) {
            GameShaderInterface.getShader().get(6).bind(worldCamera);
        }

    }

}
