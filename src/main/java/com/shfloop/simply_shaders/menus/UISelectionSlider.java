package com.shfloop.simply_shaders.menus;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.Viewport;
import finalforeach.cosmicreach.ui.UIElement;

public class UISelectionSlider  extends UIElement {
    protected float currentValue;
    float[] values;
    private int currentIndex;

    public UISelectionSlider(float[] values, int defaultValIdx, float x, float y, float w, float h) {
        super(x, y, w, h, false);
        this.values = values;
        if (defaultValIdx <0 || defaultValIdx >= values.length) {
            this.currentIndex = 0;
        } else {
            this.currentIndex = defaultValIdx;
        }
        this.currentValue = this.values[this.currentIndex];
        this.updateText();
        this.onCreate();
    }

    public void onClick() {
        super.onClick();
    }

    public void onMouseDown() {
        super.onMouseDown();
    }

    public void onMouseUp() {
        super.onMouseUp();
        //this.validate();
    }

//    public void validate() {
//        this.currentValue = MathUtils.clamp(this.currentValue, this.min, this.max);
//    }

    public void drawBackground(Viewport uiViewport, SpriteBatch batch, float mouseX, float mouseY) {
        super.drawBackground(uiViewport, batch, mouseX, mouseY);
        if (this.isHeld) {
            float x = this.getDisplayX(uiViewport);
            float ratio = (mouseX - x) / this.w;

            int index = (int) (ratio * (values.length - 1) ); // i think casting int just takes the floor
            this.currentIndex = index;
            this.currentValue = values[index];
            //this.validate();
        }

        this.buttonTex = UIElement.uiPanelTex;
        this.drawKnobBackground(uiViewport, batch);
    }

    public void drawKnobBackground(Viewport uiViewport, SpriteBatch batch) {
        super.drawElementBackground(uiViewport, batch);
        float x = this.getDisplayX(uiViewport);
        float y = this.getDisplayY(uiViewport);

        float ratio = (float)this.currentIndex / (float)(this.values.length - 1);
        float knobW = 10.0F;
        float knobH = this.h + 8.0F;
        float knobX = x + ratio * this.w - knobW / 2.0F;
        float knobY = y - 4.0F;
        batch.draw(uiPanelHoverBoundsTex, knobX, knobY, 1.0F, 1.0F, knobW, knobH, 1.0F, 1.0F, 0.0F, 0, 0, this.buttonTex.getWidth(), this.buttonTex.getHeight(), false, true);
        batch.draw(uiPanelTex, knobX + 1.0F, knobY + 1.0F, 1.0F, 1.0F, knobW - 2.0F, knobH - 2.0F, 1.0F, 1.0F, 0.0F, 0, 0, this.buttonTex.getWidth(), this.buttonTex.getHeight(), false, true);
    }
}
