package com.shfloop.simply_shaders.mixins;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntArray;
import com.shfloop.simply_shaders.GameShaderInterface;
import com.shfloop.simply_shaders.pack_loading.BlockPropertiesIDLoader;
import com.shfloop.simply_shaders.pack_loading.ShaderPackLoader;
import com.shfloop.simply_shaders.Shadows;
import com.llamalad7.mixinextras.sugar.Local;
import com.shfloop.simply_shaders.SimplyShaders;
import com.shfloop.simply_shaders.rendering.*;
import finalforeach.cosmicreach.singletons.GameSingletons;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.ui.UI;
import finalforeach.cosmicreach.world.Sky;
import finalforeach.cosmicreach.world.Zone;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL32C;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.opengl.GL45;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGame.class)
public abstract class InGameMixin extends GameState {

    @Shadow
    static protected PerspectiveCamera rawWorldCamera;

    //i think instead now that sprinting influences the fov i need to change from this "Lcom/badlogic/gdx/utils/viewport/Viewport;apply()V"
    //to before sky.drawsky


    @Shadow private static Player localPlayer;

    //for now ill remove this and just create the buffer in render loop not sure why this doesnt work anymore
    //i might want to move this into the player creation or whenever the gamestate switches to ingame
    //none of the shadowfbo depends on ingame existing so i really dont know why it was failing and not triggering exceptions
    @Inject(method = "create()V", at = @At("TAIL"))
    private void injectCreate(CallbackInfo ci) {
//        if (Shadows.initalized) {
//            Shadows.cleanup(); //force them to cleanup regardless
//        }
//        if (Shadows.shaders_on && !Shadows.initalized) {
//            try {
//                Shadows.turnShadowsOn();
//
//            } catch (Exception e) {
//                //if the shadows cant be turned on just call cleanup
//                Shadows.cleanup();
//            }
//        }
        Shadows.lastUsedCameraPos = new Vector3(0,0,0);
    }

    @Inject(method = "loadWorld(Lfinalforeach/cosmicreach/world/World;)V", at =@At("TAIL"))
    private void injectloadWorld(CallbackInfo ci) {

        if (SimplyShaders.timerQuery != null) {
            SimplyShaders.timerQuery.dispose();
        }
        SimplyShaders.timerQuery = new TimerQuery(3);
        ShaderPackLoader.remakeFBO();
    }

    @Inject(method = "dispose()V", at = @At("TAIL"))
    private void injectDispose(CallbackInfo ci) {
        if (SimplyShaders.timerQuery != null) {
            SimplyShaders.timerQuery.dispose();
        }
            Shadows.cleanup();
            //ChunkShader.reloadAllShaders(); base game shaders dont need to be reloaed as they will never change just need to swap out the defaulty static shaders and remesh
            SimplyShaders.holder.dispose();
            SimplyShaders.holder = null;

    }
    private final float[] temporarySkyColor = {0,0,0,0};
    private final float[] TRANSPARENT = {0,0,0,0};
    private final float[] WHITE = {1.0f,1.0f,1.0f,1.0f};

    @Inject(method = "render()V", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/world/Sky;drawSky(Lcom/badlogic/gdx/graphics/Camera;)V", shift = At.Shift.BEFORE))// Lfinalforeach/cosmicreach/world/Sky;drawStars(Lcom/badlogic/gdx/graphics/Camera)V
    private void injectInGameRender(CallbackInfo ci, @Local Zone playerZone) {
        SimplyShaders.timerQuery.startQuery(0);
        //this is causing fps to drop by 1/3
        if(Shadows.shaders_on) {
            if (!Shadows.initalized) {
                try {
                    Shadows.turnShadowsOn(); //turning shadows on will reload each shader

                } catch (Exception e) {
                    //if the shadows cant be turned on just call cleanup
                    Shadows.cleanup();
                }
                //ChunkShader.reloadAllShaders();
            }

            Shadows.lastUsedCameraPos.set(rawWorldCamera.position);



            //Shadows.lastUsedCameraPos = rawWorldCamera.position.cpy();


            Shadows.updateCenteredCamera();
            if (BlockPropertiesIDLoader.packEnableShadows) { //TODO TEMPORARY
                Gdx.gl.glBindFramebuffer(36160, Shadows.shadow_map.getDepthMapFbo());
                Gdx.gl.glViewport(0,0, Shadows.shadow_map.getDepthMapTexture().getWidth(), Shadows.shadow_map.getDepthMapTexture().getHeight());
                //what would happen if i cleared the buffer to 0 instead of 1. i cuoldnt use depth testing to eliminate frags but it would remove the problem of shadows going through terrain that doesnt have faces like the edge of render distance

                Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
                Shadows.shadowPass = true;
                // nned to improve framerate its getting cut by like 1/3 with default shaders
                //using hte same render causes a few extra BlockModelJson calls but it isnt very much compared to what it did originally
                //GameSingletons.zoneRenderer.render(playerZone, Shadows.getCamera());
                ((BatchedZoneRendererInterface)GameSingletons.zoneRenderer).renderShadowPass(playerZone,Shadows.getCamera());

                Gdx.gl.glDepthMask(true);
                //Gdx.gl.glCullFace(GL20.GL_BACK);
                Gdx.gl.glEnable(GL20.GL_CULL_FACE);

                for (Entity e : playerZone.getAllEntities()) {
                    e.render(Shadows.getCamera()); //ENtity shaders during shadow pass also need to be distorted to apply correctly to shadow map
                }
                ((BatchedZoneRendererInterface)GameSingletons.zoneRenderer).markWaterAsSeen(); //i need to make sure i set the chunkbatches to act like they were rendered or else it messes up water rendering for main pass
                Shadows.shadowPass = false;


                //clear  framebuffer 0
                SimplyShaders.holder.unBindFrameBuffer(); // might not have to call this if im setting a different framebuffer
                Gdx.gl.glViewport(0,0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                Gdx.gl.glClear(org.lwjgl.opengl.GL20.GL_DEPTH_BUFFER_BIT | org.lwjgl.opengl.GL20.GL_COLOR_BUFFER_BIT); // might not need this

            }



            //Shadows.redraw_stars = true;

        }
        SimplyShaders.timerQuery.endQuery();
        SimplyShaders.timerQuery.startQuery(1);
        //i want to bind the new framebuffer to always be used

//        //cant forget to clear the framebuffer
//        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
//        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT | GL20.GL_COLOR_BUFFER_BIT);

//        int[] drawBuffers = {GL32.GL_COLOR_ATTACHMENT0,GL32.GL_COLOR_ATTACHMENT1};
//        GL32.glDrawBuffers(drawBuffers);

        //SimplyShaders.fbo.begin();

        //ScreenUtils.clear(1.0f,1.0f,0.0f,1.0f,true);

        SimplyShaders.holder.clearTextures(playerZone);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

        //need to bind the framebuffer that has the depth attachment
        //should have a better solution
        Gdx.gl.glViewport(0,0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        SimplyShaders.holder.bindFrameBuffer(SimplyShaders.holder.baseGameFrameBuffer);


        //System.out.println("RENDERSTART");
        SimplyShaders.inRender = true;
//        int[] drawBuffers = {GL32.GL_COLOR_ATTACHMENT0};// drawbuffers for sky star shader not needed since its in gameshader now
//        GL32.glDrawBuffers(drawBuffers);
//        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT | GL20.GL_COLOR_BUFFER_BIT);
//        GameSingletons.zoneRenderer.render(playerZone, rawWorldCamera);
//        SimplyShaders.fbo.end();

    }
        //stop the framebuffer so UI gets rendered normally to the screen
        //then i need to do the final render so that ui displays properly
    @Inject(method = "render",at = @At(value = "INVOKE", target ="Lfinalforeach/cosmicreach/rendering/GameParticleRenderer;render(Lcom/badlogic/gdx/graphics/Camera;F)V", shift = At.Shift.AFTER))
    private void deferredWaterRender(CallbackInfo ci) {
        //deferred rendering goes here
        //in iris it seems to make a seperate framebuffer for water rendering and uses alt as the render texture if deferred renders to colorTex1 for ex
        //
        GL45.glMemoryBarrier(GL45.GL_FRAMEBUFFER_BARRIER_BIT);
        ((BatchedZoneRendererInterface) GameSingletons.zoneRenderer).renderWater(localPlayer.getZone(), rawWorldCamera);
    }
    @Inject(method = "render",at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/ui/UI;render()V"))
    private void stopRenderBuffer(CallbackInfo ci) {
        //do composite rendes on the same screen quad;
        //THIS WILL CRASH IF THE NAIVE RENDERER IS USED though i dont think you can still select it


        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST); // disable it so screen quad doesnt get removed
        //i think i walso want to disable gl blend
        Gdx.gl.glDisable(GL20.GL_BLEND); //gl blend is enabled in batched zone renderer

        SimplyShaders.timerQuery.endQuery();
        SimplyShaders.timerQuery.startQuery(2);
        if (ShaderPackLoader.shaderPackOn) {
            SimplyShaders.compositeStageRenderer.render(rawWorldCamera);
        }



        SimplyShaders.inRender = false;// should stop finalshader from from drying to call drawbuffers

        SimplyShaders.finalStageRenderer.render(rawWorldCamera);
        //System.out.println("DONE QUAD");


        //need to reset each textures min filter because if the texture had mip maps generated it needed to be set to gl_linear_mipmap_linear to read properly
        //but needs to be set to GL_LINAER if not
//        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
//        for (BufferTexture tex: SimplyShaders.holder.getRenderTextures()) {
//
//            if (tex.isMipMapEnabled) {
//                Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, tex.getID());
//                Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_2D,GL20.GL_TEXTURE_MIN_FILTER,GL32C.GL_LINEAR);
//            }
//        }
//        for (BufferTexture tex: SimplyShaders.holder.getSwapTextures()) {
//
//            if (tex.isMipMapEnabled) {
//                Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, tex.getID());
//                Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_2D,GL20.GL_TEXTURE_MIN_FILTER,GL32C.GL_LINEAR);
//            }
//        }




        SimplyShaders.timerQuery.endQuery();
        SimplyShaders.timerQuery.swapQueryBuffers();
        if (!UI.renderUI) {
            //i think i just need to clear the screen
            Gdx.gl.glActiveTexture(33984);
            Gdx.gl.glBindTexture(3553, 0); // this is called after uirender so it might be important
        }
        Shadows.previousCameraPosition.set(rawWorldCamera.position);
        Shadows.previousProjection.set(rawWorldCamera.projection);
        Shadows.previousView.set(rawWorldCamera.view);
        //need to bind vertexarray
        //need to bind the textuere maybe
        //need to call glDrawArrays(GL_TRIANGLE,0,6)


        //MIGHT NEED TO USE GLBIINDFRAGDATALOCATION on all shaders to bind the correct output to attachment

        // so i think i should just use a sprite batch



        //so i need a directive in the shader that tells simply shaders which drawbuffers to enable and in which order
    }
}
