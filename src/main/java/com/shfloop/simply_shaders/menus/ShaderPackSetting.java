package com.shfloop.simply_shaders.menus;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;

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
    public int defaultIndex;

    public ShaderPackSetting(float defaultValue,String name, float[] values, SettingType type) {
        this.name = name;
        this.values = new FloatArray(values);
        this.type = type;
        this.defaultIndex = 0;
        for (int i = 0; i < values.length; i ++) {
            if (values[i] == defaultValue) {
                this.defaultIndex = i;
            }
        }

    }
    public enum SettingType {

        Slider,
        Cycle, //bool could just be a 2 length cycle

    }
}

