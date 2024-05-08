package org.samo_lego.fabrictailor.util;

import com.mojang.authlib.properties.Property;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public class PropertyExtension {
    public static final StreamCodec<? super FriendlyByteBuf, Property> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull Property decode(FriendlyByteBuf buf) {
            var name = buf.readUtf();
            var value = buf.readUtf();
            var signature = buf.readNullable(FriendlyByteBuf::readUtf);

            return new Property(name, value, signature);
        }

        @Override
        public void encode(FriendlyByteBuf object, Property property) {
            object.writeUtf(property.name());
            object.writeUtf(property.value());
            object.writeNullable(property.signature(), FriendlyByteBuf::writeUtf);
        }
    };
}
