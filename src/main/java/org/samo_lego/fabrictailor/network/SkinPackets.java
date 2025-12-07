package org.samo_lego.fabrictailor.network;

import net.minecraft.resources.Identifier;

import static org.samo_lego.fabrictailor.FabricTailor.MOD_ID;

public class SkinPackets {
    public static final Identifier FABRICTAILOR_VANILLA_CHANGE = Identifier.fromNamespaceAndPath(MOD_ID, "skin_change_vanilla");
    public static final Identifier FABRICTAILOR_HD_CHANGE = Identifier.fromNamespaceAndPath(MOD_ID, "skin_change_hd");
    public static final Identifier FABRICTAILOR_DEFAULT_SKIN = Identifier.fromNamespaceAndPath(MOD_ID, "default_skin_request");
    public static final Identifier FT_HELLO = Identifier.fromNamespaceAndPath(MOD_ID, "hello");
}
