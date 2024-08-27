package com.shfloop.simply_shaders.mixins;

import com.shfloop.simply_shaders.rendering.RenderFBO;
import finalforeach.cosmicreach.rendering.shaders.SkyShader;
import org.lwjgl.opengl.GL32;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(SkyShader.class)
public class SkyShaderMixin {
    @Inject(method = "bind", at = @At("TAIL"))
    private void addDrawBuffersSky(CallbackInfo ci) {
        int [] drawBuffers = {GL32.GL_COLOR_ATTACHMENT0,GL32.GL_COLOR_ATTACHMENT2};

        if (!Arrays.equals(RenderFBO.lastDrawBuffers , drawBuffers)) {
            GL32.glDrawBuffers(drawBuffers);
            RenderFBO.lastDrawBuffers = drawBuffers;
           // System.out.println("SKYSHADERBUFFER");

        }
    }
}
