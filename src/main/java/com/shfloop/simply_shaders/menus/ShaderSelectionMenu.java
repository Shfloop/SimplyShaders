package com.shfloop.simply_shaders.menus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.shfloop.simply_shaders.pack_loading.ShaderPackLoader;
import com.shfloop.simply_shaders.Shadows;
import finalforeach.cosmicreach.gamestates.*;
import finalforeach.cosmicreach.io.SaveLocation;
import finalforeach.cosmicreach.ui.HorizontalAnchor;
import finalforeach.cosmicreach.ui.UIElement;
import finalforeach.cosmicreach.ui.VerticalAnchor;


import java.io.File;
import java.io.IOException;
import java.util.Objects;


public class ShaderSelectionMenu extends GameState{




        Array<UIElement> shaderButtons = new Array<>();
        GameState previousState;
        UIElement upButton;
        UIElement downButton;
        int topWorldIdx;
        static int last_selected_idx =1 ; //TODO change this to load from settings file
        //static boolean shadersOn = false;
    Array<String> allShaders;


    // copied from cr WorldSelectionMenu
        private void cycleWorldButtons() {
            if (this.upButton != null) {
                if (this.topWorldIdx > 0) {
                    this.upButton.show();
                } else {
                    this.upButton.hide();
                }
            }

            if (this.downButton != null) {
                this.downButton.hide();
            }

            float y = 0.0F;

            for(int i = 0; i < this.shaderButtons.size; ++i) {
                UIElement worldButton = (UIElement)this.shaderButtons.get(i);
                if (i < this.topWorldIdx) {
                    worldButton.hide();
                } else {
                    worldButton.show();
                    worldButton.y = y + 16.0F;
                    y += 75.0F;
                    if (y > 550.0F) {
                        worldButton.hide();
                        if (this.downButton != null) {
                            this.downButton.show();
                        }
                    }
                }
            }

        }
        public ShaderSelectionMenu(final GameState previousState) {

            this.previousState = previousState;


            final String shaderRoot = SaveLocation.getSaveFolderLocation() + "/mods/shaderpacks";
            final File shaderDir = new File(shaderRoot);
            shaderDir.mkdirs();
            String[] allShaderFolders = shaderDir.list();
             allShaders = new Array<>();
            for (int i = 0; i < Objects.requireNonNull(allShaderFolders).length; i++) {
                if (allShaderFolders[i].equals("InternalShader")) {
                    continue; //dont want to add it to the array
                }
                allShaders.add(allShaderFolders[i]);

            }
            float x = 0.0f;
            float y = 0.0f;
            int idx = 1;
            UIElement enable_shader_button = new UIElement(x, y + 16.0F, 250.0F, 50.0F) {
                public void onClick() {

                    //for now i could just change the color to indicate what is selected
                    if (allShaders.size != 0) {
                        Shadows.shaders_on = !Shadows.shaders_on;

                    }
                    this.updateText();

                }

                public void updateText() { //this will just act as reset i suppose
                    this.setText(Shadows.shaders_on ? "Enabled" : "Disabled");

                }
            };
            this.shaderButtons.add(enable_shader_button);
            y+= 75.0f;
            enable_shader_button.vAnchor = VerticalAnchor.TOP_ALIGNED;
            enable_shader_button.hAnchor = HorizontalAnchor.CENTERED;
            enable_shader_button.updateText();
            enable_shader_button.show();
            this.uiObjects.add(enable_shader_button);
            for (String shader : allShaders) {
                int finalIdx = idx;
                UIElement shader_button = new UIElement(x, y + 16.0F, 250.0F, 50.0F) {
                    public void onClick() {
                        ShaderSelectionMenu.this.UpdateLastPressed();
                        //for now i could just change the color to indicate what is selected
                        last_selected_idx = finalIdx;//not sure if this will work but lets see
                        this.buttonTex = uiPanelPressedTex;
                        this.setTextColor(Color.RED);


                    }
                    public void updateText() { //this will just act as reset i suppose
                        this.buttonTex = uiPanelTex;
                        this.setTextColor(Color.WHITE);

                    }

                };
                if (idx == last_selected_idx) {
                    shader_button.setTextColor(Color.RED);
                }
                this.shaderButtons.add(shader_button);
                idx+= 1;
                y+= 75.0f;
                shader_button.vAnchor = VerticalAnchor.TOP_ALIGNED;
                shader_button.hAnchor = HorizontalAnchor.CENTERED;
                shader_button.setText(shader);
                shader_button.show();
                this.uiObjects.add(shader_button);

            }

            this.upButton = new UIElement(x + 50.0F + 125.0F, -50.0F, 50.0F, 50.0F) {
                public void onClick() {
                    super.onClick();
                    topWorldIdx = MathUtils.clamp(topWorldIdx - 1, 0, shaderButtons.size - 1);
                    cycleWorldButtons();
                }
            };
            this.upButton.vAnchor = VerticalAnchor.CENTERED;
            this.upButton.hAnchor = HorizontalAnchor.CENTERED;
            this.upButton.setText("^");
            this.upButton.show();
            this.uiObjects.add(this.upButton);
            this.downButton = new UIElement(x + 50.0F + 125.0F, 50.0F, 50.0F, 50.0F) {
                public void onClick() {
                    super.onClick();
                    topWorldIdx = MathUtils.clamp(topWorldIdx + 1, 0, shaderButtons.size - 1);
                    cycleWorldButtons();
                }
            };
            this.downButton.vAnchor = VerticalAnchor.CENTERED;
            this.downButton.hAnchor = HorizontalAnchor.CENTERED;
            this.downButton.setText("V");
            this.downButton.show();
            this.uiObjects.add(this.downButton);
            this.cycleWorldButtons();


            UIElement applyButton = new UIElement(-275.0F, -16.0F, 250.0F, 50.0F) {
                public void onClick() {
                    super.onClick();

                    //i need someway of changing settings
                    //for shaderpack settings i could load a copy when the go into edit
                    //edit the copy and if they hit apply save the file



                    ShaderSelectionMenu.this.applyShaderPackSelection();


                }
            };
            applyButton.vAnchor = VerticalAnchor.BOTTOM_ALIGNED;
            applyButton.setText(("Apply"));
            applyButton.show();
            this.uiObjects.add(applyButton);
            UIElement returnButton = new UIElement(0.0F, -16.0F, 250.0F, 50.0F) {
                public void onClick() {
                    super.onClick();
                    applyShaderPackSelection();
                    GameState.switchToGameState(previousState);
                }
            };
            returnButton.vAnchor = VerticalAnchor.BOTTOM_ALIGNED;
            returnButton.setText("Return");
            returnButton.show();
            this.uiObjects.add(returnButton);
            UIElement IsometricButton = new UIElement(-5.0F, -5.0F, 250.0F, 50.0F) {//-137.0F, 25.0F, 250.0F, 50.0F


                @Override
                public void onClick() {
                    super.onClick();
                    GameState.switchToGameState(new PackSettingsMenu(ShaderSelectionMenu.this));
                }

            };
            IsometricButton.setText("PackSettings");
            IsometricButton.vAnchor = VerticalAnchor.BOTTOM_ALIGNED;
            IsometricButton.hAnchor = HorizontalAnchor.LEFT_ALIGNED;
            IsometricButton.show();
            this.uiObjects.add(IsometricButton);
            UIElement loadButton = new UIElement(275.0F, -16.0F, 250.0F, 50.0F) {
               public void onClick() {
                    super.onClick();

                    try {
                        String saveFolderLocation = SaveLocation.getSaveFolderLocation();
                        File  saveFolder = new File(saveFolderLocation + "/mods/shaderpacks");
                        saveFolder.mkdirs(); //they should already be made once the mod initializes but whatevs
                        SaveLocation.OpenFolderWithFileManager(saveFolder);
                    } catch (IOException var2) {
                        var2.printStackTrace();
                    }

                }
            };
            loadButton.vAnchor = VerticalAnchor.BOTTOM_ALIGNED;
            loadButton.setText("Open Shader Directory");
            loadButton.show();
            this.uiObjects.add(loadButton);
        }

    private void applyShaderPackSelection()  {

        if (allShaders.size == 0) { // if there arent any packs in the folder we dont want to turn on the shaders
            return;
        }
        ShaderPackLoader.selectedPack = allShaders.get(last_selected_idx - 1) ; // Last selected idx needs to be -1 cause i dont have the enable shader buitton in allshaders but its in the scroll ilst
        //Shadows.shaders_on = shadersOn; /
        //gonna ghange it first cvause it migth be a aproblem if cleanup is called but the render still happens with shaders on
        if (InGame.getWorld() != null) {
            if (Shadows.shaders_on) {
                try {
                    Shadows.turnShadowsOn();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {

                    Shadows.cleanup();


            }
            //ChunkShader.reloadAllShaders();
        } else if (!Shadows.shaders_on) {
            Shadows.cleanup(); //dont think i need to call reload shaders
        }


    }

    private void UpdateLastPressed() {
            shaderButtons.get(last_selected_idx).updateText();
    }

    public void render() {
            super.render();
            if (Gdx.input.isKeyJustPressed(111)) {
                applyShaderPackSelection();
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



