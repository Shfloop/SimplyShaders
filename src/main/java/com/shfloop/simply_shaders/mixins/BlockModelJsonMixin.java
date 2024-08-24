package com.shfloop.simply_shaders.mixins;



import com.badlogic.gdx.utils.FloatArray;
import com.shfloop.simply_shaders.Shadows;
import com.llamalad7.mixinextras.sugar.Local;
import finalforeach.cosmicreach.RuntimeInfo;
import finalforeach.cosmicreach.rendering.blockmodels.BlockModelJson;
import finalforeach.cosmicreach.rendering.blockmodels.BlockModelJsonCuboidFace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//got rid of mixin include in json for .1.44

@Mixin(BlockModelJson.class)
public abstract class BlockModelJsonMixin {
    @Inject(method = "addVertices(Lfinalforeach/cosmicreach/rendering/IMeshData;IIII[S[I)V", at = @At(
            value = "INVOKE", target = "addVert(Lfinalforeach/cosmicreach/rendering/IMeshData;FFFFFISII)I")) // hopefully this finds the right spot

    private void addVerticiesInject(CallbackInfo ci, @Local BlockModelJsonCuboidFace f) { // just needs to update the normal helper class normal value based on the local fi
        Shadows.normal_float =switch (f.vertexIndexD) { // there might be a better way to do this
            //TODO look into this it might be causing issues
            case 1 -> 2; // i can replace this with just an updated vertex shader vec3 normal array
            case 2 -> 4;
            case 3 -> 1;
            case 4 -> 0; // whats not seen by c gets case 4 i think
            case 5 -> 5;
            case 6 -> 3;
            default -> 7;

        };



    }

    @Inject(method = "addVert(Lfinalforeach/cosmicreach/rendering/IMeshData;FFFFFISII)I", at = @At("RETURN"), cancellable = true)
    private void addNormalVertInject(CallbackInfoReturnable<Integer> cir, @Local(ordinal = 3) int size, @Local FloatArray verts) {
        int numComponents = 6;


        float[] items = verts.items;
        if (RuntimeInfo.isMac) { // mac wont work yet
            items[size + 6] =  Shadows.normal_float;// ; // i need to mess with game shader to update the changes vertex attribute position when isMac
            numComponents += 1;
        } else {
            items[size + 5] =  Shadows.normal_float ;//;
        }
        verts.size += 1;
        int indexOfCurVertex = size / numComponents;

        cir.setReturnValue(indexOfCurVertex); //i think this is right now
    }
}
