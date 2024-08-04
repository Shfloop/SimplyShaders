package com.shfloop.simply_shaders.mixins;

import com.badlogic.gdx.utils.Array;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameShader.class)
public interface GameShaderInterface {

// Used to clean up the shader array in GameShader without it spamming shaders on and off will keep the GameShader object alive
    @Accessor("allShaders")

    public static Array<GameShader> getShader() {
        throw new AssertionError();
    }


}
