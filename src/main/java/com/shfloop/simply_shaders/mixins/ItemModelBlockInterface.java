package com.shfloop.simply_shaders.mixins;

import finalforeach.cosmicreach.rendering.items.ItemModelBlock;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemModelBlock.class)
public   interface ItemModelBlockInterface {
    @Accessor("shader")
    public void setShader(GameShader shader);
}
