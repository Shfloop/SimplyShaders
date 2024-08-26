package com.shfloop.simply_shaders.mixins;

import com.badlogic.gdx.math.Vector3;
import com.llamalad7.mixinextras.sugar.Local;

import finalforeach.cosmicreach.rendering.blockmodels.BlockModelJsonCuboid;
import finalforeach.cosmicreach.rendering.blockmodels.BlockModelJsonCuboidFace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockModelJsonCuboid.class)
public abstract class BlockModelJsonCuboidMixin {
    @Shadow
    protected static Vector3 tmpNormal;

    private static Vector3 newNormal;

    @Inject(method = "initialize", at = @At( value = "INVOKE", target = "setNormal(FFF)V")) // i think this should only target the first one
    private void getNewNormalIdx(CallbackInfo ci, @Local BlockModelJsonCuboidFace f) {

        newNormal = switch (f.vertexIndexD) {
            case 4 -> new Vector3(-1,0,0); //negx
            case 3 -> new Vector3(1,0,0);
            case 1 -> new Vector3(0,-1,0); //neg y
            case 6 -> new Vector3(0,1,0);
            case 2 -> new Vector3(0,0,-1);//neg z
            case 5 -> new Vector3(0,0,1);
            default -> new Vector3(1,1,1); //shouldnt happen but still need it
        };
    }
    @Inject(method = "setNormal", at = @At("TAIL"))
    private static void overwriteNormal(CallbackInfo ci) {
        //needs shadow helper variable
        tmpNormal.set(newNormal);
    }
}
