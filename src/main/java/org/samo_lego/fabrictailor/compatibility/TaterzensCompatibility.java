package org.samo_lego.fabrictailor.compatibility;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import org.samo_lego.taterzens.interfaces.TaterzenEditor;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import static org.samo_lego.taterzens.api.TaterzensAPI.noSelectedTaterzenError;

public class TaterzensCompatibility {

    public static void setTaterzenSkin(ServerPlayerEntity executor, String value, String signature) {
        TaterzenNPC taterzen = ((TaterzenEditor) executor).getNpc();

        if(taterzen != null) {
            CompoundTag skinTag = new CompoundTag();
            skinTag.putString("value", value);
            skinTag.putString("signature", signature);

            taterzen.setSkinFromTag(skinTag);
        } else {
            executor.sendMessage(noSelectedTaterzenError(), false);
        }
    }
}
