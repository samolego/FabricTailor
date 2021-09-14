package org.samo_lego.fabrictailor.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(AdvancementsScreen.class)
public interface AdvancementsScreenAccessor {
    @Accessor("WINDOW_TEXTURE")
    static Identifier getWINDOW_TEXTURE() {
        throw new AssertionError();
    }

    @Accessor("TABS_TEXTURE")
    static Identifier getTABS_TEXTURE() {
        throw new AssertionError();
    }
}
