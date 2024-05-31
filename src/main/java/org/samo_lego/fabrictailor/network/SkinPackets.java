package org.samo_lego.fabrictailor.network;

import net.minecraft.resources.ResourceLocation;

import static org.samo_lego.fabrictailor.FabricTailor.MOD_ID;

public class SkinPackets {
    public static final ResourceLocation FABRICTAILOR_VANILLA_CHANGE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "skin_change_vanilla");
    public static final ResourceLocation FABRICTAILOR_HD_CHANGE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "skin_change_hd");
    public static final ResourceLocation FABRICTAILOR_DEFAULT_SKIN = ResourceLocation.fromNamespaceAndPath(MOD_ID, "default_skin_request");
    public static final ResourceLocation FT_HELLO = ResourceLocation.fromNamespaceAndPath(MOD_ID, "hello");
}
