package com.shfloop.simply_shaders;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;

import com.badlogic.gdx.math.Vector3;
import com.shfloop.simply_shaders.rendering.RenderFBO;
import finalforeach.cosmicreach.io.SaveLocation;
import finalforeach.cosmicreach.rendering.shaders.ChunkShader;
import finalforeach.cosmicreach.rendering.shaders.EntityShader;
import finalforeach.cosmicreach.world.Sky;

import java.io.IOException;

public class Shadows {
    public static float normal_float;
    public static ChunkShader SHADOW_CHUNK;
    public static EntityShader SHADOW_ENTITY;
    public static Vector3 lastUsedCameraPos;
    public static boolean shaders_on = false;
    public static int time_of_day = 0;
    public static boolean updateTime = false;
    public static int cycleLength = 38400;
    public static boolean doDaylightCycle = true;

    private static OrthographicCamera sunCamera;
    public static ShadowMap shadow_map;

    //nolonger used in .1.44
//    public static VertexAttribute posAttrib = VertexAttribute.Position();
//    public static VertexAttribute uvIdxAttrib = new VertexAttribute(32, 1, 5126, false, "a_uvIdx");
//    public static VertexAttribute lightingAttrib = new VertexAttribute(4, 4, "a_lighting");
//
//    public static VertexAttribute normal_attrib = new VertexAttribute(32, 1, "as_normal_dir");

    private static Vector3 lastCameraPos = new Vector3(0,0,0);
    public static boolean shadowPass = false;
    public static boolean initalized = false;
    private static final String[] SHADERS_TO_COPY = {"chunk.frag.glsl","chunk.vert.glsl", "shadowpass.frag.glsl","shadowpass.vert.glsl", "shadowEntity.frag.glsl", "shadowEntity.vert.glsl", "final.vert.glsl", "final.frag.glsl", "composite0.vert.glsl", "composite0.frag.glsl"};

    static {
        //not sure what viewport size i should be using
        sunCamera =  new OrthographicCamera(400, 400); // should change this to be initialized on the player instead
        sunCamera.near = 0.05F;
        sunCamera.far = 2000.0F;

        //calcSunDirection();
    }

    public static void reloadShaders() { //dont need to call reload shaders because that happens after this gets called by chunkshader
        if (shaders_on) {
            cleanup();
            try {
                turnShadowsOn(); //this should reset the sky time
            } catch (Exception e) {

                throw new RuntimeException(e);

            }
        }
    }
    public static void turnShadowsOn()  {
        System.out.println("Turning Shaders On");


        ShaderPackLoader.switchToShaderPack();


        //TODO add other shaders
        System.out.println("creating Shadow map");
        try { shadow_map= new ShadowMap();}
        catch (Exception e){
            cleanup();
            e.printStackTrace();
            System.out.print("ERROR   ");

            System.out.println(e);
            Shadows.shaders_on = false;
            initalized = false;
            return;
        }
        initalized = true;


        System.out.println("Finished Loading Shaders");
        //after shaders are loaded bind the render textures
        if (SimplyShaders.buffer != null) {
            //RenderFBO.bindRenderTextures();

        } else {
            System.out.println("Render Textures NOT BOUND");

        }

       // ChunkShader.reloadAllShaders();
    }



    public static OrthographicCamera getCamera() {
        // needs to check the current view frustrum how do i differ it from Menu cam vs player cam does matter cause shadow map will be different needs a way to take the current camera

        return sunCamera;
    }
    public static void updateCenteredCamera(boolean forceUpdate) { // can just be called every render

        Vector3 player_center = lastUsedCameraPos.cpy();

        double dist_traveled =  Math.sqrt((lastCameraPos.x - player_center.x) * (lastCameraPos.x - player_center.x) + (lastCameraPos.y - player_center.y) * (lastCameraPos.y - player_center.y) + (lastCameraPos.z - player_center.z) * (lastCameraPos.z - player_center.z));
        if (!forceUpdate && dist_traveled < 3.0) { //three blocks is better
            return; // if the player hasnt traveled far enough from last sun camera pos than return early so no update happens
        }

        lastCameraPos = lastUsedCameraPos.cpy();
        //System.out.println("UPDATE CAMERA CENTER");
        //im fairly confident this will make sun camera look at center of player/camera
        Shadows.sunCamera.viewportHeight = 400;
        Shadows.sunCamera.viewportWidth =400; // redundant but ill see what it does


        Vector3 old_direction = Shadows.sunCamera.direction.cpy();
        final float SUN_DISTANCE = -2000f; // the direction is opposite of what i want so this fixes it
        Shadows.sunCamera.position.x = player_center.x + old_direction.x * SUN_DISTANCE;
        Shadows.sunCamera.position.y = player_center.y + old_direction.y * SUN_DISTANCE;
        Shadows.sunCamera.position.z = player_center.z + old_direction.z * SUN_DISTANCE;
        Vector3 sun_pos = Shadows.sunCamera.position.cpy();
        double dist_to_player = Math.sqrt((sun_pos.x - player_center.x) * (sun_pos.x - player_center.x) + (sun_pos.y - player_center.y) * (sun_pos.y - player_center.y) + (sun_pos.z - player_center.z) * (sun_pos.z - player_center.z));
        Shadows.sunCamera.far = (float)dist_to_player + 256.0f;
        Shadows.sunCamera.near = (float)dist_to_player - 256.0f;



        Shadows.sunCamera.update();
    }
    public static void calcSunDirection() {
//        float temp_time = time_of_day - 960  ;
////        if (time_of_day > 1500) {
////            temp_time = 1500 - time_of_day;
////        }
//        sunCamera.position.x = temp_time;
//        sunCamera.position.y = -1.0f / 1850.0f * temp_time * temp_time + 1850; // give it a little more height
//        sunCamera.position.z = -1.0f / 1850.0f * temp_time * temp_time + 1850; //
//        sunCamera.lookAt(0,0,0);
//        sunCamera.up.set(0,1,0);
        float dayPerc =   360.0f * (float)time_of_day / cycleLength;
        sunCamera.direction.set(-0.514496f, -0.857493f, -0.0f).rotate(dayPerc, 1.0F, 0.0F, 1.0F);
        sunCamera.update();

         // whenever the sun changes the next render pass will force update the new camera with new direction
    }

    // for shader files all i need to do is switch the default static shaders for each shader type
    public static void cleanup()  {
        //if copy base shaders fails the game need to stop for good isnt much i can doi to recover
        System.out.println("Turning Shaders OFF");

        ShaderPackLoader.switchToDefaultPack();

        //TODO add other shaders
        if (shadow_map != null) {
            shadow_map.cleanup(); //:(
        }
        initalized = false;


    }



}
