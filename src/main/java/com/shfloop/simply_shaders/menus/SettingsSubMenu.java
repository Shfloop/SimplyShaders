package com.shfloop.simply_shaders.menus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ScreenUtils;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.OptionsMenu;
import finalforeach.cosmicreach.lang.Lang;
import finalforeach.cosmicreach.settings.Difficulty;
import finalforeach.cosmicreach.settings.DifficultySettings;
import finalforeach.cosmicreach.settings.GraphicsSettings;
import finalforeach.cosmicreach.ui.HorizontalAnchor;
import finalforeach.cosmicreach.ui.UIElement;
import finalforeach.cosmicreach.ui.UISlider;
import finalforeach.cosmicreach.ui.VerticalAnchor;

public class SettingsSubMenu extends GameState {
    GameState previousState;
    public SettingsSubMenu(final GameState previousState, ShaderPackSetting[] settings) { //should also have a a string input with the buttons to add but how does one update the packs settings
        this.previousState = previousState;
        int y = 0;
        int x = 0;
        for (ShaderPackSetting setting : settings) {
            UIElement settingButton ;
            if (setting.type == ShaderPackSetting.SettingType.Slider) {
                //need to copy make a new array here because the values.items isnt garenteed to have values in all spots of the array
               settingButton = new UISelectionSlider( setting.values.toArray(), setting.defaultIndex, x, y + 16.0F, 250.0F, 50.0F ) {
                   public void updateText() {
                       this.setText("" + this.currentValue);
                       //need this to change the actual setting somehow

                   }
                   public void onMouseUp() {
                       super.onMouseUp();

                       this.updateText();
                   }

               };
            } else {
                settingButton = new UIElement(x, y + 16.0F, 250.0F, 50.0F) {
                    final float[] values = setting.values.toArray();
                    int currentIdx = setting.defaultIndex;
                    public void onClick() {
                        super.onClick();
                        //update the correct settings
                        this.updateText();
                    }

                    public void updateText() {
                        currentIdx++;
                        if (currentIdx == values.length) {
                            currentIdx = 0;
                        }
                        this.setText("" + values[currentIdx]);
                    }
                };
            }
            y+= (int) 75.0f;
            settingButton.vAnchor = VerticalAnchor.TOP_ALIGNED;
            settingButton.hAnchor = HorizontalAnchor.CENTERED;
            settingButton.setText(setting.name);
            settingButton.show();
            this.uiObjects.add(settingButton);
        }


        //how do i apply the settings
        //could overwrite the file
        //but i should load the file and cache it somehow and edit the cached version


    }
    public void render() {
        super.render();
        if (Gdx.input.isKeyJustPressed(111)) {
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

}
