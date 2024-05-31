package org.samo_lego.fabrictailor.network.payload;

import com.mojang.authlib.properties.Property;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;
import org.samo_lego.fabrictailor.util.PropertyExtension;

import static org.samo_lego.fabrictailor.network.SkinPackets.FABRICTAILOR_DEFAULT_SKIN;

public record DefaultSkinPayload(Property skinProperty) implements CustomPacketPayload {

    public static final Type<DefaultSkinPayload> TYPE = new CustomPacketPayload.Type<>(FABRICTAILOR_DEFAULT_SKIN);
    public static final StreamCodec<FriendlyByteBuf, DefaultSkinPayload> CODEC = StreamCodec.composite(PropertyExtension.STREAM_CODEC, DefaultSkinPayload::skinProperty, DefaultSkinPayload::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
