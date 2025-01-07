package com.shfloop.simply_shaders.mixins;

import com.badlogic.gdx.utils.ObjectMap;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.rendering.items.ItemModel;
import finalforeach.cosmicreach.rendering.items.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.lang.ref.WeakReference;

@Mixin(ItemRenderer.class)
public interface ItemRendererInterfaceMixin {
    @Accessor("models")
    static ObjectMap<WeakReference<Item>, ItemModel> getModels() {throw new AssertionError();}
}
