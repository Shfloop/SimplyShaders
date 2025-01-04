package com.shfloop.simply_shaders.menus;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.*;
import com.shfloop.simply_shaders.SimplyShaders;
import com.shfloop.simply_shaders.pack_loading.ShaderPackLoader;
import finalforeach.cosmicreach.GameAssetLoader;
import finalforeach.cosmicreach.io.SaveLocation;
import finalforeach.cosmicreach.settings.SettingsDictionary;
import finalforeach.cosmicreach.util.Identifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.StringBuilder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Optional;
import java.util.Scanner;

public class ShaderPackSetting { //holds the value for the settings
    //if its a bool
    // slider
    // selection
    //values
    //saved value
    //name
    //the .lang is responsible for replacing values with strings and theactual name of the setting
    public SettingType type;
    public FloatArray values;
    public String name;
    private int changedIndex;
    private int defaultIndex;

    private static HashMap<String, Float> CurPackSavedSettings = new HashMap<>();



    public ShaderPackSetting(float defaultValue,String name, FloatArray values, SettingType type) {
        this.name = name;
        this.values = values;
        this.type = type;
        this.defaultIndex = 0;
        for (int i = 0; i < values.size; i ++) {
            if (values.items[i] == defaultValue) {
                this.defaultIndex = i;
            }
        }
        //probably should go in a different class
        this.changedIndex =  getSavedSettingIdx(this.name);//when a save txt is loaded if there is a saved value it will overide this
        //else set to -1 so settings can use the default
        //also used when creating the save txt so it only saves the changed info

    }

    private  int getSavedSettingIdx(String settingName) {
        //if the saved setting exists get the value if not return -1
        Float val = CurPackSavedSettings.get(settingName);
        if (val != null) {
            SimplyShaders.LOGGER.info("FOUND SAVE {}: , {}", settingName, val);
            for(int i = 0; i < this.values.size; i++) {
                if (val == this.values.get(i)) {
                    return i;// return the index of the value in the setting
                }
            }
        }

        return -1;
    }
    public static void loadUserPackSettings() {
        CurPackSavedSettings = new HashMap<>();
        File f = new File(SaveLocation.getSaveFolderLocation() + "/mods/shaderpacks/" + ShaderPackLoader.selectedPack + ".txt");
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
                    CurPackSavedSettings.put(key, Float.parseFloat(value));
                }
            }
            sc.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }
    public static void saveUserPackSettings() {
        if (PackSettingsMenu.definedSettingsMap == null) {
            return;
        }
        //loop through and check if any settings are different from default
        boolean isSettingChanged = false;
        for (ShaderPackSetting set: PackSettingsMenu.definedSettingsMap.values()) {
            if (set.changedIndex != -1) {
                isSettingChanged = true;
                break;
            }
        }

        if (!isSettingChanged ) {
            SimplyShaders.LOGGER.info("No Settings to save");
            return;
        }
        SimplyShaders.LOGGER.info("Saving Pack Setting");
        StringBuilder sb = new StringBuilder();
        for (ShaderPackSetting set: PackSettingsMenu.definedSettingsMap.values()) {
            if (set.changedIndex == -1) {
                continue;
            }
            sb.append(set.name);
            sb.append("=");
            sb.append(set.values.get(set.changedIndex));
            sb.append(" \n");
        }
        //Identifier loc = Identifier.of("shaderpacks",ShaderPackLoader.selectedPack + ".txt");
        File f = new File(SaveLocation.getSaveFolderLocation() + "/mods/shaderpacks/" + ShaderPackLoader.selectedPack + ".txt");

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

    public void setIfChangedIdx(int selectedValueIndex) {
        if (this.defaultIndex == selectedValueIndex) {
            this.changedIndex = -1;
            return;
        }
        this.changedIndex = selectedValueIndex;
    }

    public int getCurrentIdx() {
        if (this.changedIndex == -1) {
            return this.defaultIndex;
        }
        return this.changedIndex;
    }

    public enum SettingType {

        Slider,
        Cycle,
        Toggle //ill add toggle because toggle wont have any values associated with it default value will be 0/1 1 for not commented out

    }

    @Override
    public String toString() {
        //return the object as a formatted string used for the settings.glsl
        int outSize = this.name.length() + this.values.size * 2;// rough estimate of the size
        StringBuilder builder = new StringBuilder(outSize);
        builder.append("#define ");
        builder.append(this.name);
        builder.append(' ');

        builder.append(this.values.get(this.getCurrentIdx()));
        builder.append(" //[");// probably not the fastest way to build the string but its fine
        for (int i = 0; i < this.values.size; i++) {
            builder.append(this.values.items[i]);
            builder.append(" ");
        }
        builder.append("] \n");

        return builder.toString();

    }
}

