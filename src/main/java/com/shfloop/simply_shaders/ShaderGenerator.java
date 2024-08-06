package com.shfloop.simply_shaders;

import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.files.FileHandle;
import finalforeach.cosmicreach.GameAssetLoader;
import finalforeach.cosmicreach.io.SaveLocation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class ShaderGenerator {

    public static String currentShaderPackFolder = "shaders/Test/";
    public static void copyShader(String baseShaderName) { //as long as the files are findablke through load asset we should be in buisness
        FileHandle input = GameAssetLoader.loadAsset(currentShaderPackFolder + baseShaderName, false);

        //TODO make this better and add zip support

       FileHandle testShader = GameAssetLoader.loadAsset("shaders/InternalShader/internal." + baseShaderName , false); //this should find the correct values
        input.copyTo(testShader);
    }
    public static void copyBaseShader(String baseShaderName) throws IOException {
        FileHandle input = Gdx.files.internal("shaders/" + baseShaderName); //might want to look into something better but this should work
        File f = new File(SaveLocation.getSaveFolderLocation() + "/mods/assets/shaders/InternalShader/internal." + baseShaderName);
        if (!f.exists()) {
            f.createNewFile();//
        }


        FileHandle testShader = GameAssetLoader.loadAsset("shaders/InternalShader/internal." + baseShaderName, false); //this should find the correct values

        input.copyTo(testShader);
    }
    public void copyContent(String a, String b)
            throws Exception
    {
        InputStream in = getClass().getResourceAsStream("/shaders/" + a);
        //instead go rthough the given file

        File f = new File(SaveLocation.getSaveFolderLocation() + "/mods/assets/shaders/" + b);
        f.getParentFile().mkdirs();// also need to make dirs
        f.createNewFile();//creates a file only if it doesnt exist exactly what i want
        //FileInputStream in = new FileInputStream();
        FileOutputStream out = new FileOutputStream(f);

        try {

            int n;

            // read() function to read the
            // byte of data
            while ((n = in.read()) != -1) {
                // write() function to write
                // the byte of data
                out.write(n);
            }
        }
        finally {
            if (in != null) {

                // close() function to close the
                // stream
                in.close();
            }
            // close() function to close
            // the stream
            if (out != null) {
                out.close();
            }
        }

    }
}
