package org.samo_lego.fabrictailor.network.payload;

import com.mojang.authlib.properties.Property;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;
import org.samo_lego.fabrictailor.util.PropertyExtension;

import static org.samo_lego.fabrictailor.network.SkinPackets.FABRICTAILOR_HD_CHANGE;

public record HDSkinPayload(Property skinProperty) implements CustomPacketPayload {

    public static final Type<HDSkinPayload> TYPE = new CustomPacketPayload.Type<>(FABRICTAILOR_HD_CHANGE);
    public static final StreamCodec<FriendlyByteBuf, HDSkinPayload> CODEC = StreamCodec.composite(PropertyExtension.STREAM_CODEC, HDSkinPayload::skinProperty, HDSkinPayload::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
