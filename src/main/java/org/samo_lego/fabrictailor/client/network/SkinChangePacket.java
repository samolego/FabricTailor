package org.samo_lego.fabrictailor.client.network;

import com.mojang.authlib.properties.Property;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.impl.networking.client.ClientNetworkingImpl;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static org.samo_lego.fabrictailor.FabricTailor.MOD_ID;

public class SkinChangePacket {
    public static final ResourceLocation FABRICTAILOR_CHANNEL = new ResourceLocation(MOD_ID, "skin_change");

    public static Packet<ServerCommonPacketListener> create(@NotNull Property skinData) {
        return ClientNetworkingImpl.createC2SPacket(FABRICTAILOR_CHANNEL, generateSkinData(skinData.value(), skinData.signature()));
    }

    private static FriendlyByteBuf generateSkinData(String value, String signature) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeUtf(value);
        buf.writeUtf(signature);

        return buf;
    }
}
