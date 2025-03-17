package com.shfloop.simply_shaders.rendering;

import com.badlogic.gdx.graphics.Camera;
import finalforeach.cosmicreach.world.Zone;

public interface BatchedZoneRendererInterface {
    public void renderWater(Zone zone, Camera worldCamera);
}
