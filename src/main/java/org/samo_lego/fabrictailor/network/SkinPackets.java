package org.samo_lego.fabrictailor.network;

import com.mojang.authlib.properties.Property;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static org.samo_lego.fabrictailor.FabricTailor.MOD_ID;

public class SkinPackets {
    public static final ResourceLocation FABRICTAILOR_VANILLA_CHANGE = new ResourceLocation(MOD_ID, "skin_change_vanilla");
    public static final ResourceLocation FABRICTAILOR_HD_CHANGE = new ResourceLocation(MOD_ID, "skin_change_hd");
    public static final ResourceLocation FABRICTAILOR_DEFAULT_SKIN = new ResourceLocation(MOD_ID, "default_skin_request");

    public static FriendlyByteBuf skin2ByteBuf(@NotNull Property skinData) {
        return generateSkinData(skinData.getValue(), skinData.getSignature());
    }


    private static FriendlyByteBuf generateSkinData(String value, String signature) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeUtf(value);

        buf.writeUtf(Objects.requireNonNullElse(signature, ""));

        return buf;
    }
}
