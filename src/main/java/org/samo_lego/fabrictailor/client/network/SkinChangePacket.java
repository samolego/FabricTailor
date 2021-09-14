package org.samo_lego.fabrictailor.client.network;

import com.mojang.authlib.properties.Property;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import static org.samo_lego.fabrictailor.FabricTailor.MOD_ID;

public class SkinChangePacket {
    public static final Identifier FABRICTAILOR_CHANNEL = new Identifier(MOD_ID, "skin_change");

    public static CustomPayloadC2SPacket create(@NotNull Property skinData) {
        return new CustomPayloadC2SPacket(FABRICTAILOR_CHANNEL, generateSkinData(skinData.getValue(), skinData.getSignature()));
    }

    private static PacketByteBuf generateSkinData(String value, String signature) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeString(value);
        buf.writeString(signature);

        return buf;
    }
}
