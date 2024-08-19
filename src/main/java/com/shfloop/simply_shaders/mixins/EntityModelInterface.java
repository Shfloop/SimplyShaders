package com.shfloop.simply_shaders.mixins;

import com.badlogic.gdx.graphics.Texture;
import finalforeach.cosmicreach.rendering.entities.EntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityModel.class)
public interface EntityModelInterface {
    @Accessor
    Texture getDiffuseTexture();
}
