package org.samo_lego.fabrictailor.compatibility;

import com.mojang.authlib.properties.Property;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.samo_lego.taterzens.interfaces.ITaterzenEditor;
import org.samo_lego.taterzens.npc.TaterzenNPC;

public class TaterzenSkins {

    public static boolean setTaterzenSkin(ServerPlayer executor, Property skinData) {
        TaterzenNPC taterzen = ((ITaterzenEditor) executor).getNpc();

        if (taterzen != null) {
            CompoundTag skinTag = new CompoundTag();
            skinTag.putString("value", skinData.value());
            skinTag.putString("signature", skinData.signature());

            taterzen.setSkinFromTag(skinTag);
            taterzen.sendProfileUpdates();
            return true;
        }
        return false;
    }
}
