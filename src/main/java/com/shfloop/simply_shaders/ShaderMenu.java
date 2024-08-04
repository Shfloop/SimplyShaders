package com.shfloop.simply_shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ScreenUtils;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.ui.UIElement;
import finalforeach.cosmicreach.ui.UISlider;

import java.io.IOException;

public class ShaderMenu extends GameState {
    GameState previousState;
    public ShaderMenu(final GameState previousState) {
        this.previousState = previousState;


        UIElement toggle_shaders_button = new UIElement(-137.0F, -200.0F, 250.0F, 50.0F) {

            @Override
            public void onCreate() {
                super.onCreate();
                super.updateText();
            }

            @Override
            public void onClick() {
                super.onClick();

                if (InGame.world != null && !Shadows.shaders_on)
                {

                    System.out.println("Changing shaders to shadows");
                    System.out.println("SUN DIRECTION " + Shadows.getCamera().direction);
                    try {
                        Shadows.turnShadowsOn();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    //creating new shader reloads it so this should be it just need to load back the old shaders when done

                } else if (InGame.world != null && Shadows.shaders_on) {
                    try {
                        Shadows.cleanup();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    dispose();
                }
                Shadows.shaders_on = !Shadows.shaders_on;
                this.updateText();

            }
            @Override
            public void updateText() {
                this.setText("Shaders: " + (Shadows.shaders_on ? "on": "off"));
            }
        };
        toggle_shaders_button.updateText(); // again dont know why i need to do this onCreate should update it
        toggle_shaders_button.show();
        this.uiObjects.add(toggle_shaders_button);
        UIElement shadow_map = new UIElement(137.0F, 100.0F, 250.0F, 50.0F) {
            public void onClick() {
                super.onClick();
                GameState.switchToGameState(new ShaderSelectionMenu(ShaderMenu.this));
            }
        };
        shadow_map.setText("Shaders");
        shadow_map.show();
        this.uiObjects.add(shadow_map);


        UISlider sun_time_slider = new UISlider(0.0F, 3000.0F, (float) Shadows.time_of_day, 137.0F, -200.0F, 250.0F, 50.0F) {
            @Override
            public void onCreate() {
                super.onCreate();
                this.updateText();
            }

            @Override
            public void onMouseUp() {
                super.onMouseUp();
                Shadows.time_of_day = (int)this.currentValue;
                Shadows.calcSunDirection();
                this.updateText();
            }

            @Override
            public void validate() {
                super.validate();
                this.currentValue = (float)((int)this.currentValue);
                this.updateText();
            }

            @Override
            public void updateText() {
                this.setText("Time : " + (int)this.currentValue);
            }// not sure how to measure this

        };
        sun_time_slider.show();
        this.uiObjects.add(sun_time_slider);

        UIElement doneButton = new UIElement(0.0F, 200.0F, 250.0F, 50.0F) {
            @Override
            public void onClick() {
                super.onClick();

                GameState.switchToGameState(previousState);
            }
        };
        doneButton.setText("Done");
        doneButton.show();
        this.uiObjects.add(doneButton);
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
