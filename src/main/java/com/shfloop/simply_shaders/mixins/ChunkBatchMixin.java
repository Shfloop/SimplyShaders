package com.shfloop.simply_shaders.mixins;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.shfloop.simply_shaders.Shadows;
import com.llamalad7.mixinextras.sugar.Local;
import finalforeach.cosmicreach.rendering.ChunkBatch;
import finalforeach.cosmicreach.rendering.MeshData;
import finalforeach.cosmicreach.rendering.meshes.IGameMesh;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.world.Zone;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static finalforeach.cosmicreach.rendering.ChunkBatch.lastBoundShader;

@Mixin(ChunkBatch.class)
public abstract class ChunkBatchMixin {
    //@Inject(method = "render(Lfinalforeach/cosmicreach/world/Zone;Lcom/badlogic/gdx/graphics/Camera;)V", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/rendering/meshes/IGameMesh;bind(Lcom/badlogic/gdx/graphics/glutils/ShaderProgram;)V", shift = At.Shift.BEFORE))

//    @Inject(method = "render(Lfinalforeach/cosmicreach/world/Zone;Lcom/badlogic/gdx/graphics/Camera;)V", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/rendering/shaders/GameShader;bindOptionalUniform3f(Ljava/lang/String;Lcom/badlogic/gdx/mat/Vector3;)V", shift = At.Shift.BEFORE))
//    private void shadowInject(CallbackInfo ci, @Local Camera worldCamera) { // just gonna have a exit early for shadow chunk not sure how i can easily implement a shadow pass vert and frag shader to work with transparent blocks
//        //loses about 50fps
//        if (Shadows.shadowPass) {
//            if (lastBoundShader !=Shadows.SHADOW_CHUNK){ //SUPER UGLY BUT IDK
//                lastBoundShader = Shadows.SHADOW_CHUNK;
//
//                //Todo maybe make sure these get added to the array last and just dynamically get the position so future shaders added bu the game dont break the mod
//
//                lastBoundShader.bind(worldCamera); // who knowws if this is going to work cause i sure dont // i could just make this perminent sun_camera
//                //a little stupid cause bind a diferent shader before and this will overwrite it but it works without overwriting the method
//
//
//                //getting rid of normal translation in shadowpass vertex shader stops the glass from being transparent
//
//            }
//        }
//    }
    @Shadow
    protected boolean seen;
    @Shadow
    protected Array<MeshData> meshDatasToAdd;
    @Shadow
    protected IGameMesh mesh;
    @Shadow
    protected GameShader shader;
    @Shadow
    protected long seenCount;
    @Shadow
    protected static long seenStep;
    @Shadow
    protected static  MeshData combined;
    @Shadow
    protected boolean needToRebuild;
    @Shadow
    protected BoundingBox boundingBox;
    @Shadow
    protected abstract void rebuildMesh(MeshData combined);

    /*
    OVerwrite chunkBath Render
        i reuse teh base game rendering for shadow pass and the shaders are tied to meshes so ive got to change them in chunkbatch
        In order to efficiently swap shaders im overwriting this
     */

    public void render(Zone zone, Camera worldCamera) { //FIXME ChunkWater shaders are being bound on each call taking up excess time
        if (this.seenCount == seenStep) {
            if (this.needToRebuild) {
                this.rebuildMesh(combined);
                this.needToRebuild = false;
            }

            if (this.mesh != null) {
                this.mesh.setAutoBind(false);
                if (Shadows.shadowPass) {
                    if(lastBoundShader != Shadows.SHADOW_CHUNK) {
                        lastBoundShader = Shadows.SHADOW_CHUNK;
                        lastBoundShader.bind(worldCamera);
                    }
                }else if (lastBoundShader != this.shader) {
                    lastBoundShader = this.shader;
                    lastBoundShader.bind(worldCamera);
                }

                lastBoundShader.bindOptionalUniform3f("u_batchPosition", this.boundingBox.min);
                this.mesh.bind(lastBoundShader.shader);
                this.mesh.render(lastBoundShader.shader, 4);
                this.mesh.unbind(lastBoundShader.shader);
            }
        }

        this.seen = false;
        this.meshDatasToAdd.size = 0;
    }
}
