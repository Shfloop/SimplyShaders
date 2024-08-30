package com.shfloop.simply_shaders.mixins;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.utils.ScreenUtils;
import com.shfloop.simply_shaders.ShaderPackLoader;
import com.shfloop.simply_shaders.Shadows;
import com.llamalad7.mixinextras.sugar.Local;
import com.shfloop.simply_shaders.SimplyShaders;
import com.shfloop.simply_shaders.rendering.FinalShader;
import com.shfloop.simply_shaders.rendering.RenderFBO;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.rendering.shaders.ChunkShader;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.world.Sky;
import finalforeach.cosmicreach.world.Zone;
import org.lwjgl.opengl.GL32;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(InGame.class)
public abstract class InGameMixin extends GameState {

    @Shadow
    static protected PerspectiveCamera rawWorldCamera;

    //i think instead now that sprinting influences the fov i need to change from this "Lcom/badlogic/gdx/utils/viewport/Viewport;apply()V"
    //to before sky.drawsky


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

    }

    @Inject(method = "loadWorld(Lfinalforeach/cosmicreach/world/World;)V", at =@At("TAIL"))
    private void injectloadWorld(CallbackInfo ci) {



        if (SimplyShaders.buffer!= null) { //move this shit to loadWorld
            SimplyShaders.buffer.dispose(); // it should already be disposed but just to be sure
        }
        try {
            SimplyShaders.buffer = new RenderFBO(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            //RenderFBO.bindRenderTextures();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Inject(method = "dispose()V", at = @At("TAIL"))
    private void injectDispose(CallbackInfo ci) {

            Shadows.cleanup();
            //ChunkShader.reloadAllShaders(); base game shaders dont need to be reloaed as they will never change just need to swap out the defaulty static shaders and remesh
            SimplyShaders.buffer.dispose();
            SimplyShaders.buffer = null;

    }
    @Inject(method = "render()V", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/world/Sky;drawSky(Lcom/badlogic/gdx/graphics/Camera;)V"))// Lfinalforeach/cosmicreach/world/Sky;drawStars(Lcom/badlogic/gdx/graphics/Camera)V
    private void injectInGameRender(CallbackInfo ci, @Local Zone playerZone) {
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

            Shadows.lastUsedCameraPos = rawWorldCamera.position.cpy();
            Shadows.updateCenteredCamera();



            Gdx.gl.glBindFramebuffer(36160, Shadows.shadow_map.getDepthMapFbo());
            Gdx.gl.glViewport(0,0, Shadows.shadow_map.getDepthMapTexture().getWidth(), Shadows.shadow_map.getDepthMapTexture().getHeight());

            Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
            Shadows.shadowPass = true;
           // nned to improve framerate its getting cut by like 1/3 with default shaders
            //using hte same render causes a few extra BlockModelJson calls but it isnt very much compared to what it did originally
            GameSingletons.zoneRenderer.render(playerZone, Shadows.getCamera());

            Gdx.gl.glDepthMask(true);

            for (Entity e : playerZone.allEntities) {
                e.render(Shadows.getCamera()); //ENtity shaders during shadow pass also need to be distorted to apply correctly to shadow map
            }
            Shadows.shadowPass = false;



            Gdx.gl.glBindFramebuffer(36160, 0); // might not have to call this if im setting a different framebuffer
            Gdx.gl.glViewport(0,0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            Gdx.gl.glClear(org.lwjgl.opengl.GL20.GL_DEPTH_BUFFER_BIT | org.lwjgl.opengl.GL20.GL_COLOR_BUFFER_BIT); // might not need this



            //Shadows.redraw_stars = true;

        }
        //i want to bind the new framebuffer to always be used
        Gdx.gl.glBindFramebuffer(GL32.GL_FRAMEBUFFER, SimplyShaders.buffer.getFboHandle());
//        //cant forget to clear the framebuffer
//        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
//        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT | GL20.GL_COLOR_BUFFER_BIT);

//        int[] drawBuffers = {GL32.GL_COLOR_ATTACHMENT0,GL32.GL_COLOR_ATTACHMENT1};
//        GL32.glDrawBuffers(drawBuffers);

        //SimplyShaders.fbo.begin();
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        //ScreenUtils.clear(1.0f,1.0f,0.0f,1.0f,true);
        Sky sky = Sky.currentSky;



       // ScreenUtils.clear(sky.currentSkyColor, true); // this clears all color buffers
        //dont want to use ScreenUtils cause glClear clears all FBA to the same color;
        GL32.glDrawBuffers(RenderFBO.allDrawBuffers);
        RenderFBO.lastDrawBuffers = RenderFBO.allDrawBuffers;
        //i need to set lastdrawbuffers or else performance tanks

        //Sky Should not be drawn to other color attachments only number 0
        final float[] skyColor = {sky.currentSkyColor.r, sky.currentSkyColor.g, sky.currentSkyColor.b, sky.currentSkyColor.a};
        GL32.glClearBufferfv(GL32.GL_COLOR, 0, skyColor);

        final float[] white = {1.0f,1.0f,1.0f,1.0f};
        GL32.glClearBufferfv(GL32.GL_DEPTH, 0, white);
        GL32.glClearBufferfv(GL32.GL_COLOR,  1, white);

       final float[] transparent = {0,0,0,0};



       for (int i =2; i < 8; i++) {//should make this index into an arrya which only gets the used attachments so im not clearing all 8 when im only using 4 but i dont think its that much of an improvment
           GL32.glClearBufferfv(GL32.GL_COLOR,  i, transparent);
       }

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
    @Inject(method = "render",at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/ui/UI;render()V"))
    private void stopRenderBuffer(CallbackInfo ci) {
        //do composite rendes on the same screen quad;

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST); // disable it so screen quad doesnt get removed
        //i think i walso want to disable gl blend
        Gdx.gl.glDisable(GL20.GL_BLEND); //gl blend is enabled in batched zone renderer

//        GameShader composite0 = GameShaderInterface.getShader().get(10);// FIXME need a better way to keep track of shaders
//        composite0.bind(rawWorldCamera);
//
//        SimplyShaders.screenQuad.render(composite0.shader, GL20.GL_TRIANGLE_FAN);
//
//        composite0.unbind();
        if (ShaderPackLoader.shaderPackOn) {
            if (ShaderPackLoader.shader1.size >=10) {
                for(int i = 9; i < ShaderPackLoader.shader1.size; i++) {
                    FinalShader composite = (FinalShader)  ShaderPackLoader.shader1.get(i);
                    composite.bind(rawWorldCamera);
                    SimplyShaders.screenQuad.render(composite.shader, GL20.GL_TRIANGLE_FAN);
                    composite.unbind();
                }
            }
        }



        SimplyShaders.inRender = false;// should stop finalshader from from drying to call drawbuffers
       // System.out.println("Composite done");

        //bind framebuffer 0
        Gdx.gl.glBindFramebuffer(GL32.GL_FRAMEBUFFER, 0);
        //screen should alreayd be cleared and i dont think it woudl matter much
//        //render the screen quad with final.vsh and final.fsh just to outColor so it should display to screen
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
//        Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT);

        //SimplyShaders.fbo.end();
        //int texHandle = SimplyShaders.fbo.getColorBufferTexture().getTextureObjectHandle();
        //System.out.println("Starting quad render");
       GameShader finalShader = FinalShader.DEFAULT_FINAL_SHADER;
        finalShader.bind(rawWorldCamera);
        //finalShader.bindOptionalInt("colorTex0", texHandle);
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        //Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT | GL20.GL_COLOR_BUFFER_BIT);
        //finalShader.bindOptionalTexture("noiseTex", ChunkShader.noiseTex, 0); //this works

        SimplyShaders.screenQuad.render(finalShader.shader, GL20.GL_TRIANGLE_FAN); //as long as this is in the pool of shaders to get updated with colertexture spots i dont need to bind textures in shader
        finalShader.unbind();
        //System.out.println("DONE QUAD");

        //need to bind vertexarray
        //need to bind the textuere maybe
        //need to call glDrawArrays(GL_TRIANGLE,0,6)


        //MIGHT NEED TO USE GLBIINDFRAGDATALOCATION on all shaders to bind the correct output to attachment

        // so i think i should just use a sprite batch



        //so i need a directive in the shader that tells simply shaders which drawbuffers to enable and in which order
    }
}
