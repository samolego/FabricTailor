package org.samo_lego.fabrictailor.mixin.client;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PropertyMap.class)
public class MixinPropertyMap {
    @Shadow @Final @Mutable
    private Multimap<String, Property> properties;

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void mutablePropertyMaps(Multimap<String, Property> properties, CallbackInfo ci) {
        this.properties = HashMultimap.create(properties);
    }
}
