package com.shfloop.simply_shaders.mixins;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.shfloop.simply_shaders.Shadows;
import com.llamalad7.mixinextras.sugar.Local;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.world.Zone;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(InGame.class)
public class InGameMixin extends GameState {
    @Shadow
    static protected PerspectiveCamera rawWorldCamera;

    //i think instead now that sprinting influences the fov i need to change from this "Lcom/badlogic/gdx/utils/viewport/Viewport;apply()V"
    //to before sky.drawsky
    @Inject(method = "create()V", at = @At("TAIL"))
    private void injectCreate(CallbackInfo ci) {
        if (Shadows.shaders_on && !Shadows.initalized) {
            try {
                Shadows.turnShadowsOn();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Inject(method = "dispose()V", at = @At("TAIL"))
    private void injectDispose(CallbackInfo ci) {
        try {
            Shadows.cleanup();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Inject(method = "render()V", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/world/Sky;drawSky(Lcom/badlogic/gdx/graphics/Camera;)V"))// Lfinalforeach/cosmicreach/world/Sky;drawStars(Lcom/badlogic/gdx/graphics/Camera)V
    private void injectInGameRender(CallbackInfo ci, @Local Zone playerZone) {
        //this is causing fps to drop by 1/3
        if (Shadows.shaders_on) {
            Shadows.lastUsedCameraPos = rawWorldCamera.position.cpy();
            Shadows.updateCenteredCamera();



            Gdx.gl.glBindFramebuffer(36160, Shadows.shadow_map.getDepthMapFbo());
            Gdx.gl.glViewport(0,0, Shadows.shadow_map.getDepthMapTexture().getWidth(), Shadows.shadow_map.getDepthMapTexture().getHeight());

            Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
            Shadows.shadowPass = true;
           // nned to improve framerate its getting cut by like 1/3 with default shaders
            GameSingletons.zoneRenderer.render(playerZone, Shadows.getCamera());

            Gdx.gl.glDepthMask(true);

            for (Entity e : playerZone.allEntities) {
                e.render(Shadows.getCamera()); //ENtity shaders during shadow pass also need to be distorted to apply correctly to shadow map
            }
            Shadows.shadowPass = false;



            Gdx.gl.glBindFramebuffer(36160, 0);
            Gdx.gl.glViewport(0,0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            Gdx.gl.glClear(org.lwjgl.opengl.GL20.GL_DEPTH_BUFFER_BIT | org.lwjgl.opengl.GL20.GL_COLOR_BUFFER_BIT); // might not need this
            //Shadows.redraw_stars = true;
        }
        }
}
