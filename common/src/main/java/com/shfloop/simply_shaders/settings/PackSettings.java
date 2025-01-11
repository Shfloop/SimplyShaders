package com.shfloop.simply_shaders.settings;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.IntArray;
import com.shfloop.simply_shaders.SimplyShaders;
import com.shfloop.simply_shaders.pack_loading.ShaderPackLoader;
import finalforeach.cosmicreach.io.SaveLocation;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.util.*;

public class PackSettings {
    public String packName;
    public  HashMap<String, ShaderPackSetting> definedSettingsMap;

    public  Properties packProperties;

    private  HashMap<String, Float> packSavedSettingsMap;
    private String[] settingsFile;

    public final IntArray disableBufferClearing = new IntArray();
    public final HashMap<String,Float> bufferTexturesScale = new HashMap<>();

    public PackSettings(String packName) {
        if (packName == null) {
            SimplyShaders.LOGGER.info("PACK SETTINGS NAME IS NULL");
            throw new RuntimeException("PACK SETTINGS NAME IS NULL");
        }
        this.packName = packName;
        this.loadUserPackSettings(); //needs to happen first because load glsl will check the saved settings to see if a defualt value should be overwritten
        this.loadShaderProperties();
        this.loadGlslSettings();
    }




    public String[] getSettingsString() {
        //take all the current settings objects and write them out into a basically txt file so it can be used as includes to shaderfiles
        if (settingsFile != null) {
            return settingsFile;
        }
        String[] settingLines = new String[definedSettingsMap.size()];
        int idx = 0;
        for (ShaderPackSetting sval : definedSettingsMap.values()) {
            settingLines[idx++] = sval.toString();// the game shader will append the newline for us

        }
        settingsFile = settingLines;
        return settingsFile;
    }

    private  void loadShaderProperties() {

        packProperties = new Properties();
        try {
            String temp = String.join("\n", ShaderPackLoader.loadFromZipOrUnzipShaderPack("shader.properties", packName));
            InputStream contents = new ByteArrayInputStream(temp.getBytes(StandardCharsets.UTF_8));

            packProperties.load(contents);

        }
        catch (InvalidPathException e) { //it should be fine to continue but the settings page wont load
            return;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        //temporary to load some properties
        //so each buffer texture can have a setting where it has the resolution
        //need a pack setting that runs
        //todo might want to add custom value for each widht and height
        for (int i = 0; i < 8; i++) {
            String bufferName = "colorTex" + i;
            float value = Float.parseFloat(packProperties.getProperty("size.buffer." + bufferName,"-1"));
            if(value > 0 ) {// i guess the values can be bigger than one not sure why you would want to though
                //use the default 1.0
                this.bufferTexturesScale.put(bufferName, value);
            }


        }



    }
    private  void loadGlslSettings() {
        //i should really make a locate function in shaderpackloader so the file structure doesnt have to be exact

        definedSettingsMap = new HashMap<>();
        try {
            String[] settingsFile = ShaderPackLoader.loadFromZipOrUnzipShaderPack("settings.glsl", packName);
            for (String line: settingsFile) {
                //create the settingsObj frmo each line
                //add it to a hashmap with the key being the string name
                //need to parse the line
                //lines structered const "type" "name" = "defaultValue" //[0,1,2,3,4,5,6]
                //or it could be #define "Name" <optionally>"defaultValue //"comment" [0,15,25,35]
                // for ifDef uses #define might be commented out but i still want that to be enabled
                //define has to be lowercase
                //each line might not have a setting
                System.out.println(line);
                int defineLineIdx = line.indexOf("#define");
                int firstCommentIdx = line.indexOf("//");
                if (defineLineIdx >= 0) {
                    int defineEndIdx = defineLineIdx + 7;
                    int spaceIdx = line.indexOf(" ", defineEndIdx + 1 );
                    //make sure if theres no spaces after the settingName it should just do the lineLength
                    String settingName = line.substring(defineEndIdx , (spaceIdx == -1 ? line.length(): spaceIdx)).trim(); // this is dumb need to loop to find end of whitespace

                    if (firstCommentIdx == -1) {
                        //NO comment is found
                        //treat it as an active toggle define setting


                        //the line is a toggle that is toggled on by default
                        //TOGGLE DEFAULT ON
                        addToggleSetting(settingName, true);
                        System.out.println("TOGGLE ON SETTING");


                    } else
                    if (firstCommentIdx < defineLineIdx) { //means the define is commented out and means its just an ifdef define no values
                        //create toggle setting default off
                        if(line.indexOf('[') != -1) {
                            continue; //means its a commented out slider/values
                            //shouldnt be used but if someone wants to remove a setting by commenting it should be allowed
                        }
                        //TOGGLE DEFAULT OFF
                        addToggleSetting(settingName, false);
                        System.out.println("TOGGLE OFF SETTING");
                    } else {
                        //continue checking
                        //only think left to check is if its its toggle
                        //both slider and cycle have the same data


                        for (int i = defineEndIdx;  i < line.length(); i++) {
                            if(line.charAt(i) >= 20) {
                                defineEndIdx = i;
                                break;
                            }
                        }
                        System.out.println("name: " + settingName);
                        //test if the default value is there
                        if (firstCommentIdx <= defineEndIdx + settingName.length()) {
                            throw new RuntimeException("ERM what");
                        }
                        String defaultValue = line.substring(defineEndIdx + settingName.length() + 1, firstCommentIdx).trim();
                        if (defaultValue.isEmpty()) {
                            //means its a toggle setting
                            //TOGGLE ON WITH A COMMENT
                            addToggleSetting(settingName, true);
                            System.out.println("TOGGLE ON SETTING 2");
                        } else {
                            float defaultParseValue ;
                            try {
                                defaultParseValue = Float.parseFloat(defaultValue);
                            } catch (NumberFormatException e) {
                                throw new RuntimeException(" Default cant parse" + e);
                            }
                            int valuesStartIdx = line.indexOf("[", firstCommentIdx);
                            if (valuesStartIdx <0) {
                                throw new RuntimeException("NO VALUES");
                            }

                            String[] stringValues = line.substring(valuesStartIdx).split(" ");
                            System.out.println(Arrays.toString(stringValues));

                            ShaderPackSetting data = getShaderPackSetting(stringValues, defaultParseValue, settingName);
                            //todo change this to use the lang key so the glsl and pack can use different names
                            definedSettingsMap.put(data.name, data);
                        }
                    }
                }
            }

        }
        catch (InvalidPathException e) { //it should be fine to continue but the settings page wont load
            throw new RuntimeException("NOT SETUP " + e);
        }


    }
    private void addToggleSetting(String settingName, boolean isActive) {
        int defaultValue = isActive ? 1 : 0;

        ShaderPackSetting data = new ShaderPackSetting(defaultValue, settingName,  getBooleanSetting(settingName));
        //TODO USE LANG KEY
        this.definedSettingsMap.put(data.name, data);
    }
    //TODO move this to ShaderPackSetting it doesnt need to be in PackSettings
    //ALSO
    private int getBooleanSetting(String settingName) {
        Float val = this.packSavedSettingsMap.get(settingName);
        if (val != null) {
            return  val.intValue();
        }
        return -1;
    }
    private @NotNull ShaderPackSetting getShaderPackSetting(String[] stringValues, float defaultParseValue, String settingName) {
        FloatArray settingValues = new FloatArray(stringValues.length);// should be enough
        boolean endOfValues = false;
        for (String val: stringValues) {
            String stringFloat;
            if (val.contains("[")) {
                stringFloat = val.replace("[", "");
                if (stringFloat.isEmpty()) {
                    //means there is a space at the end of last Value and the size is incorrect
                    break;
                }
            } else if (val.contains("]")) {
                endOfValues = true;
                stringFloat = val.replace("]", "");
                if (stringFloat.isEmpty()) {
                    //means there is a space at the end of last Value and the size is incorrect
                    break;
                }

            } else {
                stringFloat = val;
            }

            //add the parsed value to a float Array
            float parsedVal;
            try {
                parsedVal = Float.parseFloat(stringFloat);

            } catch (NumberFormatException e) {
                throw new RuntimeException("YO NUMBER CANT PARSE" + e);
            }

            settingValues.add(parsedVal);
            if (endOfValues) {
                break;
            }
        }

        //create the setting
        return new ShaderPackSetting(defaultParseValue, settingName,settingValues, ShaderPackSetting.SettingType.Slider, getSavedSettingIdx(settingName, settingValues));
    }
    private int getSavedSettingIdx(String settingName, FloatArray values) {
        //if the saved setting exists get the value if not return -1
        Float val = this.packSavedSettingsMap.get(settingName);
        if (val != null) {
            SimplyShaders.LOGGER.info("FOUND SAVE {}: , {}", settingName, val);
            for(int i = 0; i < values.size; i++) {
                if (val == values.get(i)) {
                    return i;// return the index of the value in the setting
                }
            }
        }

        return -1;
    }
    public  void loadUserPackSettings() {
        this.packSavedSettingsMap = new HashMap<>();
        File f = new File(SaveLocation.getSaveFolderLocation() + "/mods/shaderpacks/" + this.packName + ".txt");
        if (!f.exists()) {
            SimplyShaders.LOGGER.info("SavedPackSettings dont exist");
            return;
        }
        try {
            Scanner sc = new Scanner(f);
            while(sc.hasNextLine()) {
                String line = sc.nextLine();
                int idxOfEq = line.indexOf('=');
                if (idxOfEq > 0) {
                    int endOfVal = line.indexOf(" ",idxOfEq);
                    String key = line.substring(0, idxOfEq);
                    String value = line.substring(idxOfEq + 1, endOfVal);
                    this.packSavedSettingsMap.put(key, Float.parseFloat(value));
                }
            }
            sc.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }

    public  void saveUserPackSettings() {

        if (this.definedSettingsMap == null) {
            return;
        }
        //loop through and check if any settings are different from default
        boolean isSettingChanged = false;
        for (ShaderPackSetting set: this.definedSettingsMap.values()) {
            if (set.getChangedIndex() != -1) {
                isSettingChanged = true;
                break;
            }
        }

        if (!isSettingChanged && this.packSavedSettingsMap.isEmpty()) { //if the pack Saved Settings map is empty then it shouldnt update settings but if settings are all default but theres still values in the packSettinsMap it wont be empty so it should clear the fiel
            SimplyShaders.LOGGER.info("No Settings to save");
            return;
        }
        SimplyShaders.LOGGER.info("Saving Pack Setting");
        StringBuilder sb = new StringBuilder();
        for (ShaderPackSetting set: definedSettingsMap.values()) {

            if (set.getChangedIndex() == -1) {
                continue;
            }

            sb.append(set.name);
            sb.append("=");
            //really dumb but i only have boolean and slider settings
            if (set.type == ShaderPackSetting.SettingType.Toggle) {
                sb.append(set.getChangedIndex());
            } else {
                sb.append(set.values.get(set.getChangedIndex()));
            }

            sb.append(" \n");
        }
        //Identifier loc = Identifier.of("shaderpacks",ShaderPackLoader.selectedPack + ".txt");
        File f = new File(SaveLocation.getSaveFolderLocation() + "/mods/shaderpacks/" + this.packName + ".txt");

        try {
            f.createNewFile();// create if it doesnt exist
            FileOutputStream fos = new FileOutputStream(f);

            try {

                fos.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            } catch (Throwable var6) {
                try {
                    fos.close();
                } catch (Throwable var5) {
                    var6.addSuppressed(var5);
                }

                throw var6;
            }

            fos.close();
        } catch (Exception var7) {
            var7.printStackTrace();
        }



    }
}
