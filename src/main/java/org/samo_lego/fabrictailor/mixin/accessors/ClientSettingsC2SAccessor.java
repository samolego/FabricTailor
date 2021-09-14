package org.samo_lego.fabrictailor.mixin.accessors;

import net.minecraft.network.packet.c2s.play.ClientSettingsC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientSettingsC2SPacket.class)
public interface ClientSettingsC2SAccessor {
    @Mutable
    @Accessor("playerModelBitMask")
    void setPlayerModelBitMask(int playerModelBitMask);
}
