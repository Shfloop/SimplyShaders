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
        builder.append(this.values.get(defaultIndex));
        builder.append(" //[");// probably not the fastest way to build the string but its fine
        for (int i = 0; i < this.values.size; i++) {
            builder.append(this.values.items[i]);
            builder.append(" ");
        }
        builder.append("] ");
        System.out.println(builder.toString());
        return builder.toString();

    }
}

