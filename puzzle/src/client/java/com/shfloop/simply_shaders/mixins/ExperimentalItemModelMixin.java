//package com.shfloop.simply_shaders.mixins;
//
//import com.badlogic.gdx.Gdx;
//import com.badlogic.gdx.graphics.Camera;
//import com.badlogic.gdx.graphics.PerspectiveCamera;
//import com.badlogic.gdx.math.Matrix4;
//import com.badlogic.gdx.math.Vector3;
//import com.github.puzzle.game.engine.items.ExperimentalItemModel;
//import com.github.puzzle.game.engine.items.model.IPuzzleItemModel;
//import com.github.puzzle.game.items.IModItem;
//import finalforeach.cosmicreach.items.ItemStack;
//import finalforeach.cosmicreach.ui.UI;
//import org.lwjgl.opengl.GL20;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Overwrite;
//import org.spongepowered.asm.mixin.Shadow;
//
//@Mixin(ExperimentalItemModel.class)
//public abstract class ExperimentalItemModelMixin implements IPuzzleItemModel {
//    @Shadow
//    protected static PerspectiveCamera heldItemCamera;
//
//    @Shadow
//    IModItem item;
//
//
//    /*
//        Overwriting this until Pullrequest is made to change to GlDepthFunc GL_ALWAYS
//        need to still write to depth buffer for fog and other things it just makes sense to do so
//        changing glEnable/Disable to GLDepthFunc
//     */
//    @Overwrite
//    public void renderAsHeldItem(Vector3 pos, ItemStack stack2, Camera handCam, float popUpTimer, float maxPopUpTimer, float swingTimer, float maxSwingTimer) {
//        Matrix4 tmpHeldMat4 = new Matrix4();
//        ItemStack stack = UI.hotbar.getSelectedItemStack();
//
//        heldItemCamera.fieldOfView = 50.0F;
//        heldItemCamera.viewportHeight = handCam.viewportHeight;
//        heldItemCamera.viewportWidth = handCam.viewportWidth;
//        heldItemCamera.near = handCam.near;
//        heldItemCamera.far = handCam.far;
//        heldItemCamera.update();
//        tmpHeldMat4.idt();
//        float swing;
//        if (popUpTimer > 0.0F) {
//            swing = (float)Math.pow(popUpTimer / maxPopUpTimer, 2.0);
//            tmpHeldMat4.translate(0.0F, -1.0F * swing, 0.0F);
//        }
//
//        tmpHeldMat4.translate(1.65F, -1.25F, -2.5F);
//        tmpHeldMat4.rotate(Vector3.Y, -75.0F);
//        tmpHeldMat4.translate(-0.25F, -0.25F, -0.25F);
//        if (swingTimer > 0.0F) {
//            swing = swingTimer / maxSwingTimer;
//            swing = 1.0F - (float)Math.pow(swing - 0.5F, 2.0) / 0.25F;
//            tmpHeldMat4.rotate(Vector3.Z, 90.0F * swing);
//            float st = -swing;
//            tmpHeldMat4.translate(st * 2.0F, st, 0.0F);
//        }
//
//        if (item.isTool()) {
//            tmpHeldMat4.translate(.6f,0, 0);
//            tmpHeldMat4.translate(0,-.2f, 0);
//            tmpHeldMat4.rotate(new Vector3(0, 0, 1), 20);
//            tmpHeldMat4.rotate(new Vector3(1, 0, 0), 15);
//        }
//
//       // Gdx.gl.glDisable(2929); //old
//        Gdx.gl.glDepthFunc(GL20.GL_ALWAYS); //new
//
//        ((ExperimentalItemModel)(Object)this).renderGeneric(pos, stack, heldItemCamera, tmpHeldMat4, false);
//
//        //Gdx.gl.glEnable(2929); //old
//        Gdx.gl.glDepthFunc(GL20.GL_LESS); //new
//    }
//}
