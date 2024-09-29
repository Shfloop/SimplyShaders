package com.shfloop.simply_shaders.mixins;

import com.llamalad7.mixinextras.sugar.Local;
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
        //System.out.println("HERE IT IS " + block.getStringId());
        String blockID = block.getStringId();
        Integer temp = Shadows.blockPropertiesIDMap.get(blockID);
        if (temp != null) {
            Shadows.shaderBlockGroupId = temp.floatValue();

        } else {
            Shadows.shaderBlockGroupId = 0.0f;
        }

    }
}
