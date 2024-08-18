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


public class DynamicSkyClone extends Sky {
    //DynamicSky isnt public and i dont know how to use access wideners
    private SkyShader skyShader;
    Mesh skyMesh;
    Vector3 sunDirection = new Vector3(0.0F, 1.0F, 0.0F);
    float i;

    public DynamicSkyClone(String nameLangKey) {
        super(nameLangKey, new Color(Color.BLACK), new Color(Color.WHITE), true);
        this.skyShader = SkyShader.SKY_SHADER;
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
        float currentTimeSeconds = (float)playerZone.getCurrentWorldTick() * 0.05F;
        float cycleLength = 1920.0F;
        this.i = 360.0F * (currentTimeSeconds / cycleLength);
        this.sunDirection.set(0.0F, 1.0F, 0.0F).rotate(this.i, 0.0F, 0.0F, 1.0F);
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
