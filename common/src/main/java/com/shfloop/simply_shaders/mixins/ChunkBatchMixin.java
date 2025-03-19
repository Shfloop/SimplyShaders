package com.shfloop.simply_shaders.mixins;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.shfloop.simply_shaders.Shadows;
import com.llamalad7.mixinextras.sugar.Local;
import com.shfloop.simply_shaders.rendering.ChunkBatchInterface;
import finalforeach.cosmicreach.rendering.ChunkBatch;
import finalforeach.cosmicreach.rendering.meshes.MeshData;
import finalforeach.cosmicreach.rendering.meshes.IGameMesh;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.world.Zone;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static finalforeach.cosmicreach.rendering.ChunkBatch.lastBoundShader;

@Mixin(ChunkBatch.class)
public abstract class ChunkBatchMixin implements ChunkBatchInterface {

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

    @Shadow private static int uniformLocationBatchPosition;

    @Shadow public static GameShader lastBoundShader;

    /**
     * @author shfloop
     * @reason because
     *
     */
    @Overwrite
    public void render(Zone zone, Camera worldCamera) {
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
                        float cx = worldCamera.position.x;
                        float cy = worldCamera.position.y;
                        float cz = worldCamera.position.z;
                        worldCamera.position.setZero();
                        worldCamera.update();
                        lastBoundShader.bind(worldCamera);
                        worldCamera.position.set(cx, cy, cz);
                        worldCamera.update();
                        lastBoundShader.bindOptionalUniform3f("cameraPosition", worldCamera.position);
                        uniformLocationBatchPosition =lastBoundShader.getUniformLocation("u_batchPosition");
                    }
                }else if (lastBoundShader != this.shader) {
                    lastBoundShader = this.shader;
                    float cx = worldCamera.position.x;
                    float cy = worldCamera.position.y;
                    float cz = worldCamera.position.z;
                    worldCamera.position.setZero();
                    worldCamera.update();
                    lastBoundShader.bind(worldCamera);
                    worldCamera.position.set(cx, cy, cz);
                    worldCamera.update();
                    lastBoundShader.bindOptionalUniform3f("cameraPosition", worldCamera.position);
                    uniformLocationBatchPosition = this.shader.getUniformLocation("u_batchPosition");
                }

                float bx = this.boundingBox.min.x - worldCamera.position.x;
                float by = this.boundingBox.min.y - worldCamera.position.y;
                float bz = this.boundingBox.min.z - worldCamera.position.z;
                lastBoundShader.bindOptionalUniform3f(uniformLocationBatchPosition, bx, by, bz);
                this.mesh.bind(lastBoundShader.shader);
                this.mesh.render(lastBoundShader.shader, 4);
                this.mesh.unbind(lastBoundShader.shader);
            }
        }

        this.seen = false;
        this.meshDatasToAdd.size = 0;
    }
    public void markAsSeen() {
        seen = false;
        meshDatasToAdd.size = 0;
    }
}
