package org.samo_lego.fabrictailor.network;

import com.mojang.authlib.properties.Property;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static org.samo_lego.fabrictailor.FabricTailor.MOD_ID;

public class SkinPackets {
    public static final ResourceLocation FABRICTAILOR_HELLO = new ResourceLocation(MOD_ID, "hello");
    public static final ResourceLocation FABRICTAILOR_SKIN_CHANGE = new ResourceLocation(MOD_ID, "skin_change");
    public static final ResourceLocation FABRICTAILOR_DEFAULT_SKIN = new ResourceLocation(MOD_ID, "default_skin_request");

    public static ServerboundCustomPayloadPacket createSkinChangePacket(@NotNull Property skinData) {
        return new ServerboundCustomPayloadPacket(FABRICTAILOR_SKIN_CHANGE, generateSkinData(skinData.getValue(), skinData.getSignature()));
    }

    public static ClientboundCustomPayloadPacket createHelloPacket(boolean allowDefaultSkinButton) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBoolean(allowDefaultSkinButton);

        return new ClientboundCustomPayloadPacket(FABRICTAILOR_HELLO, buf);
    }

    private static FriendlyByteBuf generateSkinData(String value, String signature) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeUtf(value);
        buf.writeUtf(signature);

        return buf;
    }
}
