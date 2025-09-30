package com.shfloop.simply_shaders.mixins;


import com.shfloop.simply_shaders.SimplyShaders;
import finalforeach.cosmicreach.gamestates.MainMenu;
import finalforeach.cosmicreach.lwjgl3.Lwjgl3Launcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import static com.shfloop.simply_shaders.SimplyShaders.LOGGER;

@Mixin(MainMenu.class)
public class MainMenuMixinPuzzle {
    @Inject(method = "create", at = @At("HEAD"))
    private void injected(CallbackInfo ci) {
        LOGGER.info("SimplyShaders PUzzle mixin logged!");
        SimplyShaders.initializeBuffer();
    }
}