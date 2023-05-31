package org.samo_lego.fabrictailor.mixin.accessors.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(AdvancementsScreen.class)
public interface AAdvancementsScreen {
    @Accessor("WINDOW_LOCATION")
    static ResourceLocation getWINDOW_LOCATION() {
        throw new AssertionError();
    }
}
