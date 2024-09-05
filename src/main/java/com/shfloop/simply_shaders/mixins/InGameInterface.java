package com.shfloop.simply_shaders.mixins;

import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(InGame.class)
public  interface InGameInterface {
    @Accessor("localPlayer")
    public static Player getLocalPlayer() {throw new AssertionError();}
    @Accessor("world")
    public static World getWorld() {throw new AssertionError();}
}
