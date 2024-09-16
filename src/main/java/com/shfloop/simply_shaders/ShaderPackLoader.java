package com.shfloop.simply_shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import com.badlogic.gdx.utils.Array;
import com.shfloop.simply_shaders.mixins.*;
import com.shfloop.simply_shaders.rendering.FinalShader;
import finalforeach.cosmicreach.GameAssetLoader;
import finalforeach.cosmicreach.entities.Entity;

import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.io.SaveLocation;

import finalforeach.cosmicreach.rendering.entities.EntityModelInstance;

import finalforeach.cosmicreach.rendering.items.ItemModel;
import finalforeach.cosmicreach.rendering.items.ItemModelBlock;
import finalforeach.cosmicreach.rendering.items.ItemThingModel;
import finalforeach.cosmicreach.rendering.shaders.*;
import finalforeach.cosmicreach.util.AnsiColours;

import finalforeach.cosmicreach.util.Identifier;

import finalforeach.cosmicreach.world.*;


import java.io.IOException;

import java.io.PrintStream;
import java.nio.file.*;


public class ShaderPackLoader {
    //called from GameShaderMixin to load the shader based off shader selection (loaded from settings)
    //needs to get the String of whatever file it gets
    //im fine with returning a string because i only want to load shaderPacks
    //can either be from GDx.classpath or Gdx.absolute (unzipped folder) or a zipped folder in mods/assets/shaders

    public static boolean shaderPackOn = false;
    public static String selectedPack;
    public static boolean isZipPack = false;
    public static Array<GameShader> shader1;
    public static Array<GameShader> shader2;
    private static boolean useArray2 = false;


    //also want this to init the shaders
    //probably be easier to keep track of shader arrays here

    public static void switchToShaderPack() {
        //check if folder is zip pack
        isZipPack = selectedPack.endsWith(".zip");

        //should init shaderpack for new array
       // useArray2 = shaderPackOn; //wont work cause shader 1 wont be used after we start using shader2
        shaderPackOn = true;
       // initShaderPack(useArray2 ? shader2 : shader1); // if shaderpackon is true when switching it means we are switching from a shader pack so ive got to use shader2 so the game doesnt crash when remeshing
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

    }
    public static void switchToDefaultPack() {
        shaderPackOn = false;
        isZipPack = false;
        useArray2 = false;
        setDefaultShaders();
        remeshAllRegions();
        changeItemShader();
        updateEntityShader();
        //remesh
    }
    public static void remeshAllRegions() {
        if (InGame.world == null) {
            return;
        }
        //this needs to
        for (Zone zone: InGame.world.getZones()) {
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

        Sky.skyChoices.set(0, new DynamicSky("base:dynamic_sky", "Dynamic_Sky"));
        DynamicSky temp =   (DynamicSky) Sky.skyChoices.get(0);
        Sky.currentSky = temp;
        SkyInterface.getSkies().put("base:dynamic_sky", temp);
    }
    public static void changeItemShader() { // i think i can do this without remeshing everything
    for(ItemModel model : ItemRendererInterfaceMixin.getModels().values()) { // this just needs to go through held items
        if (model instanceof ItemModelBlock) {
            ((ItemModelBlockInterface)model).setShader(Shadows.BLOCK_ENTITY_SHADER); //Maybe this works
        } else if (model instanceof ItemThingModel) {
            ((ItemThingModelInterface)model).setProgram(ItemShader.DEFAULT_ITEM_SHADER);
        }

    } //2d items dont need to get new shader i just need to change entity shader for them to work


    }
    public static void updateEntityShader() {
        if (InGame.world != null) {
            for (Entity e: InGameInterface.getLocalPlayer().getZone(InGameInterface.getWorld()).allEntities) {
                if (e.modelInstance instanceof EntityModelInstance) {
                    ((EntityModelInstanceInterface) e.modelInstance).setShader(EntityShader.ENTITY_SHADER);
                }
            }
        }


    }

    //not sure what it does if i call .split
    // probably be better to use an inputstream of some kind
    public static String[] loadShader(Identifier location) { //wil just be the shader name ex chunk.frag.glsl no folders
        //if shaderpack on then it should be base location so i basicaaly just need to replace the namespace with folder name
        // if its loading a pack it will start with "/shaders/"
        // else im going to be loading hte jar shader
        if (ShaderPackLoader.shaderPackOn) { //it true needs to load from mods assets shaders/ packname/ program/"shader"
            //loading a custom pack shader
            //need to check if its a zip folder
            //shaders will be in program folder
            //take the current selected file handle - i need to do something seperate if its a zip[ folder

            System.out.println(location.getName());
            //TOdo make an assets map so packs dont keep loading the same common files that already have been found
            if (!isZipPack) { // load from regular gdx file absolute
                //cant use load asset caues it will add it to the all assets which would interfere with getting vanillal stuff
              FileHandle unzippedFile = Gdx.files.absolute(SaveLocation.getSaveFolderLocation() + "/mods/assets/shaders/" + ShaderPackLoader.selectedPack +  "/" + location.getName()); //ive excluded /shaders when creating shaderpack shaders
                System.out.println(unzippedFile);
              return unzippedFile.readString().split("\n");
            } else {
                //TODO replace with program
                Path zipFilePath = Paths.get(SaveLocation.getSaveFolderLocation(), "/mods/assets/shaders/" + selectedPack);
                try (FileSystem fs = FileSystems.newFileSystem(zipFilePath, (ClassLoader) null)) {
                    Path path = fs.getPath(location.getName());
                        System.out.println(path);
                    return Files.readString(path).split("\n");
                } catch (InvalidPathException e) {
                    //crash for now but FIXME
                    throw new RuntimeException("FILE doesnt exist in zip");
                } catch (IOException e) {
                    throw new RuntimeException("Could not read the file in zip");

                }
            }
        } else {
          return  GameAssetLoader.loadAsset(location).readString().split("\n");
        }


    }





    private static void setDefaultShaders() {
       Array<GameShader> allShaders = GameShaderInterface.getShader();
        ChunkShader.DEFAULT_BLOCK_SHADER = (ChunkShader) allShaders.get(0);
        ChunkShader.WATER_BLOCK_SHADER = (ChunkShader) allShaders.get(1);
        SkyStarShader.SKY_STAR_SHADER = (SkyStarShader) allShaders.get(2);
        SkyShader.SKY_SHADER = (SkyShader) allShaders.get(3);

        EntityShader.ENTITY_SHADER = (EntityShader) allShaders.get(4);
        //for now dont f with death screen (5)
        ItemShader.DEFAULT_ITEM_SHADER = (ItemShader) allShaders.get(6);
        FinalShader.DEFAULT_FINAL_SHADER = (FinalShader) allShaders.get(7);
        Shadows.BLOCK_ENTITY_SHADER = (ChunkShader) ChunkShader.DEFAULT_BLOCK_SHADER;
    }

    //create the new array based onthe shaderpack folder
    //
    private static void initShaderPack(Array<GameShader> packShaders) throws IOException {
        ///this is stupid but idk what else to do
        // i need to create a new array whena shaderpack is switched
        //ill have 3 arrays of shaders
        //default which i keep having to pop
        //shaders 1
        //and then shadesr 2 which will only be used when switching between shaders packs so the world doesnt crash on reload
        Array<GameShader> allShaders = GameShaderInterface.getShader();


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


        FinalShader.DEFAULT_FINAL_SHADER =  new FinalShader(Identifier.of("shaders/final.vert.glsl"), Identifier.of("shaders/final.frag.glsl"),  false);
        packShaders.add(allShaders.pop());

        Shadows.BLOCK_ENTITY_SHADER = new ChunkShader(Identifier.of("shaders/blockEntity.vert.glsl"), Identifier.of("shaders/blockEntity.frag.glsl"));
        packShaders.add(allShaders.pop());

        //add the rest from the pack  shadow , shadowentity, ? composite0-8 as many as given


        Shadows.SHADOW_CHUNK = new ChunkShader(Identifier.of("shaders/shadowChunk.vert.glsl"), Identifier.of("shaders/shadowChunk.frag.glsl"));
        packShaders.add(allShaders.pop());

        Shadows.SHADOW_ENTITY = new EntityShader(Identifier.of("shaders/shadowEntity.vert.glsl"), Identifier.of("shaders/shadowEntity.frag.glsl"));
        packShaders.add(allShaders.pop());

        //load composite and settings here maybe
        //composite shaders start at 9
        if (isZipPack) {
            Path zipFilePath = Paths.get(SaveLocation.getSaveFolderLocation(), "/mods/assets/shaders/" + selectedPack);
            try (FileSystem fs = FileSystems.newFileSystem(zipFilePath, (ClassLoader) null)) {
                for (int i = 0; i < 8; i++) {
                    String compositeName = "shaders/composite" + i;
                    Path path = fs.getPath( compositeName + ".frag.glsl");
                    //will cause invalid pathexception if it doesnt exits
                    if (Files.exists(path)) {
                        new FinalShader(Identifier.of(compositeName + ".vert.glsl"), Identifier.of(compositeName + ".frag.glsl"), true);
                        packShaders.add(allShaders.pop());
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
                FileHandle compositeTest = Gdx.files.absolute(SaveLocation.getSaveFolderLocation() + "/mods/assets/shaders/" + ShaderPackLoader.selectedPack +  "/" + compositeName + ".frag.glsl");

                if (compositeTest.exists()) {
                    new FinalShader(Identifier.of(compositeName + ".vert.glsl"), Identifier.of(compositeName + ".frag.glsl"), true);
                    packShaders.add(allShaders.pop());
                } else {

                    break;
                }
            }

        }

    }
}
