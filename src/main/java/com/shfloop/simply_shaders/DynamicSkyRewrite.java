package com.shfloop.simply_shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.SphereShapeBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.rendering.shaders.SkyShader;
import finalforeach.cosmicreach.world.Sky;
import finalforeach.cosmicreach.world.World;
import finalforeach.cosmicreach.world.Zone;

public class DynamicSkyRewrite extends Sky {

    private SkyShader skyShader;
    Mesh skyMesh;
    Vector3 sunDirection = new Vector3(0.514496f, 0.857493f, 0.0f);
    float i;
    float lastT =(int)Shadows.time_of_day * 20;
    int timeT = (int)Shadows.time_of_day * 20;
    int lastTUpdate = 0;
    public DynamicSkyRewrite(String nameLangKey) {
        super(nameLangKey, new Color(Color.BLACK), new Color(Color.WHITE), true);
        this.skyShader = SkyShader.SKY_SHADER;
        //need to initalize the lastTupdate to the current time so it doesnt update on f6 reload
        World world = InGame.world;
        Zone playerZone = InGame.getLocalPlayer().getZone(world);
        lastTUpdate = (int) playerZone.getCurrentWorldTick();
    }
    public void drawSky(Camera worldCamera) {
        if (this.skyMesh == null) {
            MeshBuilder meshBuilder = new MeshBuilder();
            meshBuilder.begin(this.skyShader.allVertexAttributesObj, 4);
            SphereShapeBuilder.build(meshBuilder, 100.0F, 100.0F, 100.0F, 16, 16, 0.0F, 360.0F, 0.0F, 360.0F);
            this.skyMesh = meshBuilder.end();
        }

        Gdx.gl.glDepthMask(false);
        if (this.shouldDrawStars) {
            Gdx.gl.glCullFace(1029);
            this.drawStars(worldCamera);
        }

        Gdx.gl.glEnable(3042);
        Gdx.gl.glCullFace(1028);
        this.skyShader.bind(worldCamera);
        this.skyShader.bindOptionalUniform3f("u_sunDirection", this.sunDirection);
        this.skyMesh.bind(this.skyShader.shader);
        this.skyMesh.render(this.skyShader.shader, 4);
        this.skyMesh.unbind(this.skyShader.shader);
        this.skyShader.unbind();
        Gdx.gl.glDepthMask(true);
    }

    public void update() {
        World world = InGame.world;
        Zone playerZone = InGame.getLocalPlayer().getZone(world);
//        float currentTimeSeconds = (float)playerZone.currentTick * 0.05F;
//       final float cycleLength = 60.0f;
//       //this gives percentage cycle completeed
//        this.i = 360.0F * (currentTimeSeconds % cycleLength) / cycleLength; // this makes it easier to set the time


        final float cycleLength = 38400.0f;
        //final float cycleLength = 1920.0f;
        //timeT
        int currentTick = (int)playerZone.getCurrentWorldTick() - lastTUpdate;
            timeT += currentTick;
            if (timeT > cycleLength) {
                timeT = 0;
                lastT = 0;
            }
            lastTUpdate = (int)playerZone.getCurrentWorldTick(); // this seems dumb but it will get changed soon

        if (Shadows.updateTime) { //time of day should last 1920 seconds like base game
            //means the slider in shader menu was touched
            //i want to set the current timeT to the new time
            timeT =(int) Shadows.time_of_day * 20;

            //System.out.println("UPDATETIME " + timeT);
            lastT = timeT;
            //this could be better as its just a duplicate but okay for a temp thing


        }

        this.i =   360.0f * (float)timeT / cycleLength;
        //System.out.println("TIME " + timeT + "LAST T" + lastT);
        //TODO also add a way to stop the time from ticking
 //TODO the rotation probabaly isnt correct but looks okay for now
        this.sunDirection.set(0.514496f, 0.857493f, 0.0f).rotate(this.i, 1.0F, 0.0F, 1.0F); // need to rotate it differently
        //if (currentTimeSeconds > lastT + cycleLength / 1000) {
            //lets update the sun camera every 5.0
        if (timeT > lastT + cycleLength / 4000  || Shadows.updateTime){ // i want to update every 2000 of cycle
            Shadows.updateTime = false; // bit weird but it should work
            //System.out.println("UPDATE_TIME " + timeT);
//            System.out.println("SUN DIRECTION " + this.sunDirection);
            //FIXME the time isnt being set correctly
            Shadows.time_of_day =  ((float) timeT / 20); //this doesnt work if the cycle time is changed
            if (Shadows.shaders_on) {
                if (Shadows.lastUsedCameraPos != null) { //feel bad doing this but easy fix
                    Shadows.forceUpdate = true; //TODO this should really just be a method input
                    Shadows.getCamera().direction.set(new Vector3(this.sunDirection.x * -1 , this.sunDirection.y * -1, this.sunDirection.z * -1));
                    Shadows.updateCenteredCamera();

                }

            }

            lastT = timeT;
        }
        float noonDot = this.sunDirection.dot(Vector3.Y);
        float l = MathUtils.clamp(noonDot + 0.25F, 0.0F, 1.0F);
        float r = l * l;
        float g = l * l * l;
        float b = 0.05F + l * l * l;
        float sr = 0.1F;
        float sg = 0.4F;
        float sb = 0.7F;
        sr = MathUtils.lerp(sr, r, 1.0F - l);
        sg = MathUtils.lerp(sg, g, 1.0F - l);
        sb = MathUtils.lerp(sb, b, 1.0F - l);
        float sunSetOrRiseSkyR = 0.75F;
        float sunSetOrRiseSkyG = 0.2F;
        float sunSetOrRiseSkyB = 0.0F;
        float n = MathUtils.clamp(1.0F - Math.abs(noonDot), 0.0F, 1.0F);
        sr = MathUtils.lerp(sr, sunSetOrRiseSkyR, n);
        sg = MathUtils.lerp(sg, sunSetOrRiseSkyG, n);
        sb = MathUtils.lerp(sb, sunSetOrRiseSkyB, n);
        sr = MathUtils.lerp(sr, 0.0F, Math.max(0.0F, -noonDot));
        sg = MathUtils.lerp(sg, 0.0F, Math.max(0.0F, -noonDot));
        sb = MathUtils.lerp(sb, 0.0F, Math.max(0.0F, -noonDot));
        this.currentSkyColor.set(sr, sg, sb, 1.0F);
        float ar = Math.max(sr, r);
        float ag = Math.max(sg, g);
        float ab = Math.max(sb, b);
        ar = MathUtils.clamp(ar, 0.05F, 0.75F);
        ag = MathUtils.clamp(ag, 0.05F, 0.75F);
        ab = MathUtils.clamp(ab, 0.1F, 0.75F);
        this.currentAmbientColor.set(ar, ag, ab, 1.0F);
    }
}
