package com.shfloop.simply_shaders.mixins;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.shfloop.simply_shaders.Shadows;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.rendering.items.ItemModel;
import finalforeach.cosmicreach.rendering.items.ItemModelBlock;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import org.lwjgl.opengl.GL20;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemModelBlock.class)
public abstract class  ItemModelBlockMixin extends ItemModel {

    @Shadow
    protected static  PerspectiveCamera heldItemCamera;

    @Shadow
    protected static Matrix4 tmpHeldMat4;

    //not sure why this doesnt work but
//    @Redirect(method = "<init>", at = @At(value = "INVOKE_ASSIGN",
//            target = "Lfinalforeach/cosmicreach/rendering/shaders/GameShader;getShaderForBlockState(Lfinalforeach/cosmicreach/blocks/BlockState;)Lfinalforeach/cosmicreach/rendering/shaders/GameShader;"))
//    private static GameShader redirectBlockEntityShader(BlockState b) {
//        return Shadows.BLOCK_ENTITY_SHADER;
//    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void replaceBlockEntityShader(CallbackInfo ci) {
        ((ItemModelBlockInterface)(Object)this).setShader(Shadows.BLOCK_ENTITY_SHADER);
    }
    /*

    Overwrites ItemModelBlock render held Item
    Im doing this because i need the held item to render to depth buffer and vanilla disables depth test instead of setting depth to GL_ALWAYS

     */
    @Overwrite
    public void renderAsHeldItem(Vector3 worldPosition, Camera worldCamera, float popUpTimer, float maxPopUpTimer, float swingTimer, float maxSwingTimer) {
        heldItemCamera.fieldOfView = 50.0F;
        heldItemCamera.viewportHeight = worldCamera.viewportHeight;
        heldItemCamera.viewportWidth = worldCamera.viewportWidth;
        heldItemCamera.near = worldCamera.near;
        heldItemCamera.far = worldCamera.far;
        heldItemCamera.update();
        tmpHeldMat4.idt();
        if (popUpTimer > 0.0F) {
            float pop = (float)Math.pow((double)(popUpTimer / maxPopUpTimer), 2.0);
            tmpHeldMat4.translate(0.0F, -1.0F * pop, 0.0F);
        }

        tmpHeldMat4.translate(1.65F, -1.25F, -2.5F);
        tmpHeldMat4.rotate(Vector3.Y, -75.0F);
        tmpHeldMat4.translate(-0.25F, -0.25F, -0.25F);
        if (swingTimer > 0.0F) {
            float swing = swingTimer / maxSwingTimer;
            swing = 1.0F - (float)Math.pow((double)(swing - 0.5F), 2.0) / 0.25F;
            tmpHeldMat4.rotate(Vector3.Z, 90.0F * swing);
            float st = -swing;
            tmpHeldMat4.translate(st * 2.0F, st, 0.0F);
        }

       // Gdx.gl.glDisable(2929); //old code
        Gdx.gl.glDepthFunc(GL20.GL_ALWAYS); //new

        this.render(worldPosition, heldItemCamera, tmpHeldMat4, true);
        //Gdx.gl.glEnable(2929);

        Gdx.gl.glDepthFunc(GL20.GL_LESS); //new
    }
}
