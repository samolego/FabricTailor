package org.samo_lego.fabrictailor.compatibility;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import org.samo_lego.taterzens.interfaces.TaterzenEditor;
import org.samo_lego.taterzens.npc.TaterzenNPC;

public class TaterzensCompatibility {

    public static boolean setTaterzenSkin(ServerPlayerEntity executor, String value, String signature) {
        TaterzenNPC taterzen = ((TaterzenEditor) executor).getNpc();

        if(taterzen != null) {
            NbtCompound skinTag = new NbtCompound();
            skinTag.putString("value", value);
            skinTag.putString("signature", signature);

            taterzen.setSkinFromTag(skinTag);
            taterzen.sendProfileUpdates();
            return true;
        }
        return false;
    }
}
