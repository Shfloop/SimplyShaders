package com.shfloop.simply_shaders.pack_loading;

import com.badlogic.gdx.utils.StringBuilder;
import com.shfloop.simply_shaders.TexBufferContainer;
import com.shfloop.simply_shaders.pack_loading.ShaderPackLoader;
import finalforeach.cosmicreach.rendering.shaders.ChunkShader;

import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.HashMap;

public class BlockPropertiesIDLoader {

    //in charge of loading properties file
    //parsing it
    //then changing the ChunkShader texbuffer ids to match the properties file

    public static float shaderBlockGroupId;

    public static int baseGeneratedBlockID = 0;

    public static HashMap<String, Integer > baseGeneratedBlockIDMap = new HashMap<>();
    public static ArrayList<String> baseGeneratedBlockIDArray = new ArrayList<>();
    public static HashMap<String, Integer > shaderBlockIDMap = new HashMap<>();
    public static boolean packEnableShadows = true;


    public static void updateChunkTexBuf() {

        shaderBlockIDMap = new HashMap<>(); // clear the hashmap each time pack is loaded
        //TODO the base game ChunkTexBufferis changed during base game aswell look into changin git at shader runtime
        loadProperties("block.properties");



        float[] items = ChunkShader.faceTexBufFloats.items;

        int currentDefaultID = 0;
        int shaderGroupID = shaderBlockIDMap.getOrDefault(baseGeneratedBlockIDArray.get(currentDefaultID ), 0);
        for (int i = 11; i < items.length; i+= 12) {
            //instaed of using the full 32 im just going to use bits in mantissa so 23 and ill use FF for block group and 7FFF for base ID
            int temp = (int)items[i] >> 8;
            if (temp != currentDefaultID) {
                currentDefaultID = temp;
                shaderGroupID = shaderBlockIDMap.getOrDefault(baseGeneratedBlockIDArray.get(currentDefaultID ), 0);

            }
            items[i] = (float) ((currentDefaultID << 8) | (shaderGroupID & 255));
        }

    }
    private static boolean putBlockIDinMap(String line, int ID) {
        if (ID <= 0) {
            throw new RuntimeException("Invalid Block ID in block.properties");
        }

        for (String block: line.split(" ")) {
            String trimmed = block.trim();
            if (trimmed.contains("\\")) {
                //add " \" and continue on next line
                return true;
            } else {
                if (shaderBlockIDMap.getOrDefault(trimmed, -1) != -1) {
                    //means its a duplicate and the shader shouldnt continue
                    throw new RuntimeException("Duplicate Block entry in block.properties: " + trimmed);
                }
                //check if the block is an empty string or just space
                if (!trimmed.isEmpty()) {
                    shaderBlockIDMap.put(trimmed, ID);
                }

            }
        }
        return false;
    }

    public static void loadProperties(String filePath) {
        packEnableShadows = false; //default to false if it cant find a pack.shadows
        try {
            String[] contents = ShaderPackLoader.loadFromZipOrUnzipShaderPack(filePath);
            //in the future i shold probably add compilation directives
            boolean continueNextLine = false;
            int ID = -1;
            for (String line: contents) {
                if (line.startsWith("#")) {
                    continue;
                }
                if (continueNextLine) {
                    if (putBlockIDinMap(line, ID)) {
                        continue;
                    }
                    continueNextLine = false;
                    continue;
                }

                if (line.startsWith("block.")) {
                    int blockIDStart =  line.indexOf('.');// might be unnecessary  but not sure/
                    int blockIDEnd = line.indexOf('=');
                    String blockID = line.substring(blockIDStart + 1, blockIDEnd);
                    ID = Integer.parseInt(blockID);


                    if (putBlockIDinMap(line.substring(blockIDEnd + 1), ID)) {
                        continueNextLine = true;
                        continue;
                    }
                } else if (line.startsWith("pack.shadows")) {
                    int blockIDEnd = line.indexOf('=');
                    String test = line.substring(blockIDEnd + 1);
                    packEnableShadows = test.trim().equals("true");
                }
            }
        } catch (InvalidPathException e) {

            return; //if the file isnt found then dont worry about updating block properties
        }



    }


    public static int chunkFloatArrayBinarySearch(int x) {
        System.out.println(ChunkShader.faceTexBufFloats.items.length);

        int faceSize = TexBufferContainer.NUM_FLOATS_PER_FACE_UVTEXBUFF;
        float[] array = ChunkShader.faceTexBufFloats.items;
        int high = ChunkShader.faceTexBufFloats.items.length / faceSize - 1;
        int low = 0;



        while (low <= high) {
            int mid = low + (high - low) / 2;
            int IDMid = mid * faceSize + faceSize -1;
            int value = Float.floatToRawIntBits(array[IDMid]) >> 16;
            if(value == x) {
                return mid;
            }
            if (value < x) {
                low = mid + 1;
            } else {
                high = mid -1;
            }

        }
        return -1;
    }
    public static void findValueInChunkBuf(int x) {
        float[] items = ChunkShader.faceTexBufFloats.items;
        if (items.length == 0) {
            return;
        } else {

            StringBuilder buffer = new StringBuilder(32);
            buffer.append(Float.floatToRawIntBits(items[ 11]) >> 16);

            for(int i = 1; i < items.length / 12; ++i) {
                int value = Float.floatToRawIntBits(items[i* 12 + 11]) >> 16;
                buffer.append(',');
                buffer.append(value);
            }

            System.out.println(buffer);
        }



        int startIndex = chunkFloatArrayBinarySearch(x);
        System.out.println(startIndex);
        System.out.println(Float.floatToRawIntBits(items[startIndex* 12 + 11]) >> 16);
        int lowerIDX = startIndex;
        int upperIDX = startIndex;

        for (int i = startIndex; i >= 0 ; i--) {
            int value = Float.floatToRawIntBits(items[i* 12 + 11]) >> 16;
            if (value != x) {
                lowerIDX = i + 1;
                break;
            }
            if (i== 0) {
                lowerIDX = 0;
                break;
            }
        }
        int length = items.length / 12 - 1;
        for (int i = startIndex; i <= length ; i++) {
            int value = Float.floatToRawIntBits(items[i* 12 + 11]) >> 16;
            if (value != x) {
                upperIDX = i - 1;
                break;
            }
            if (i == length) {
                upperIDX = length;
                break;
            }
        }

        System.out.println("LOWER " + getValue(lowerIDX, items) + " UPPER " + getValue(upperIDX, items));
        System.out.println("LOWER " + getValue(lowerIDX + 1, items) + " UPPER " + getValue(upperIDX + 1, items));
        System.out.println("LOWER " + getValue(lowerIDX - 1, items) + " UPPER " + getValue(upperIDX - 1, items));

    }


    public static int getValue(int index, float[] items) {

        return  Float.floatToRawIntBits(items[index* 12 + 11]) >> 16;
    }

}
