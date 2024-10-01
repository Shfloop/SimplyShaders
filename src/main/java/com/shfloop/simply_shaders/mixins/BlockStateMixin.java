package com.shfloop.simply_shaders.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.shfloop.simply_shaders.BlockPropertiesIDLoader;
import com.shfloop.simply_shaders.Shadows;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockState.class)
public class BlockStateMixin {
    @Inject(method = "initialize", at = @At("HEAD"))
    private void testInject(CallbackInfo ci, @Local Block block) {
        String blockID = block.getStringId();
        Integer temp = BlockPropertiesIDLoader.baseGeneratedBlockIDMap.getOrDefault(blockID, -1);
        if (temp == -1) {
            int generatedBlockID = BlockPropertiesIDLoader.baseGeneratedBlockID++;
            if (generatedBlockID > 32766) {
                throw new RuntimeException("TOO MANY BASE BLOCK IDS");
            }
            BlockPropertiesIDLoader.baseGeneratedBlockIDMap.put(blockID, generatedBlockID);
            BlockPropertiesIDLoader.baseGeneratedBlockIDArray.add(blockID);
            BlockPropertiesIDLoader.shaderBlockGroupId = (float) (generatedBlockID << 8);
            //BlockPropertiesIDLoader.shaderBlockGroupId = Float.intBitsToFloat(generatedBlockID << 16);
        } else {
            BlockPropertiesIDLoader.shaderBlockGroupId = (float) (temp << 8);
        }
    }
}
