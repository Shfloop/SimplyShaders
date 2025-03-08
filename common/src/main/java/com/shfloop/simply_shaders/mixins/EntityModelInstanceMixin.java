package com.shfloop.simply_shaders.mixins;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.shfloop.simply_shaders.Shadows;
import com.llamalad7.mixinextras.sugar.Local;
import finalforeach.cosmicreach.rendering.entities.EntityModel;
import finalforeach.cosmicreach.rendering.entities.instances.EntityModelInstance;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityModelInstance.class)
public abstract class  EntityModelInstanceMixin {

    @Shadow
    protected Color tintColor;
    @Shadow
   protected EntityModel entityModel;
    @Inject(method = "render", at = @At(value = "INVOKE", target =
            "Lcom/badlogic/gdx/graphics/Mesh;render(Lcom/badlogic/gdx/graphics/glutils/ShaderProgram;I)V"))
    private void newEntityRenderInject(CallbackInfo ci, @Local Camera worldCamera) {
        if (Shadows.shadowPass) {
            GameShader temp = Shadows.SHADOW_ENTITY;
            temp.bind(worldCamera);
            //Todo for now just bind the textures even though they arent really needed
            temp.bindOptionalTexture("texDiffuse",((EntityModelInstance)(Object) (this)).getDiffuse(), 0);
            temp.bindOptionalUniform4f("tintColor", tintColor);
        }
    }
}