package org.samo_lego.fabrictailor.mixin.accessors;

import net.minecraft.commands.arguments.selector.EntitySelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntitySelector.class)
public interface AEntitySelector {

    @Accessor
    String getPlayerName();

}
