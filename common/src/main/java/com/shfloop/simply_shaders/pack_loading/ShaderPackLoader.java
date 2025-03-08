package com.shfloop.simply_shaders.pack_loading;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.shfloop.simply_shaders.DynamicSkyInterface;
import com.shfloop.simply_shaders.Shadows;
import com.shfloop.simply_shaders.SimplyShaders;
import com.shfloop.simply_shaders.mixins.*;
import com.shfloop.simply_shaders.rendering.CompositeShader;
import com.shfloop.simply_shaders.rendering.FinalShader;
import com.shfloop.simply_shaders.settings.PackSettings;
import finalforeach.cosmicreach.GameAssetLoader;
import finalforeach.cosmicreach.entities.Entity;

import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.io.SaveLocation;

import finalforeach.cosmicreach.rendering.entities.instances.EntityModelInstance;

import finalforeach.cosmicreach.rendering.items.ItemModel;
import finalforeach.cosmicreach.rendering.items.ItemModelBlock;
import finalforeach.cosmicreach.rendering.items.ItemThingModel;
import finalforeach.cosmicreach.rendering.shaders.*;


import finalforeach.cosmicreach.util.Identifier;

import finalforeach.cosmicreach.world.*;
import org.lwjgl.opengl.GL32;


import java.io.IOException;

import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;


public class ShaderPackLoader {
    public static boolean shaderPackOn = false;
    public static String selectedPack;
    public static boolean isZipPack = false;
    public static Array<GameShader> shader1;
    public static final Map<String, String> shaderDefaultsMap = new HashMap<>();
    public static int compositeStartIdx =0;

    public static PackSettings packSettings;
    public static  IntArray drawBuffersUsed;



    public static void switchToShaderPack() {
        drawBuffersUsed = new IntArray(8);

        //check if folder is zip pack
        isZipPack = selectedPack.endsWith(".zip");

        //should init shaderpack for new array

        shaderPackOn = true;



        packSettings = new PackSettings(selectedPack);



        BlockPropertiesIDLoader.updateChunkTexBuf(); // need to load properties before initializing shaders
       shader1 = new Array<>();
        try {
            initShaderPack(shader1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //remesh?
        remeshAllRegions();
        remeashAllSkies();
        changeItemShader();
        updateEntityShader();
        remakeFBO();

    }
    public static void switchToDefaultPack() {
        drawBuffersUsed = null;
        shaderPackOn = false;
        isZipPack = false;
        //packSettings.saveUserPackSettings(); // dont think this is needed as long as packSettingsMenu always savees settings for the pack on exit
        packSettings = null;
        setDefaultShaders();
        remeshAllRegions();
        remeashAllSkies();
        changeItemShader();
        updateEntityShader();
        //remesh
        remakeFBO();
    }

    public static void remakeFBO() { // i think when a pack was in the middle of switching buffers and you switch to a pack withotu switching like default it can get stuck using the wrong texture for reading
        //need to remake fbo anyway when i do custom renderTexture scales/sizes
//        if (SimplyShaders.buffer != null) {
//            SimplyShaders.buffer.dispose();
//        }
//
//        try {
//            SimplyShaders.buffer = new RenderFBO(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
        SimplyShaders.initTextureHolder();
    }
    public static void remeshAllRegions() {
        if (InGame.getWorld() == null) {
            return;
        }
        //this needs to
        for (Zone zone: InGame.getWorld().getZones()) {
            for (Region reg: zone.getRegions()) {
                for (Chunk chunk: reg.getChunks()) {
                    chunk.flagForRemeshing(false); //hm setting this to true does not help makes it much worse
                }
            }

        }
    }
    public static void remeashAllSkies() {
        for (Sky sky: Sky.skyChoices) {
            sky.starMesh = null;
        }

        //Sky.skyChoices.set(0, new DynamicSky("base:dynamic_sky", "Dynamic_Sky"));
        //DynamicSky temp =   (DynamicSky) Sky.skyChoices.get(0);
        //Sky.currentSky = temp;
        //SkyInterface.getSkies().put("base:dynamic_sky", temp);
        if (Sky.currentSky instanceof DynamicSky) {
            ((DynamicSkyInterface)Sky.currentSky).setCurrentShader();
        }
        ((DynamicSkyInterface)Sky.skyChoices.first()).setCurrentShader();
    }
    public static void changeItemShader() {
    for(ItemModel model : ItemRendererInterfaceMixin.getModels().values()) {
        if (model instanceof ItemModelBlock) {
            ((ItemModelBlockInterface)model).setShader(Shadows.BLOCK_ENTITY_SHADER);
        } else if (model instanceof ItemThingModel) {
            ((ItemThingModelInterface)model).setProgram(ItemShader.DEFAULT_ITEM_SHADER);
        }

    } //2d items dont need to get new shader i just need to change entity shader for them to work


    }
    public static void updateEntityShader() {
        if (InGame.getWorld() != null) {
            for (Entity e: InGame.getLocalPlayer().getZone().getAllEntities()) {
                if (e.modelInstance instanceof EntityModelInstance) {
                    ((EntityModelInstanceInterface) e.modelInstance).setShader(EntityShader.ENTITY_SHADER);
                }
            }
        }


    }
    public static String[] tryDefualtShader(Identifier location) {

        //if the shader is in the map then try to assign a different shader
        //if it isnt in the map try loading from base shaders
        //all else fails throw runtimeerror

        String locNew = shaderDefaultsMap.get(location.getName());
        if (locNew != null) {
            System.out.println("Missing shader, defaulting to: " +  locNew);
            return loadShader(Identifier.of(locNew), true); //i want these to search through recursively
        }
        System.out.println("Missing shader, loading from base: " + location.toString());
        return loadShader(location,false);

    }


    // probably be better to use an inputstream of some kind
    //TODO errors in included files only come out as errors of the parent
    //add the child files names with the error
    public static String[] loadShader(Identifier location, boolean lookInShaderPack) {
        if (lookInShaderPack) {
            if (location.getName().contains("settings.glsl")) {
                return packSettings.getSettingsString(); //load the edited/saved version of the pack file from the settings
                //when loading/saving the settings i need to add the pack name to the filename so TestShadersv7settings.glsl or maybe .settings
            }
            Identifier temp = Identifier.of("shaderpacks/" + selectedPack, location.getName());
            //in case of shaders from pack for now i dont have defaulting so ill just crash
            try {
                return loadFromZipOrUnzipShaderPack(temp);
            } catch (InvalidPathException e) { //TODO not sure if zip packs will throw the same error if file isnt found
                //if it falls back to a default shader but doesnt find it it should crash
                return tryDefualtShader(location);
            }
         //TOdo make an assets map so packs dont keep loading the same common files that already have been found

        } else {
          return  GameAssetLoader.loadAsset(location).readString().split("\n");
        }


    }
    public static String[] loadFromZipOrUnzipShaderPack(String fileName, String packName) throws  InvalidPathException {
        Identifier location = Identifier.of("shaderpacks/" + packName, fileName);
        return loadFromZipOrUnzipShaderPack(location);
    }
    public static String[] loadFromZipOrUnzipShaderPack(String fileName) throws  InvalidPathException {
        Identifier location = Identifier.of("shaderpacks/" + selectedPack, fileName);
       return loadFromZipOrUnzipShaderPack(location);
    }
    //this cant have isZipPack because it needs to be pack independent
    public static String[] loadFromZipOrUnzipShaderPack(Identifier location) throws InvalidPathException {
        if (location.getNamespace().endsWith(".zip")) {
            Path zipFilePath = Paths.get(SaveLocation.getSaveFolderLocation(),"mods" ,location.getNamespace()); // in case of shaderpacks namespace will be shaderpacks/PACKNAME
            try (FileSystem fs = FileSystems.newFileSystem(zipFilePath, (ClassLoader) null)) {
                Path path = fs.getPath(location.getName());
                //System.out.println(path);
                return Files.readString(path).split("\n");
            }  catch (IOException e) {
                throw new InvalidPathException(e.getMessage(), "Could not read the file in zip: " );

            }
        } else {
            FileHandle handle = GameAssetLoader.loadAsset(location);
            if (handle == null) {
                throw new InvalidPathException(location.toPath(), " File Not Found"); //Game Asset Loader already prints error so i can just catch and continue
            }
           return handle.readString().split("\n");
        }
    }

/*

[19:17:51.784] [INFO] [com.shfloop.simply_shaders.Shadows]: Turning Shaders OFF
[19:18:16.640] [SEVERE] [java.lang.Throwable]: java.lang.IndexOutOfBoundsException: index can't be >= size: 7 >= 7
[19:18:16.640] [SEVERE] [java.lang.Throwable]: 	at com.badlogic.gdx.utils.Array.get(Array.java:155)
[19:18:16.640] [SEVERE] [java.lang.Throwable]: 	at com.shfloop.simply_shaders.ShaderPackLoader.setDefaultShaders(ShaderPackLoader.java:197)
[19:18:16.640] [SEVERE] [java.lang.Throwable]: 	at com.shfloop.simply_shaders.ShaderPackLoader.switchToDefaultPack(ShaderPackLoader.java:70)
[19:18:16.640] [SEVERE] [java.lang.Throwable]: 	at com.shfloop.simply_shaders.Shadows.cleanup(Shadows.java:145)
[19:18:16.641] [SEVERE] [java.lang.Throwable]: 	at com.shfloop.simply_shaders.ShaderSelectionMenu.applyShaderPackSelection(ShaderSelectionMenu.java:243)
[19:18:16.641] [SEVERE] [java.lang.Throwable]: 	at com.shfloop.simply_shaders.ShaderSelectionMenu.render(ShaderSelectionMenu.java:262)
[19:18:16.641] [SEVERE] [java.lang.Throwable]: 	at finalforeach.cosmicreach.BlockGame.render(BlockGame.java:126)
[19:18:16.641] [SEVERE] [java.lang.Throwable]: 	at com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window.update(Lwjgl3Window.java:387)
[19:18:16.641] [SEVERE] [java.lang.Throwable]: 	at com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application.loop(Lwjgl3Application.java:193)
[19:18:16.641] [SEVERE] [java.lang.Throwable]: 	at com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application.<init>(Lwjgl3Application.java:167)
[19:18:16.641] [SEVERE] [java.lang.Throwable]: 	at finalforeach.cosmicreach.lwjgl3.Lwjgl3Launcher.createApplication(Lwjgl3Launcher.java:103)
[19:18:16.641] [SEVERE] [java.lang.Throwable]: 	at finalforeach.cosmicreach.lwjgl3.Lwjgl3Launcher.main(Lwjgl3Launcher.java:91)
[19:18:16.641] [SEVERE] [java.lang.Throwable]: 	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103)
[19:18:16.641] [SEVERE] [java.lang.Throwable]: 	at java.base/java.lang.reflect.Method.invoke(Method.java:580)
[19:18:16.641] [SEVERE] [java.lang.Throwable]: 	at dev.crmodders.cosmicquilt.loader.CosmicGameProvider.launch(CosmicGameProvider.java:442)
[19:18:16.642] [SEVERE] [java.lang.Throwable]: 	at dev.crmodders.cosmicquilt.loader.knot.Knot.launch(Knot.java:23)
[19:18:16.642] [SEVERE] [java.lang.Throwable]: 	at dev.crmodders.cosmicquilt.loader.knot.KnotClient.main(KnotClient.java:7)

 */



    private static void setDefaultShaders() {
        //idk why this is crashing its not getting 8 shaders somehow in allShaders so 7 is out of bounds
       Array<GameShader> allShaders = GameShaderAccessor.getShader();
        ChunkShader.DEFAULT_BLOCK_SHADER = (ChunkShader) allShaders.get(0);
        ChunkShader.WATER_BLOCK_SHADER = (ChunkShader) allShaders.get(1);
        SkyStarShader.SKY_STAR_SHADER = (SkyStarShader) allShaders.get(2);
        SkyShader.SKY_SHADER = (SkyShader) allShaders.get(3);

        EntityShader.ENTITY_SHADER = (EntityShader) allShaders.get(4);
        //for now dont f with death screen (5) switched to spritebatchShader
        ItemShader.DEFAULT_ITEM_SHADER = (ItemShader) allShaders.get(6);    
        VisualTextShader.TEXT_SHADER = (VisualTextShader) allShaders.get(7);
        ParticleShader.PARTICLE_SHADER = (ParticleShader) allShaders.get(8);
        FinalShader.DEFAULT_FINAL_SHADER = (FinalShader) allShaders.get(9);
        Shadows.BLOCK_ENTITY_SHADER = (ChunkShader) ChunkShader.DEFAULT_BLOCK_SHADER;
    }

    //create the new array based onthe shaderpack folder
    //
    private static void initShaderPack(Array<GameShader> packShaders) throws IOException {

        Array<GameShader> allShaders = GameShaderAccessor.getShader();


        ChunkShader.DEFAULT_BLOCK_SHADER = new ChunkShader(Identifier.of("shaders/chunk.vert.glsl"), Identifier.of("shaders/chunk.frag.glsl"));
        packShaders.add(allShaders.pop()); //i dont want to infinitly add shaders to allshaders

        ChunkShader.WATER_BLOCK_SHADER = new ChunkShader(Identifier.of("shaders/chunk-water.vert.glsl"), Identifier.of("shaders/chunk-water.frag.glsl"));
        packShaders.add(allShaders.pop());

        SkyStarShader.SKY_STAR_SHADER = new SkyStarShader(Identifier.of("shaders/sky-star.vert.glsl"), Identifier.of("shaders/sky-star.frag.glsl"));
        packShaders.add(allShaders.pop());

        SkyShader.SKY_SHADER =  new SkyShader(Identifier.of("shaders/sky.vert.glsl"), Identifier.of("shaders/sky.frag.glsl"));
        packShaders.add(allShaders.pop());

        EntityShader.ENTITY_SHADER =  new EntityShader(Identifier.of("shaders/entity.vert.glsl"), Identifier.of("shaders/entity.frag.glsl"));
        packShaders.add(allShaders.pop());

        packShaders.add(allShaders.get(5)); //TODO

        ItemShader.DEFAULT_ITEM_SHADER = new ItemShader(Identifier.of("shaders/item_shader.vert.glsl"), Identifier.of("shaders/item_shader.frag.glsl"));
        packShaders.add(allShaders.pop());


        FinalShader.DEFAULT_FINAL_SHADER =  new FinalShader(Identifier.of("shaders/final.vert.glsl"), Identifier.of("shaders/final.frag.glsl"));
        packShaders.add(allShaders.pop());

        Shadows.BLOCK_ENTITY_SHADER = new ChunkShader(Identifier.of("shaders/blockEntity.vert.glsl"), Identifier.of("shaders/blockEntity.frag.glsl"));
        packShaders.add(allShaders.pop());

        //add the rest from the pack  shadow , shadowentity, ? composite0-8 as many as given

        if (BlockPropertiesIDLoader.packEnableShadows) {

            Shadows.SHADOW_CHUNK = new ChunkShader(Identifier.of("shaders/shadowChunk.vert.glsl"), Identifier.of("shaders/shadowChunk.frag.glsl"));
            packShaders.add(allShaders.pop());

            Shadows.SHADOW_ENTITY = new EntityShader(Identifier.of("shaders/shadowEntity.vert.glsl"), Identifier.of("shaders/shadowEntity.frag.glsl"));
            packShaders.add(allShaders.pop());

        }


        //load composite and settings here maybe
        //composite shaders start at 9
        compositeStartIdx = packShaders.size ;
        System.out.println("Composite start IDX " + compositeStartIdx);

        if (isZipPack) { //TODO redo this so the pack can specify which composite it wants to use so i can enable /disable them without removing them from pack
            Path zipFilePath = Paths.get(SaveLocation.getSaveFolderLocation(), "/mods/shaderpacks/" + selectedPack);
            try (FileSystem fs = FileSystems.newFileSystem(zipFilePath, (ClassLoader) null)) {
                for (int i = 0; i < 8; i++) {
                    String compositeName = "shaders/composite" + i;
                    Path path = fs.getPath( compositeName + ".frag.glsl");
                    //will cause invalid pathexception if it doesnt exits
                    if (Files.exists(path)) {
                        new CompositeShader(Identifier.of(compositeName + ".vert.glsl"), Identifier.of(compositeName + ".frag.glsl"));
                        packShaders.add(allShaders.pop());
                    } else {
                        break;
                    }

                }
            } catch (IOException e) {
                throw new RuntimeException("ZIP fs cant be created");
            }
            catch (InvalidPathException e) {
                //means composite ended
                //safely exit
            }
        } else {
            for (int i = 0; i < 8; i++ ) {
                String compositeName = "shaders/composite" + i;
                System.out.println(compositeName);
                FileHandle compositeTest = Gdx.files.absolute(SaveLocation.getSaveFolderLocation() + "/mods/shaderpacks/" + ShaderPackLoader.selectedPack +  "/" + compositeName + ".frag.glsl");

                if (compositeTest.exists()) {
                    new CompositeShader(Identifier.of(compositeName + ".vert.glsl"), Identifier.of(compositeName + ".frag.glsl"));
                    packShaders.add(allShaders.pop());
                } else {

                    break;
                }
            }

        }

    }

    static {
        shaderDefaultsMap.put("shaders/blockEntity.frag.glsl","shaders/chunk.frag.glsl");
        shaderDefaultsMap.put("shaders/blockEntity.vert.glsl","shaders/chunk.vert.glsl");
    }
    public static void addDrawBuffer(int attachmentNum) {
        if (attachmentNum - GL32.GL_COLOR_ATTACHMENT0 < 0 || attachmentNum - GL32.GL_COLOR_ATTACHMENT0 >= 8) {
            throw new RuntimeException("Attacment number incorrect " + attachmentNum);
        }
        if (!drawBuffersUsed.contains(attachmentNum)) {
            drawBuffersUsed.add(attachmentNum);
        }
    }
}
