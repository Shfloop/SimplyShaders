package com.shfloop.simply_shaders.mixins;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.shfloop.simply_shaders.DynamicSkyInterface;
import com.shfloop.simply_shaders.Shadows;
import finalforeach.cosmicreach.rendering.shaders.SkyShader;
import finalforeach.cosmicreach.world.DynamicSky;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(DynamicSky.class)
public abstract  class DynamicSkyMixin implements DynamicSkyInterface {
    public float lastUpdateTime;

    @Override
    public void setLastUpdateTime() {
        forceUpdate = true;
    }

    public boolean forceUpdate = true;

    @Shadow protected SkyShader skyShader;
    @Shadow
    protected Vector3 sunDirection;
    @Shadow protected float i;
    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lcom/badlogic/gdx/math/Vector3;rotate", shift = At.Shift.AFTER))
    private void rotateSunAngle(CallbackInfo ci) {

        if (Shadows.shaders_on) {
            sunDirection.rotate(45f, 1.0f,0.0f,0.0f);
            Vector3 cameraDirection = Shadows.getCamera().direction;
            cameraDirection.x = sunDirection.x * -1;
            cameraDirection.y = sunDirection.y * -1;
            cameraDirection.z = sunDirection.z * -1;
            cameraDirection.nor();
//            Vector3 norSunVector = sunDirection.cpy();
//            norSunVector.nor();
//            lastUpdateTime = i;
//            Shadows.getCamera().direction.set(new Vector3(norSunVector.x * -1 , norSunVector.y * -1, norSunVector.z * -1));
            Shadows.getCamera().update();
        }
    }


}
