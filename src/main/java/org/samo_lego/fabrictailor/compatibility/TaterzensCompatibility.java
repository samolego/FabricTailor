package org.samo_lego.fabrictailor.compatibility;

import com.mojang.authlib.properties.Property;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import org.samo_lego.taterzens.interfaces.ITaterzenEditor;
import org.samo_lego.taterzens.npc.TaterzenNPC;

public class TaterzensCompatibility {

    public static boolean setTaterzenSkin(ServerPlayerEntity executor, Property skinData) {
        TaterzenNPC taterzen = ((ITaterzenEditor) executor).getNpc();

        if(taterzen != null) {
            NbtCompound skinTag = new NbtCompound();
            skinTag.putString("value", skinData.getValue());
            skinTag.putString("signature", skinData.getSignature());

            taterzen.setSkinFromTag(skinTag);
            taterzen.sendProfileUpdates();
            return true;
        }
        return false;
    }
}
