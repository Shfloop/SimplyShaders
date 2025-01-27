package com.shfloop.simply_shaders.menus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.shfloop.simply_shaders.pack_loading.ShaderPackLoader;
import com.shfloop.simply_shaders.settings.PackSettings;
import com.shfloop.simply_shaders.settings.ShaderPackSetting;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.ui.HorizontalAnchor;
import finalforeach.cosmicreach.ui.UIElement;
import finalforeach.cosmicreach.ui.VerticalAnchor;
import org.jetbrains.annotations.NotNull;

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
    //public static String[] settingsFile;
    //public static Properties currentShaderPropeties;
    //public static HashSet<String> packSettingVariables;

    public Array<String> mainScreenButtons;
    //public static HashMap<String, ShaderPackSetting> definedSettingsMap;

    private final PackSettings packSettings;
    //each sub menu is given the object to modify
    //or have a static function use this to reconstruct the settings.glsl file
    //need to replace the include settings.glsl with a the file fetch and replace it with reconstructed one
    //

    public PackSettingsMenu(final GameState previousState, @NotNull String selectedPackName) {
        if (ShaderPackLoader.packSettings != null) {
            if (selectedPackName.equals(ShaderPackLoader.packSettings.packName)) {
                //if the selected pack is already loaded use it
                this.packSettings = ShaderPackLoader.packSettings;
            } else {
                this.packSettings = new PackSettings(selectedPackName);
            }

        } else {
            this.packSettings = new PackSettings(selectedPackName);
        }

        //loadSettings(); //only need to load settings when packSettings obj is created or updated
       mainScreenButtons = new Array<>();
        for (String prop: packSettings.packProperties.stringPropertyNames()) {
            //find all the keys with the screen. name
            if (prop.contains("screen.")) {
                String buttonName = prop.substring(prop.indexOf('.') + 1);

                assert false;
                mainScreenButtons.add(buttonName);

                //split on whitespace for the values
                //packSettingVariables.addAll(Arrays.asList(currentShaderPropeties.getProperty(prop).split(" ")));

            }
        }
        final int CENTER_X_SPACING = 160;
       int x = -CENTER_X_SPACING;
       int y = 75;
        this.previousState = previousState;
        //each page has a list of settings that it can have
        //each setting has a list of values that the setting can have
        //eahc setting page name is the screen.""
        //each SettingVariable



        System.out.println(mainScreenButtons);

        for (String settingPage : mainScreenButtons) {

            //create the Setting[] for the main screeen subMenu
            String[] pageVariables;
            //use regex
            pageVariables = packSettings.packProperties.getProperty("screen." + settingPage).split("\\s+"); // have the page variables be an array and the variables in the settings .glsl be a hashset

            //for each page variable look up the the hashed name from the loaded settings.glsl
            //then let teh uiElement hold onto the shaderpack setting obj
            ShaderPackSetting[] settings = new ShaderPackSetting[pageVariables.length];
            int settingIdx = 0;
            for (String var: pageVariables) {
                ShaderPackSetting temp = packSettings.definedSettingsMap.get(var);
                System.out.println(var + " : " + temp);
                settings[settingIdx++] = packSettings.definedSettingsMap.get(var); //i may want to remove from the map
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

            if (x == CENTER_X_SPACING) {
                x = -CENTER_X_SPACING;
                y += 75;
            } else {
                x = CENTER_X_SPACING;
            }

            //y+= (int) 75.0f;
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
        packSettings.saveUserPackSettings();
    }








    }
