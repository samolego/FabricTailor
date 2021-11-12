package org.samo_lego.fabrictailor.network;

import com.mojang.authlib.properties.Property;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import static org.samo_lego.fabrictailor.FabricTailor.MOD_ID;

public class SkinPackets {
    public static final Identifier FABRICTAILOR_HELLO = new Identifier(MOD_ID, "hello");
    public static final Identifier FABRICTAILOR_SKIN_CHANGE = new Identifier(MOD_ID, "skin_change");
    public static final Identifier FABRICTAILOR_DEFAULT_SKIN = new Identifier(MOD_ID, "default_skin_request");

    public static CustomPayloadC2SPacket createSkinChangePacket(@NotNull Property skinData) {
        return new CustomPayloadC2SPacket(FABRICTAILOR_SKIN_CHANGE, generateSkinData(skinData.getValue(), skinData.getSignature()));
    }

    public static CustomPayloadS2CPacket createHelloPacket(boolean allowDefaultSkinButton) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBoolean(allowDefaultSkinButton);

        return new CustomPayloadS2CPacket(FABRICTAILOR_HELLO, buf);
    }

    private static PacketByteBuf generateSkinData(String value, String signature) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeString(value);
        buf.writeString(signature);

        return buf;
    }
}
