package com.shfloop.simply_shaders.menus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ScreenUtils;
import com.shfloop.simply_shaders.pack_loading.ShaderPackLoader;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.ui.HorizontalAnchor;
import finalforeach.cosmicreach.ui.UIElement;
import finalforeach.cosmicreach.ui.VerticalAnchor;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

public class PackSettingsMenu extends GameState {
    //load settings.glsl to get the variables to be used in the shaders.properties file
    //in shaders.properties load for each screen. make a button with a name after the . and before the space
    // for each item in the values put it into a map
    // buttons can either be a slider or cycle button
    //have a slider definition in shader.properties
    //each screen should have a max of 10 buttons per screen
    //for now no sub buttons just make something that works
    // i also need the shader.properties to load stuff for the render like scale of a composite pass or other settings

    GameState previousState;
    public static String[] settingsFile;
    public static Properties currentShaderPropeties;
    public static HashSet<String> packSettingVariables;

    public Array<String> mainScreenButtons;
    public static HashMap<String, ShaderPackSetting> definedSettingsMap;
    //each sub menu is given the object to modify
    //or have a static function use this to reconstruct the settings.glsl file
    //need to replace the include settings.glsl with a the file fetch and replace it with reconstructed one
    //
    public static void loadSettings() { //need to initialize the settings even if they dont go into packmenu
        loadShaderProperties();
        loadGlslSettings();
    }
    public PackSettingsMenu(final GameState previousState) {
       loadSettings();
       mainScreenButtons = new Array<>();
        for (String prop: currentShaderPropeties.stringPropertyNames()) {
            //find all the keys with the screen. name
            if (prop.contains("screen.")) {
                String buttonName = prop.substring(prop.indexOf('.') + 1);

                assert false;
                mainScreenButtons.add(buttonName);

                //split on whitespace for the values
                //packSettingVariables.addAll(Arrays.asList(currentShaderPropeties.getProperty(prop).split(" ")));

            }
        }

       int x = 0;
       int y = 0;
        this.previousState = previousState;
        //each page has a list of settings that it can have
        //each setting has a list of values that the setting can have
        //eahc setting page name is the screen.""
        //each SettingVariable



        System.out.println(mainScreenButtons);
        System.out.println(definedSettingsMap);
        for (String settingPage : mainScreenButtons) {

            //create the Setting[] for the main screeen subMenu
            String[] pageVariables;
            //use regex
            pageVariables = currentShaderPropeties.getProperty("screen." + settingPage).split("\\s+"); // have the page variables be an array and the variables in the settings .glsl be a hashset

            //for each page variable look up the the hashed name from the loaded settings.glsl
            //then let teh uiElement hold onto the shaderpack setting obj
            ShaderPackSetting[] settings = new ShaderPackSetting[pageVariables.length];
            int settingIdx = 0;
            for (String var: pageVariables) {
                ShaderPackSetting temp = definedSettingsMap.get(var);
                System.out.println(var + " : " + temp);
                settings[settingIdx++] = definedSettingsMap.get(var); //i may want to remove from the map
            }

            UIElement menu_button = new UIElement(x, y + 16.0F, 250.0F, 50.0F) {

                //needs all the settings for the button in the screen

                public void onClick() {


                    this.buttonTex = uiPanelPressedTex;
                    GameState.switchToGameState(new SettingsSubMenu(PackSettingsMenu.this, settings));



                }
                public void updateText() { //this will just act as reset i suppose
                    this.buttonTex = uiPanelTex;
                    this.setTextColor(Color.WHITE);

                }

            };



            y+= (int) 75.0f;
            menu_button.vAnchor = VerticalAnchor.TOP_ALIGNED;
            menu_button.hAnchor = HorizontalAnchor.CENTERED;
            //todo change settingPage to lang
            menu_button.setText(settingPage);
            menu_button.show();
            this.uiObjects.add(menu_button);

        }



    }
    public void render() {
        super.render();
        if (Gdx.input.isKeyJustPressed(111)) {
            saveChanges();
            switchToGameState(this.previousState);
        }

        ScreenUtils.clear(0.145F, 0.078F, 0.153F, 1.0F, true);
        Gdx.gl.glEnable(2929);
        Gdx.gl.glDepthFunc(513);
        Gdx.gl.glEnable(2884);
        Gdx.gl.glCullFace(1029);
        Gdx.gl.glEnable(3042);
        Gdx.gl.glBlendFunc(770, 771);
        this.drawUIElements();
    }
    public void saveChanges() {
        //in charge of updating the settingsArray
        ShaderPackSetting.saveUserPackSettings();
    }

    private static void loadShaderProperties() {
//        if (packSettingVariables != null && currentShaderPropeties != null) {
//            System.out.println("SKIPPING FILE LOAD FOR SHADER PROPERTIES");
//            return;
//        }
        packSettingVariables = new HashSet<>();
        currentShaderPropeties = new Properties();
        try {
            String temp = String.join("\n", ShaderPackLoader.loadFromZipOrUnzipShaderPack("shader.properties"));
            InputStream contents = new ByteArrayInputStream(temp.getBytes(StandardCharsets.UTF_8));

            currentShaderPropeties.load(contents);

        }
        catch (InvalidPathException e) { //it should be fine to continue but the settings page wont load
            return;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }



    }

    private static void loadGlslSettings() {
        //i should really make a locate function in shaderpackloader so the file structure doesnt have to be exact

//        if (definedSettingsMap != null) {
//            System.out.println("SKIPPING LOAD GLSL");
//            return;
//        }
        definedSettingsMap = new HashMap<>();
        try {
            String[] settingsFile = ShaderPackLoader.loadFromZipOrUnzipShaderPack("settings.glsl");
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
                        if (firstCommentIdx < defineLineIdx) { //means the define is commented out and means its just an ifdef define no values
                            //create toggle setting default off
                            throw new RuntimeException("NOT IMPLEMENTED");
                        } else {
                            //continue checking
                            //only think left to check is if its its toggle
                            //both slider and cycle have the same data

                            int defineEndIdx = defineLineIdx + 7;
                            for (int i = defineEndIdx;  i < line.length(); i++) {
                                if(line.charAt(i) >= 20) {
                                   defineEndIdx = i;
                                    break;
                                }
                            }
                            String settingName = line.substring(defineEndIdx , line.indexOf(" ", defineEndIdx + 1 )).trim(); // this is dumb need to loop to find end of whitespace
                            System.out.println("name: " + settingName);
                            //test if the default value is there
                            if (firstCommentIdx <= defineEndIdx + settingName.length()) {
                                throw new RuntimeException("ERM what");
                            }
                            String defaultValue = line.substring(defineEndIdx + settingName.length() + 1, firstCommentIdx).trim();
                            if (defaultValue.isEmpty()) {
                                //means its a toggle setting
                                System.out.println("VALUE EMPTY");
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
            throw new RuntimeException("NOT SETUP");
        }


    }

    private static @NotNull ShaderPackSetting getShaderPackSetting(String[] stringValues, float defaultParseValue, String settingName) {
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
        return new ShaderPackSetting(defaultParseValue, settingName,settingValues, ShaderPackSetting.SettingType.Slider);
    }

    public static void createSettingsString() {
        //take all the current settings objects and write them out into a basically txt file so it can be used as includes to shaderfiles
        String[] settingLines = new String[definedSettingsMap.size()];
        int idx = 0;
        for (ShaderPackSetting sval : definedSettingsMap.values()) {
            settingLines[idx++] = sval.toString();// the game shader will append the newline for us
            
        }
        settingsFile = settingLines;
    }




    }
