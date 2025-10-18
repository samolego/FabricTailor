package org.samo_lego.fabrictailor.mixin.accessors;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Avatar.class)
public interface AAvatar {
    @Accessor("DATA_PLAYER_MODE_CUSTOMISATION")
    EntityDataAccessor<Byte> getPLAYER_MODEL_PARTS();
}
