package org.samo_lego.fabrictailor.util;

import com.mojang.authlib.properties.Property;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class PropertyExtension {
    public static final StreamCodec<? super FriendlyByteBuf, Property> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public Property decode(FriendlyByteBuf buf) {
        }

        @Override
        public void encode(FriendlyByteBuf object, Property object2) {

        }
    };
}
