package com.shfloop.simply_shaders.mixins;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntSet;
import com.llamalad7.mixinextras.sugar.Local;
import com.shfloop.simply_shaders.Shadows;
import com.shfloop.simply_shaders.SimplyShaders;
import com.shfloop.simply_shaders.rendering.BatchedZoneRendererInterface;
import com.shfloop.simply_shaders.rendering.ChunkBatchInterface;
import com.shfloop.simply_shaders.rendering.RenderTextureHolder;
import finalforeach.cosmicreach.rendering.BatchedZoneRenderer;
import finalforeach.cosmicreach.rendering.ChunkBatch;
import finalforeach.cosmicreach.world.Zone;
import org.lwjgl.opengl.GL45;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BatchedZoneRenderer.class)
public abstract class BatchedZoneRendererMixin implements BatchedZoneRendererInterface {
    @Shadow @Final private IntMap<Boolean> layerWritesToDepth;

    @Shadow protected abstract void getChunksToRender(Zone zone, Camera worldCamera);

    @Shadow protected abstract void requestMeshes();

    @Shadow protected abstract void disposeUnusedBatches(boolean unloadAll);

    @Shadow protected abstract void addMeshDatasToChunkBatches();

    @Shadow @Final private IntArray layerNums;

    @Shadow @Final private IntSet seenLayerNums;

    @Shadow @Final private IntMap<Array<ChunkBatch>> layers;

    @Shadow public boolean drawDebugLines;

    @Shadow protected abstract void drawDebugLines(Camera worldCamera);

//    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/rendering/BatchedZoneRenderer;requestMeshes()V"))
//    private void injectSwitchToFrontFace(CallbackInfo ci) {
//        if(Shadows.shadowPass) {
//            //Gdx.gl.glCullFace(GL20.GL_FRONT);
//            //only like 30 fps drop in a test might have worse cases but im not sure how else to fix shadows
//            Gdx.gl.glDisable(GL20.GL_CULL_FACE);
//        }
//
//    }
    @Unique
    private boolean copiedDepthTex = false;
//    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/badlogic/gdx/graphics/GL20;glDepthMask(Z)V",shift = At.Shift.AFTER))
//    private void copyDepthTexAndEnableDepthMask(CallbackInfo ci, @Local(ordinal = 0) int layerNum) {
//
//        if (!copiedDepthTex && !layerWritesToDepth.get(layerNum,true)) {
////            if (Shadows.shadowPass) {
////
////            }
//            copiedDepthTex = true;
//            //TODO during a shadow pass once the water starts to get rendered i can just return and cancel the water render ni shadowpass
//            GL45.glMemoryBarrier(GL45.GL_TEXTURE_UPDATE_BARRIER_BIT);
////            //might not need this instead use
//            GL45.glCopyImageSubData(
//                    SimplyShaders.holder.depthTexture.id, GL20.GL_TEXTURE_2D,0,0,0,0,
//                    SimplyShaders.holder.noWaterDepthTex.id, GL20.GL_TEXTURE_2D, 0, 0, 0, 0,
//                    SimplyShaders.holder.depthTexture.width, SimplyShaders.holder.depthTexture.height, 1);// but i need to bind teh second texture to a framebuffre i think;
//        }
//        Gdx.gl.glDepthMask(true);// i alwasy want transparent geometry to write to depth tex; i copy out the pre translucent depth to a seperate texture
//
//    }
    @Unique
    private int savedLayerNumIdx= -1;

    /**
     * @author shfloop
     * @reason The base game doesnt defer the water rendering leaving entities to render on top of water without blending into the water
     * this also makes it hard to to stuff with reflections because i need the water to fail depth tests if its being hidden by entites /held item
     */
    @Overwrite
    public void render(Zone zone, Camera worldCamera) {
        savedLayerNumIdx = -1;// have to initialize this to -1 each frame render
        Gdx.gl.glEnable(2929);
        Gdx.gl.glDepthFunc(513);
        Gdx.gl.glEnable(2884);
        Gdx.gl.glCullFace(1029);
        Gdx.gl.glEnable(3042);
        Gdx.gl.glBlendFunc(770, 771);
        getChunksToRender(zone, worldCamera);
        requestMeshes();
//        if(Shadows.shadowPass) {
//            //Gdx.gl.glCullFace(GL20.GL_FRONT);
//            //only like 30 fps drop in a test might have worse cases but im not sure how else to fix shadows
//            Gdx.gl.glDisable(GL20.GL_CULL_FACE);
//        }
        disposeUnusedBatches(false);
        addMeshDatasToChunkBatches();
        ChunkBatch.lastBoundShader = null;
        Gdx.gl.glDepthMask(true);// i want all terrain and water to render to depth texture
        //i exit early when water is reached where base game switches depthMas to false
        //make sure to copy the depth right before water is rendered
        for(int i = 0; i < layerNums.items.length; i++) {
            int layerNum = layerNums.items[i];
            if(seenLayerNums.contains(layerNum)) {
                Array<ChunkBatch> layer = layers.get(layerNum);
                if (layer != null) {
                    boolean renderingTerrain = layerWritesToDepth.get(layerNum, true);
                    if (!renderingTerrain) {
                        //need to save the curent i value to resume rendering with water
                        //this is good because in shadowPass idont want to render water anyway so i need to not call render water
                        savedLayerNumIdx = i;
                        break;

                    }
                    for (ChunkBatch batch : layer) {
                        batch.render(zone, worldCamera);
                    }
                }


            }
        }
        if (ChunkBatch.lastBoundShader != null) {
            ChunkBatch.lastBoundShader.unbind();
        }

        if (drawDebugLines) {
           drawDebugLines(worldCamera);
        }

        Gdx.gl.glActiveTexture(33984);
        Gdx.gl.glBindTexture(3553, 0);
    }
    public void renderWater(Zone zone, Camera worldCamera){
        if (savedLayerNumIdx == -1) {
            //there can be frames withohut any water chunks rendered
            return;
        }
        Gdx.gl.glEnable(2929);
        Gdx.gl.glDepthFunc(513);
        Gdx.gl.glEnable(2884);
        Gdx.gl.glCullFace(1029);
        Gdx.gl.glEnable(3042);
        Gdx.gl.glBlendFunc(770, 771);
        for (int i = savedLayerNumIdx; i < layerNums.items.length; i++) {
            int layerNum = layerNums.items[i];
            if(seenLayerNums.contains(layerNum)) {
                Array<ChunkBatch> layer = layers.get(layerNum);
                if (layer != null) {
                    for (ChunkBatch batch : layer) {
                        batch.render(zone, worldCamera);
                    }
                }


            }
        }
        if (ChunkBatch.lastBoundShader != null) {
            ChunkBatch.lastBoundShader.unbind();
        }

        if (drawDebugLines) {
            drawDebugLines(worldCamera);
        }

        Gdx.gl.glActiveTexture(33984);
        Gdx.gl.glBindTexture(3553, 0);
    }
    //TODO Leaves are untoggled in SimpleShader
    @Unique
    public void markWaterAsSeen() {
        if (savedLayerNumIdx == -1) {
            //there can be frames withohut any water chunks rendered
            return;
        }
        for (int i = savedLayerNumIdx; i < layerNums.items.length; i++) {
            int layerNum = layerNums.items[i];
            if(seenLayerNums.contains(layerNum)) {
                Array<ChunkBatch> layer = layers.get(layerNum);
                if (layer != null) {
                    for (ChunkBatch batch : layer) {

                        ((ChunkBatchInterface) batch).markAsSeen();
                    }
                }


            }
        }
    }
    public void renderShadowPass(Zone zone, Camera worldCamera) {
        savedLayerNumIdx = -1;// have to initialize this to -1 each frame render
        Gdx.gl.glEnable(2929);
        Gdx.gl.glDepthFunc(513);
       // Gdx.gl.glEnable(2884); enable cull face
        Gdx.gl.glCullFace(1029); //ill set this just to make sure other rendering doesnt depend on this
        //Gdx.gl.glEnable(3042); //blend i dont think this is required for shadows ubt it would be for colored shadows if i every add those (probably not)
        Gdx.gl.glBlendFunc(770, 771);

        getChunksToRender(zone, worldCamera);
        requestMeshes();

            //Gdx.gl.glCullFace(GL20.GL_FRONT);
            //only like 30 fps drop in a test might have worse cases but im not sure how else to fix shadows
            Gdx.gl.glDisable(GL20.GL_CULL_FACE);
            Gdx.gl.glDisable(GL20.GL_BLEND);

        disposeUnusedBatches(false);
        addMeshDatasToChunkBatches();
        ChunkBatch.lastBoundShader = null;
        Gdx.gl.glDepthMask(true);// i want all terrain and water to render to depth texture
        //i exit early when water is reached where base game switches depthMas to false
        //make sure to copy the depth right before water is rendered
        for(int i = 0; i < layerNums.items.length; i++) {
            int layerNum = layerNums.items[i];
            if(seenLayerNums.contains(layerNum)) {
                Array<ChunkBatch> layer = layers.get(layerNum);
                if (layer != null) {
                    boolean renderingTerrain = layerWritesToDepth.get(layerNum, true);
                    if (!renderingTerrain) {
                        //need to save the curent i value to resume rendering with water
                        //this is good because in shadowPass idont want to render water anyway so i need to not call render water
                        savedLayerNumIdx = i;
                        break;

                    }
                    for (ChunkBatch batch : layer) {
                        ((ChunkBatchInterface) batch).renderShadowPass(zone, worldCamera);
                    }
                }


            }
        }
        if (ChunkBatch.lastBoundShader != null) {
            ChunkBatch.lastBoundShader.unbind();
        }

        if (drawDebugLines) {
            drawDebugLines(worldCamera);
        }

        Gdx.gl.glActiveTexture(33984);
        Gdx.gl.glBindTexture(3553, 0);
    }
}
