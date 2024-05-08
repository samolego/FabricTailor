package org.samo_lego.fabrictailor.network.payload;

import com.mojang.authlib.properties.Property;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;
import org.samo_lego.fabrictailor.util.PropertyExtension;

import static org.samo_lego.fabrictailor.network.SkinPackets.FABRICTAILOR_VANILLA_CHANGE;

public record VanillaSkinPayload(Property skinProperty) implements CustomPacketPayload {

    public static final Type<VanillaSkinPayload> TYPE = CustomPacketPayload.createType(FABRICTAILOR_VANILLA_CHANGE.toString());
    public static final StreamCodec<FriendlyByteBuf, VanillaSkinPayload> CODEC = StreamCodec.composite(PropertyExtension.STREAM_CODEC, VanillaSkinPayload::skinProperty, VanillaSkinPayload::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
