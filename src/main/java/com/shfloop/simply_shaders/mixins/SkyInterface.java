package com.shfloop.simply_shaders.mixins;

import com.badlogic.gdx.utils.ObjectMap;
import finalforeach.cosmicreach.world.Sky;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Sky.class)
public interface SkyInterface {
    @Accessor("skies")
    static ObjectMap<String, Sky> getSkies() {throw new AssertionError();}
}
