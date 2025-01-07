package com.shfloop.simply_shaders.mixins;

import finalforeach.cosmicreach.rendering.items.ItemThingModel;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(ItemThingModel.class)
public abstract interface ItemThingModelInterface {



        @Accessor("program")
        public void setProgram(GameShader program);


}
