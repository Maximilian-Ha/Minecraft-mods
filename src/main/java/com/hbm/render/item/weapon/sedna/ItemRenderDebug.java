package com.hbm.render.item.weapon.sedna;

import com.hbm.main.ResourceManager;

/**
 * Die Debugwaffe teilt sich Modell und Bewegung mit dem schweren Revolver und unterscheidet
 * sich nur in der Textur.
 */
public class ItemRenderDebug extends ItemRenderHeavyRevolver {

    public ItemRenderDebug() {
        super(ResourceManager.DEBUG_GUN_TEX);
    }
}
