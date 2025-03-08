package com.shfloop.simply_shaders.mixins;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.shfloop.simply_shaders.DynamicSkyInterface;
import com.shfloop.simply_shaders.Shadows;
import com.shfloop.simply_shaders.SimplyShaders;
import finalforeach.cosmicreach.rendering.shaders.SkyShader;
import finalforeach.cosmicreach.world.DynamicSky;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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
    @Override
    public void setCurrentShader() {
        skyShader = SkyShader.SKY_SHADER;
    }

    public boolean forceUpdate = true;

    @Shadow protected SkyShader skyShader;
    @Shadow
    protected Vector3 sunDirection;
    @Shadow protected float i;
    @Shadow private Matrix4 sunMoonModelMat;
    @Unique
    private final Vector3 angledNoon = Vector3.Y.cpy().rotate(45f,1.0f,0.0f,0.0f).nor();
    private final Vector3 horizonVec = new Vector3(-1,0,0);

    @Inject(method = "drawSky", at = @At(value = "INVOKE", target = "Lcom/badlogic/gdx/math/Matrix4;rotate(FFFF)Lcom/badlogic/gdx/math/Matrix4;"))
    private void rotateSunAndMoon(CallbackInfo ci) {
        sunMoonModelMat.rotate(1.0f,0.0f,0.0f,45);
    }
    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lcom/badlogic/gdx/math/Vector3;dot(Lcom/badlogic/gdx/math/Vector3;)F", shift = At.Shift.AFTER))
    private void rotateSunAngle(CallbackInfo ci) {

        if (Shadows.shaders_on) {




            sunDirection.rotate(45f, 1.0f,0.0f,0.0f);
            float noonDot = angledNoon.dot(sunDirection) ;
            Vector3 cameraDirection = Shadows.getCamera().direction;
            if(noonDot >= 0) {
                cameraDirection.x = sunDirection.x * -1;
                cameraDirection.y = sunDirection.y * -1;
                cameraDirection.z = sunDirection.z * -1;
            } else {
                cameraDirection.x = sunDirection.x ;
                cameraDirection.y = sunDirection.y ;
                cameraDirection.z = sunDirection.z ;
            }


            cameraDirection.nor();
//            Vector3 norSunVector = sunDirection.cpy();
//            norSunVector.nor();
//            lastUpdateTime = i;
//            Shadows.getCamera().direction.set(new Vector3(norSunVector.x * -1 , norSunVector.y * -1, norSunVector.z * -1));





            Shadows.getCamera().update();


            float sunAngle;
            float horizonAngle = horizonVec.dot(sunDirection) * 0.5f + 0.5f;

            if (noonDot < 0) {
                sunAngle =     1.0f - horizonAngle / 2.f;
            } else {
                sunAngle = 0f + horizonAngle / 2.f;
            }
            Shadows.sunAngle = sunAngle ;
            //SimplyShaders.LOGGER.info("sun {} noon {} angle{}",sunDirection,horizonAngle,sunAngle);
            //how Optifine + iris define sunAngle 0 sunrise 0.25 noon 0.5 sunset, 0.75 midnight
        }
    }


}
