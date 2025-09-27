package com.shfloop.simply_shaders.rendering;

import com.badlogic.gdx.graphics.Camera;
import finalforeach.cosmicreach.world.Zone;

public interface ChunkBatchInterface {
   public void markAsSeen();
   public void renderShadowPass(Zone zone,Camera camera);
}
