package com.shfloop.simply_shaders;

import com.shfloop.simply_shaders.menus.ShaderSelectionMenu;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.ui.UIElement;

public class OptionsUIElement extends UIElement {
    GameState temp;
    public OptionsUIElement(float x, float y, float w, float h, GameState next) {
        super(x, y, w, h);
        this.temp = next;
    }
    @Override
    public void onClick() {
        super.onClick();
        GameState.switchToGameState(new ShaderSelectionMenu(temp));
    }
}
