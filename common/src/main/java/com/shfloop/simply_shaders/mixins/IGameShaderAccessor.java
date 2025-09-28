package com.shfloop.simply_shaders.mixins;

import finalforeach.cosmicreach.rendering.shaders.IGameShader;
import finalforeach.cosmicreach.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.HashMap;

@Mixin(IGameShader.class)
public interface IGameShaderAccessor {

    @Accessor("ALL_SHADERS")

    static HashMap<Identifier, HashMap<Identifier, IGameShader>> getAllShaders() {
        throw new AssertionError();
    }
}
