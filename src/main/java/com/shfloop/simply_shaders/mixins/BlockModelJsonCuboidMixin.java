package com.shfloop.simply_shaders.mixins;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.shfloop.simply_shaders.BlockPropertiesIDLoader;
import com.shfloop.simply_shaders.TexBufferContainer;
import finalforeach.cosmicreach.rendering.blockmodels.BlockModelJsonCuboid;

import finalforeach.cosmicreach.rendering.shaders.ChunkShader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockModelJsonCuboid.class)
public abstract class BlockModelJsonCuboidMixin {
    @Inject(method = "initialize", at = @At("HEAD"))
    private void injectTest(CallbackInfo ci) {
        //System.out.println("face name " + Shadows.modelJsonTemp);

    }



    @Shadow
    protected static Vector3 tmpFaceNormal;
    @Shadow
    protected static Vector3 tmpVertPos;


    private static final TexBufferContainer myTmpFloats = new TexBufferContainer();
    private static final ObjectIntMap<TexBufferContainer> myFloatsToIdx = new ObjectIntMap<>();
//    public static float shaderBlockGroupId;

    @Overwrite
    private static int getFaceTexBufFloatsIdx(float u, float v, Vector3 vertNormal) {
        FloatArray floats = ChunkShader.faceTexBufFloats;
        myTmpFloats.floats[0] = u;
        myTmpFloats.floats[1] = v;
        myTmpFloats.floats[2] = vertNormal.x;
        myTmpFloats.floats[3] = vertNormal.y;
        myTmpFloats.floats[4] = vertNormal.z;
        Vector3 faceNormal = tmpFaceNormal;
        myTmpFloats.floats[5] = faceNormal.x;
        myTmpFloats.floats[6] = faceNormal.y;
        myTmpFloats.floats[7] = faceNormal.z;
        Vector3 vertPos = tmpVertPos;
        myTmpFloats.floats[8] = vertPos.x;
        myTmpFloats.floats[9] = vertPos.y;
        myTmpFloats.floats[10] = vertPos.z;
        myTmpFloats.floats[11] = BlockPropertiesIDLoader.shaderBlockGroupId;
        int fIdx = myFloatsToIdx.get(myTmpFloats, -1);
        if (fIdx == -1) {
            fIdx = floats.size / TexBufferContainer.NUM_FLOATS_PER_FACE_UVTEXBUFF;
            myFloatsToIdx.put(new TexBufferContainer(myTmpFloats), fIdx);
            floats.add(u);
            floats.add(v);
            floats.add(vertNormal.x);
            floats.add(vertNormal.y);
            floats.add(vertNormal.z);
            floats.add(faceNormal.x);
            floats.add(faceNormal.y);
            floats.add(faceNormal.z);
            floats.add(vertPos.x);
            floats.add(vertPos.y);
            floats.add(vertPos.z);
            floats.add(BlockPropertiesIDLoader.shaderBlockGroupId);
            //System.out.println(Shadows.shaderBlockGroupId);
        }

        return fIdx;
    }
}
