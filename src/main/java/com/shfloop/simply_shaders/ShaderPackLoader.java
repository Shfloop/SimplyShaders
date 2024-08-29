package com.shfloop.simply_shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import finalforeach.cosmicreach.GameAssetLoader;
import finalforeach.cosmicreach.io.SaveLocation;
import finalforeach.cosmicreach.rendering.shaders.ChunkShader;
import finalforeach.cosmicreach.rendering.shaders.SkyStarShader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Scanner;

public class ShaderPackLoader {
    //called from GameShaderMixin to load the shader based off shader selection (loaded from settings)
    //needs to get the String of whatever file it gets
    //im fine with returning a string because i only want to load shaderPacks
    //can either be from GDx.classpath or Gdx.absolute (unzipped folder) or a zipped folder in mods/assets/shaders

    private static FileSystem zipFile;
    public static boolean shaderPackOn = false;
    public static String selectedPack;
    public static boolean isZipPack = false;

    //also want this to init the shaders
    //probably be easier to keep track of shader arrays here




    //not sure what it does if i call .split so it might eb better
    //probably be better to use an inputstream of some kind
    public static String[] loadShader(String fileName) { //wil just be the shader name ex chunk.frag.glsl no folders

        // if its loading a pack it will start with "/shaders/"
        // else im going to be loading hte jar shader
        if (ShaderPackLoader.shaderPackOn) { //it true needs to load from mods assets shaders/ packname/ program/"shader"
            //loading a custom pack shader
            //need to check if its a zip folder
            //shaders will be in program folder
            //take the current selected file handle - i need to do something seperate if its a zip[ folder
            if (!isZipPack) { // load from regular gdx file absolute
                //cant use load asset caues it will add it to the all assets which would interfere with getting vanillal stuff
              FileHandle unzippedFile = Gdx.files.absolute(SaveLocation.getSaveFolderLocation() + "/mods/assets/shaders/" + ShaderPackLoader.selectedPack +  "/" + fileName);
                System.out.println(unzippedFile);
              return unzippedFile.readString().split("\n");
            } else {
                //TODO replace with program
                Path zipFilePath = Paths.get(SaveLocation.getSaveFolderLocation(), "/mods/assets/shaders/" + selectedPack);
                try (FileSystem fs = FileSystems.newFileSystem(zipFilePath, (ClassLoader) null)) {
                    Path path = fs.getPath("ShadersV3", fileName);
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
            //load from internal
            //the base game shaders should still work
            //but idc
            if ( GameAssetLoader.ALL_ASSETS.containsKey(fileName)) { // this would only get base shaders if shaderpack is on
                return GameAssetLoader.ALL_ASSETS.get(fileName).readString().split("\n");
            }
            //this only needs to load final.frag/final.vert
            FileHandle handle = Gdx.files.classpath("baseShaders/" + fileName); //classpath just does resourceasStram
            System.out.println(handle.path());
            if (handle.exists()) { //could just look for default shader as well
                System.out.println(" from resources");
                GameAssetLoader.ALL_ASSETS.put(fileName, handle);
                return handle.readString().split("\n");
            } else {
                System.out.println(" from jar");
                FileHandle fileFromJar = Gdx.files.internal("shaders/" + fileName);
                GameAssetLoader.ALL_ASSETS.put(fileName, fileFromJar);
                return fileFromJar.readString().split("\n");
            }
            //System.out.println(handle.path());
            //System.out.println(SaveLocation.getSaveFolderLocation())

        }


    }

    public static void loadDefaultShaders() {
        //
        //go in specific order
        //just needs to create the shader and loadShader will be called
    }
    public static void loadUserShaders(String folderName)  {
        if (folderName.endsWith(".zip")) {

            Path zipFilePath = Paths.get(SaveLocation.getSaveFolderLocation(), "/mods/assets/shaders/" + folderName);
            try (FileSystem fs = FileSystems.newFileSystem(zipFilePath, (ClassLoader) null)) {
                    //change to program
                zipFile = fs; // just want to create teh zipfilesystem
                //could make this a program test to see if theres an appropriate file structure or maybe load settings
                  //  Path test = fs.getPath("ShadersV3","chunk.frag.glsl");
                  //  System.out.println("Created fileSystem reading chunkfrag");
                //Files.readString(test);
                //System.out.println("Completed");
                //for now i read set file
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            zipFile = null;
        }

        //load all shaders

    }

    public static void initShaders() {
        //
        //ChunkShader.
        ChunkShader.DEFAULT_BLOCK_SHADER = new ChunkShader("chunk.vert.glsl", "chunk.frag.glsl");
        ChunkShader.WATER_BLOCK_SHADER = new ChunkShader("chunk-water.vert.glsl", "chunk-water.frag.glsl");
        ChunkShader.noiseTex = new Texture(GameAssetLoader.loadAsset("textures/special/noise.png"), true);
        ChunkShader.noiseTex.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);


        //Sky star Shader
        SkyStarShader.SKY_STAR_SHADER = new SkyStarShader("sky-star.vert.glsl", "sky-star.frag.glsl");
    }
}
