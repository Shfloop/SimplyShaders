package com.shfloop.simply_shaders;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.shfloop.simply_shaders.mixins.GameShaderAccessor;
import com.shfloop.simply_shaders.pack_loading.ShaderPackLoader;
import finalforeach.cosmicreach.chat.Chat;
import finalforeach.cosmicreach.rendering.shaders.ChunkShader;
import finalforeach.cosmicreach.rendering.shaders.EntityShader;
import finalforeach.cosmicreach.rendering.shaders.GameShader;

import java.util.HashMap;


public class Shadows {
    public static  ChunkShader BLOCK_ENTITY_SHADER  ;
    public static ChunkShader SHADOW_CHUNK;
    public static EntityShader SHADOW_ENTITY;
    public static Vector3 lastUsedCameraPos ;
    public static boolean shaders_on = false;
    public static int time_of_day = 0;
    public static boolean updateTime = false;
    public static int cycleLength = 38400;
    public static boolean doDaylightCycle = true;

    public static Vector3 previousCameraPosition = new Vector3();
    public static Matrix4 previousProjection = new Matrix4();

    public static Matrix4 previousView= new Matrix4();

    public static HashMap<String, Integer > blockPropertiesIDMap = new HashMap<String, Integer>();

    private static OrthographicCamera sunCamera;
    public static ShadowMap shadow_map;


    private static Vector3 lastCameraPos = new Vector3(0,0,0);
    public static boolean shadowPass = false;
    public static boolean initalized = false;
    public static int defaultAllShadersSize = -1;
    private static final String[] SHADERS_TO_COPY = {"chunk.frag.glsl","chunk.vert.glsl", "shadowpass.frag.glsl","shadowpass.vert.glsl", "shadowEntity.frag.glsl", "shadowEntity.vert.glsl", "final.vert.glsl", "final.frag.glsl", "composite0.vert.glsl", "composite0.frag.glsl"};

    public static float sunAngle = 0.0f; //value from 0-1.0f im guessing 1,0f means noon;
    static {

        sunCamera =  new OrthographicCamera(256, 256); // should change this to be initialized on the player instead
        sunCamera.near = -512.0f;
        sunCamera.far = 256.0f;
        blockPropertiesIDMap.put("base:leaves_poplar", 32);

        //calcSunDirection();
    }

    public static void reloadShaders() { //dont need to call reload shaders because that happens after this gets called by chunkshader
        if (shaders_on) {
            cleanup();
            try {
                turnShadowsOn();

            } catch (Exception e) {

                throw new RuntimeException(e);

            }
        }
    }
    public static void turnShadowsOn()  {
        System.out.println("Turning Shaders On");

        try {
            ShaderPackLoader.switchToShaderPack();
        } catch (RuntimeException e) {

            ShaderPackLoader.switchToDefaultPack();
            System.out.println("ERROR in Shader pack loading");
            e.printStackTrace();
            Chat.MAIN_CLIENT_CHAT.addMessage( null, e.getMessage()); //FixMe need better error handling this is needed cause when shader pack fails it is still added to allShaders
            //but i dont want this to happen for other errors so only ones where shader fails to compile / load after it gets created
            System.out.println(e.getMessage());
            Array<GameShader> defaultShaders = GameShaderAccessor.getShader();
            if (defaultAllShadersSize == -1) {
                throw new RuntimeException("YOU MESSED SOMETHING UP");
            }
            if(defaultShaders.size > defaultAllShadersSize) { //default shaders should only be size 7 // if shaderpack loading fails it can add a shader too it without removing it
                //This shouldnt be necessary but is an easy solution to shader loading without redoing the entire thing
                defaultShaders.pop();
            }
            Shadows.shaders_on = false;
            initalized = false;
            return;
        }



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


       // ChunkShader.reloadAllShaders();
    }



    public static OrthographicCamera getCamera() {

        return sunCamera;
    }
    public static void updateCenteredCamera() { // can just be called every render


       // Vector3 player_center = lastUsedCameraPos.cpy();
        float dist_traveled = lastUsedCameraPos.dst(lastCameraPos);
        //double dist_traveled =  Math.sqrt((lastCameraPos.x - player_center.x) * (lastCameraPos.x - player_center.x) + (lastCameraPos.y - player_center.y) * (lastCameraPos.y - player_center.y) + (lastCameraPos.z - player_center.z) * (lastCameraPos.z - player_center.z));



        if(dist_traveled > 2.0) { //look at a another way to do this iris seems to calc when crossing block borders
            //lastCameraPos = lastUsedCameraPos.cpy();
            lastCameraPos.set(lastUsedCameraPos);
            Shadows.sunCamera.position.set(lastCameraPos);
            Shadows.sunCamera.update();

        }



    }
    public static void calcSunDirection() {
        float dayPerc =   360.0f * (float)time_of_day / cycleLength;
        sunCamera.direction.set(-0.514496f, -0.857493f, -0.0f).rotate(dayPerc, 1.0F, 0.0F, 1.0F);
        sunCamera.update();
    }


    public static void cleanup()  {
        //if copy base shaders fails the game need to stop for good isnt much i can do to recover
        System.out.println("Turning Shaders OFF");

        ShaderPackLoader.switchToDefaultPack();

        //TODO add other shaders
        if (shadow_map != null) {
            shadow_map.cleanup(); //:(
        }
        initalized = false;


    }



}
