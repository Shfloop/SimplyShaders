package com.shfloop.simply_shaders.mixins;


import com.shfloop.simply_shaders.menus.ShaderSelectionMenu;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.ui.HorizontalAnchor;
import finalforeach.cosmicreach.ui.UIElement;
import finalforeach.cosmicreach.ui.VerticalAnchor;
import finalforeach.cosmicreach.gamestates.OptionsMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsMenu.class)
public abstract class OptionsMenuMixin extends GameState {
    @Inject(method = "<init>(Lfinalforeach/cosmicreach/gamestates/GameState;)V", at = @At("TAIL"))
    private void addIsometricOptionsButton(CallbackInfo ci) {
        GameState temp = this;
        UIElement IsometricButton = new UIElement(-5.0F, -5.0F, 250.0F, 50.0F) {//-137.0F, 25.0F, 250.0F, 50.0F


            @Override
            public void onClick() {
                super.onClick();
                GameState.switchToGameState(new ShaderSelectionMenu(temp));
            }

        };
        IsometricButton.setText("Shaders");
        IsometricButton.vAnchor = VerticalAnchor.BOTTOM_ALIGNED;
        IsometricButton.hAnchor = HorizontalAnchor.LEFT_ALIGNED;
        IsometricButton.show();
        this.uiObjects.add(IsometricButton);
    }
}
