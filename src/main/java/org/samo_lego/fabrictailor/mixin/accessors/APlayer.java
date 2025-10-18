package org.samo_lego.fabrictailor.mixin.accessors;

import com.mojang.authlib.GameProfile;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Player.class)
public interface APlayer {
    @Accessor
    @Final
    @Mutable
    void setGameProfile(GameProfile gameProfile);
}
