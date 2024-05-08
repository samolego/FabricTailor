package org.samo_lego.fabrictailor.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import static org.samo_lego.fabrictailor.network.SkinPackets.FT_HELLO;

public record FabricTailorHelloPayload(boolean allowSkinButton) implements CustomPacketPayload {

    public static final Type<FabricTailorHelloPayload> TYPE = CustomPacketPayload.createType(FT_HELLO.toString());
    public static final StreamCodec<FriendlyByteBuf, FabricTailorHelloPayload> CODEC = StreamCodec.of(
            (buf, value) -> buf.writeBoolean(value.allowSkinButton()),
            buf -> new FabricTailorHelloPayload(buf.readBoolean())
    );


    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
