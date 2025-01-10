package com.shfloop.simply_shaders.settings;

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

    //private static HashMap<String, Float> CurPackSavedSettings = new HashMap<>();
    //TODO change this and seperate out the boolean setting its a liittle to much for a single class and take some of the things out of packSettings
    public ShaderPackSetting(int defaultValue, String name, int changedValue ) {
        this.name = name;
        this.type = SettingType.Toggle;
        this.defaultIndex = defaultValue;
        this.changedIndex = changedValue;
    }

    public ShaderPackSetting(float defaultValue,String name, FloatArray values, SettingType type, int changedIndex ) {
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
        this.changedIndex =  changedIndex;//when a save txt is loaded if there is a saved value it will overide this
        //else set to -1 so settings can use the default
        //also used when creating the save txt so it only saves the changed info

    }




    public int getChangedIndex() {
        return this.changedIndex;
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
        //TODO temporary fix
        if (this.type == SettingType.Toggle && this.getCurrentIdx() == 0) {
            return "\n";
        }
        int valuesSize = this.values == null ? 4 : this.values.size * 2;
        int outSize = this.name.length() + valuesSize;// rough estimate of the size
        StringBuilder builder = new StringBuilder(outSize);
        builder.append("#define ");
        builder.append(this.name);
        builder.append(' ');
        if (this.type == SettingType.Toggle) {
            builder.append(this.getCurrentIdx());
            builder.append( '\n');
            return builder.toString();
        }

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

