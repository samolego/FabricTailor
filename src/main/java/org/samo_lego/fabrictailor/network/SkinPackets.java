package org.samo_lego.fabrictailor.network;

import net.minecraft.resources.ResourceLocation;

import static org.samo_lego.fabrictailor.FabricTailor.MOD_ID;

public class SkinPackets {
    public static final ResourceLocation FABRICTAILOR_VANILLA_CHANGE = new ResourceLocation(MOD_ID, "skin_change_vanilla");
    public static final ResourceLocation FABRICTAILOR_HD_CHANGE = new ResourceLocation(MOD_ID, "skin_change_hd");
    public static final ResourceLocation FABRICTAILOR_DEFAULT_SKIN = new ResourceLocation(MOD_ID, "default_skin_request");
    public static final ResourceLocation FT_HELLO = new ResourceLocation(MOD_ID, "hello");
}
