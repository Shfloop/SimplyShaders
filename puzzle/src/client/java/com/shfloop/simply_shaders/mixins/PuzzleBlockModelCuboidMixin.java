package com.shfloop.simply_shaders.mixins;

import com.badlogic.gdx.math.Vector3;

import com.llamalad7.mixinextras.sugar.Local;

import com.shfloop.simply_shaders.pack_loading.BlockPropertiesIDLoader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


public class PuzzleBlockModelCuboidMixin {


//    @Inject(method = "initialize", at = @At("HEAD"))
//    private void injectTest(CallbackInfo ci, @Local(argsOnly = true) PuzzleBlockModel blockModel) {
//        String model = blockModel.modelName;
//        //Constants.LOGGER.info("CUBOID MODEL NAME " + model);
//
//
//        int jsonIdx = model.lastIndexOf('.');
//        if (jsonIdx != -1) {
//            int model_ = model.indexOf("model_");
//            if (model_ != -1) {
//                String blockID = model.substring(model_+ 6,jsonIdx); //DOESNT WORK WITH MODDED BLOCKS
//                //String blockID = model.substring(0 ,tempidx);
//               // Constants.LOGGER.info("FOUND "  + blockID);
//                Integer temp = BlockPropertiesIDLoader.baseGeneratedBlockIDMap.getOrDefault(blockID, -1);
//            if (temp == -1) {
//                int generatedBlockID = BlockPropertiesIDLoader.baseGeneratedBlockID++;
//                if (generatedBlockID > 32766) {
//                    throw new RuntimeException("TOO MANY BASE BLOCK IDS");
//                }
//                //Constants.LOGGER.info(blockID + " " + generatedBlockID);
//                BlockPropertiesIDLoader.baseGeneratedBlockIDMap.put(blockID, generatedBlockID);
//                BlockPropertiesIDLoader.baseGeneratedBlockIDArray.add(blockID);
//                BlockPropertiesIDLoader.shaderBlockGroupId = (float) (generatedBlockID << 8);
//                //BlockPropertiesIDLoader.shaderBlockGroupId = Float.intBitsToFloat(generatedBlockID << 16);
//            } else {
//                BlockPropertiesIDLoader.shaderBlockGroupId = (float) (temp << 8);
//            }
//            }
//        }
//        //Constants.LOGGER.info(temp + " " + model);
//    }

//    @Inject(method = "initialize", at = @At(value = "INVOKE", target = "Lcom/badlogic/gdx/math/Vector3;nor()Lcom/badlogic/gdx/math/Vector3;", shift = At.Shift.AFTER))
//    private void fixNormalsYetAgain(CallbackInfo ci, @Local(ordinal = 1) Vector3 tmpFaceNormalRef, @Local PuzzleBlockModelCuboid.Face f ) {
//        Vector3 p1 = new Vector3(f.x1, f.y1, f.z1);
//        Vector3 p2 = new Vector3(f.x2, f.y2, f.z2);
//        Vector3 p3 = new Vector3(f.midX1, f.midY1, f.midZ1);
//        Vector3 tmpFaceNormal = new Vector3();
//        Vector3 u = p2.sub(p1);
//        Vector3 v = p3.sub(p1);
//
//        tmpFaceNormal.x = -1 *(u.y * v.z - u.z * v.y);
//        tmpFaceNormal.y = -1 *(u.z * v.x - u.x * v.z);
//        tmpFaceNormal.z = -1 *(u.x * v.y - u.y * v.x);
//        tmpFaceNormal.nor();
//        tmpFaceNormalRef.set(tmpFaceNormal);
//    }


}

