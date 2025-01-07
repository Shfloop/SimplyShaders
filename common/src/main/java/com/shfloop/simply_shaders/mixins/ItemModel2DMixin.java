package com.shfloop.simply_shaders.mixins;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import finalforeach.cosmicreach.rendering.items.ItemModel;
import finalforeach.cosmicreach.rendering.items.ItemModel2D;
import org.lwjgl.opengl.GL20;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemModel2D.class)
public abstract class ItemModel2DMixin extends ItemModel {
    @Shadow
    protected static PerspectiveCamera heldItemCamera;

    @Shadow
    protected static Matrix4 tmpHeldMat4;

    /*

        Overwrites ItemModel2D render held Item
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
        float s = 1.5F;
        if (popUpTimer > 0.0F) {
            float pop = (float)Math.pow((double)(popUpTimer / maxPopUpTimer), 2.0);
            tmpHeldMat4.translate(0.0F, -1.0F * pop, 0.0F);
        }

        tmpHeldMat4.translate(1.5F, -0.5F, -2.6F);
        tmpHeldMat4.scl(s, s, s);
        tmpHeldMat4.rotate(Vector3.Y, -75.0F);
        tmpHeldMat4.translate(-0.25F, -0.25F, -0.25F);
        if (swingTimer > 0.0F) {
            float swing = swingTimer / maxSwingTimer;
            swing = 1.0F - (float)Math.pow((double)(swing - 0.5F), 2.0) / 0.25F;
            tmpHeldMat4.rotate(Vector3.Z, 90.0F * swing);
            float st = -swing;
            tmpHeldMat4.translate(st * 2.0F, st, 0.0F);
        }

      //  Gdx.gl.glDisable(2929); //old
        Gdx.gl.glDepthFunc(GL20.GL_ALWAYS); //new

        ((ItemModel2D)(Object)this).render(worldPosition, heldItemCamera, tmpHeldMat4);


       // Gdx.gl.glEnable(2929); //old
        Gdx.gl.glDepthFunc(GL20.GL_LESS); //new
    }

}
