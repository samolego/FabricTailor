package org.samo_lego.fabrictailor.client.network;

import com.mojang.authlib.properties.Property;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static org.samo_lego.fabrictailor.FabricTailor.MOD_ID;

public class SkinChangePacket {
    public static final ResourceLocation FABRICTAILOR_CHANNEL = new ResourceLocation(MOD_ID, "skin_change");

    public static ServerboundCustomPayloadPacket create(@NotNull Property skinData) {
        return new ServerboundCustomPayloadPacket(FABRICTAILOR_CHANNEL, generateSkinData(skinData.getValue(), skinData.getSignature()));
    }

    private static FriendlyByteBuf generateSkinData(String value, String signature) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeUtf(value);
        buf.writeUtf(signature);

        return buf;
    }
}
