package com.shfloop.simply_shaders.mixins;

import com.shfloop.simply_shaders.DynamicSkyInterface;
import finalforeach.cosmicreach.chat.commands.CommandTime;
import finalforeach.cosmicreach.world.Sky;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandTime.class)
public class CommandTimeMixin  {
    @Inject(method = "run", at = @At("TAIL"))
    private void updateDynamicSkySunUpdate(CallbackInfo ci) {
        if(Sky.currentSky.skyId.equals("base:dynamic_sky")) {
            ((DynamicSkyInterface) Sky.currentSky).setLastUpdateTime(0);
        }

    }


}
