package org.samo_lego.fabrictailor.compatibility;

import carpet.script.annotation.AnnotationParser;
import carpet.script.annotation.ScarpetFunction;
import net.minecraft.server.level.ServerPlayer;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;

public class CarpetFunctions {

    public static void init() {
        AnnotationParser.parseFunctionClass(CarpetFunctions.class);
    }

    /**
     * Helper function to get texture hash from skin
     * that was set with the mod.
     *
     * @param player Player to get skin for
     * @return player's skin id (hash)
     */
    @ScarpetFunction
    public String ft_get_skin_id(ServerPlayer player) {
        return ((TailoredPlayer) player).fabrictailor_getSkinId();
    }
}
