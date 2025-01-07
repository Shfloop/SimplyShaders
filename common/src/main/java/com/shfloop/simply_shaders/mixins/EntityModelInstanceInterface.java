package com.shfloop.simply_shaders.mixins;


import finalforeach.cosmicreach.rendering.entities.EntityModelInstance;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityModelInstance.class)
public interface EntityModelInstanceInterface {
    @Accessor("shader")
    public void setShader(GameShader shader);
}
