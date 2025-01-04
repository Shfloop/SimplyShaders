package com.shfloop.simply_shaders.menus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ScreenUtils;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.ui.HorizontalAnchor;
import finalforeach.cosmicreach.ui.UIElement;
import finalforeach.cosmicreach.ui.VerticalAnchor;

public class SettingsSubMenu extends GameState {
    GameState previousState;
    int[] selectedValueIndex; //an array size of settings where each index in the array cooresponds to the selected value in the settings[]
    private final ShaderPackSetting[] settings;
    public SettingsSubMenu(final GameState previousState, ShaderPackSetting[] settings) { //should also have a a string input with the buttons to add but how does one update the packs settings
        this.previousState = previousState;
        this.selectedValueIndex = new int[settings.length];
        this.settings = settings;
        int y = 0;
        int x = 0;
        int valIdx = 0;
        for (ShaderPackSetting setting : settings) {
            selectedValueIndex[valIdx] = setting.getCurrentIdx(); // initialize the array to defuault of the setting

            int finalValIdx = valIdx;
            valIdx++;
            UIElement settingButton ;
            if (setting.type == ShaderPackSetting.SettingType.Slider) {
                //need to copy make a new array here because the values.items isnt garenteed to have values in all spots of the array

                settingButton = new UISelectionSlider( setting.values.toArray(), setting.getCurrentIdx(), x, y + 16.0F, 250.0F, 50.0F ) {
                   final int idx = finalValIdx;
                   final String settingName = setting.name;
                   public void updateText() {
                       this.setText(this.settingName + ": " + this.values[this.currentIndex]);
                       selectedValueIndex[idx] = this.currentIndex;
                       //need this to change the actual setting somehow

                   }
                   public void onMouseUp() {
                       super.onMouseUp();

                       this.updateText();
                   }

               };
            } else {
                settingButton = new UIElement(x, y + 16.0F, 250.0F, 50.0F) {
                    final String settingName = setting.name;
                    final float[] values = setting.values.toArray();
                    int currentIdx = setting.getCurrentIdx();
                    final int idx = finalValIdx;
                    public void onClick() {
                        super.onClick();
                        //update the correct settings
                        this.updateText();
                    }

                    public void updateText() {
                        selectedValueIndex[idx] = currentIdx;
                        this.setText(this.settingName + ": " + values[currentIdx]);
                        currentIdx++;
                        if (currentIdx == values.length) {
                            currentIdx = 0;
                        }


                    }
                };
            }
            y+= (int) 75.0f;
            settingButton.vAnchor = VerticalAnchor.TOP_ALIGNED;
            settingButton.hAnchor = HorizontalAnchor.CENTERED;
            settingButton.updateText();
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
            saveChangesToSettings();
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
    public void saveChangesToSettings() {
        //all i need to do is to update the ShaderPackSetting[] with the changed values and when the shader pack reloads it should use the new values
        //to save changes go through the settings and update the values
        for (int i = 0; i < this.settings.length; i++) {

            this.settings[i].setIfChangedIdx(this.selectedValueIndex[i]);
            //this.settings[i].defaultIndex= this.selectedValueIndex[i]; //no need to waste a check if its the same
        }
    }

}
