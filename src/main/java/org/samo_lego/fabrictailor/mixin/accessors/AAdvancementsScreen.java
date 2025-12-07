package org.samo_lego.fabrictailor.mixin.accessors;

import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AdvancementsScreen.class)
public interface AAdvancementsScreen {
    @Accessor("WINDOW_LOCATION")
    static Identifier getWINDOW_LOCATION() {
        throw new AssertionError();
    }
}
