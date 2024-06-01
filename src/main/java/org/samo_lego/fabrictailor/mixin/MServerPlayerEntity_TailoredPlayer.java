package org.samo_lego.fabrictailor.mixin;

import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;
import org.samo_lego.fabrictailor.mixin.accessors.AChunkMap;
import org.samo_lego.fabrictailor.mixin.accessors.ATrackedEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Optional;

import static org.samo_lego.fabrictailor.FabricTailor.config;
import static org.samo_lego.fabrictailor.FabricTailor.errorLog;
import static org.samo_lego.fabrictailor.mixin.accessors.APlayer.getPLAYER_MODEL_PARTS;

@Mixin(ServerPlayer.class)
public abstract class MServerPlayerEntity_TailoredPlayer extends Player implements TailoredPlayer {

    @Unique
    private static final String STEVE = "MHF_STEVE";
    @Unique
    private final ServerPlayer self = (ServerPlayer) (Object) this;
    @Unique
    private final GameProfile gameProfile = self.getGameProfile();
    @Unique
    private final PropertyMap map = this.gameProfile.getProperties();
    @Shadow
    public ServerGamePacketListenerImpl connection;

    @Shadow
    protected abstract void completeUsingItem();

    @Unique
    private String skinValue;
    @Unique
    private String skinSignature;
    @Unique
    private long lastSkinChangeTime = 0;

    public MServerPlayerEntity_TailoredPlayer(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }


    /**
     * @author Pyrofab
     * @see PlayerList#respawn(ServerPlayer, boolean, RemovalReason)
     * <p>
     * This method has been adapted from the Impersonate mod's <a href="https://github.com/Ladysnake/Impersonate/blob/1.16/src/main/java/io/github/ladysnake/impersonate/impl/ServerPlayerSkins.java">source code</a>
     * under GNU Lesser General Public License.
     * <p>
     * Reloads player's skin for all the players (including the one that has changed the skin)
     * </p>
     */
    @Override
    public void fabrictailor_reloadSkin() {
        if (self.getServer() == null) {
            errorLog("Tried to reload skin form client side! This should not happen!");
            return;
        }

        // Refreshing in tablist for each player
        PlayerList playerManager = self.getServer().getPlayerList();
        playerManager.broadcastAll(new ClientboundPlayerInfoRemovePacket(new ArrayList<>(Collections.singleton(self.getUUID()))));
        playerManager.broadcastAll(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(Collections.singleton(self)));

        ServerChunkCache manager = self.serverLevel().getChunkSource();
        ChunkMap storage = manager.chunkMap;
        ATrackedEntity trackerEntry = ((AChunkMap) storage).getEntityTrackers().get(self.getId());

        // Refreshing skin in world for all that see the player
        trackerEntry.getSeenBy().forEach(tracking -> trackerEntry.getServerEntity().addPairing(tracking.getPlayer()));

        // need to change the player entity on the client
        ServerLevel level = self.serverLevel();
        this.connection.send(new ClientboundRespawnPacket(
                        new CommonPlayerSpawnInfo(
                                level.dimensionTypeRegistration(),
                                level.dimension(),
                                BiomeManager.obfuscateSeed(level.getSeed()),
                                self.gameMode.getGameModeForPlayer(),
                                self.gameMode.getPreviousGameModeForPlayer(),
                                level.isDebug(),
                                level.isFlat(),
                                self.getLastDeathLocation(),
                                this.getPortalCooldown()
                        ),
                        (byte) 3
                )
        );

        this.connection.send(new ClientboundPlayerPositionPacket(self.getX(), self.getY(), self.getZ(), self.getYRot(), self.getXRot(), Collections.emptySet(), 0));
        this.connection.send(new ClientboundSetCarriedItemPacket(this.getInventory().selected));

        this.connection.send(new ClientboundChangeDifficultyPacket(level.getDifficulty(), level.getLevelData().isDifficultyLocked()));
        this.connection.send(new ClientboundSetExperiencePacket(this.experienceProgress, this.totalExperience, this.experienceLevel));
        playerManager.sendLevelInfo(self, level);
        playerManager.sendPlayerPermissionLevel(self);

        this.connection.send(new ClientboundSetHealthPacket(this.getHealth(), this.getFoodData().getFoodLevel(), this.getFoodData().getSaturationLevel()));

        for (MobEffectInstance statusEffect : this.getActiveEffects()) {
            this.connection.send(new ClientboundUpdateMobEffectPacket(self.getId(), statusEffect, false));
        }

        var equipmentList = new ArrayList<Pair<EquipmentSlot, ItemStack>>();
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            ItemStack itemStack = self.getItemBySlot(equipmentSlot);
            if (!itemStack.isEmpty()) {
                equipmentList.add(Pair.of(equipmentSlot, itemStack.copy()));
            }
        }

        if (!equipmentList.isEmpty()) {
            this.connection.send(new ClientboundSetEquipmentPacket(self.getId(), equipmentList));
        }


        if (!self.getPassengers().isEmpty()) {
            this.connection.send(new ClientboundSetPassengersPacket(self));
        }
        if (self.isPassenger()) {
            this.connection.send(new ClientboundSetPassengersPacket(self.getVehicle()));
        }

        this.onUpdateAbilities();
        playerManager.sendAllPlayerInfo(self);
    }

    /**
     * Sets the skin to the specified player and reloads it with {@link MServerPlayerEntity_TailoredPlayer#fabrictailor_reloadSkin()} ()} reloadSkin().
     *
     * @param skinData skin texture data
     * @param reload   whether to send packets around for skin reload
     */
    public void fabrictailor_setSkin(Property skinData, boolean reload) {
        try {
            this.map.removeAll(TailoredPlayer.PROPERTY_TEXTURES);
        } catch (Exception ignored) {
            // Player has no skin data, no worries
        }

        try {
            this.map.put(TailoredPlayer.PROPERTY_TEXTURES, skinData);

            // Saving skin data
            this.skinValue = skinData.value();
            this.skinSignature = skinData.signature();

            // Reloading skin
            if (reload) {
                this.fabrictailor_reloadSkin();
            }

            this.lastSkinChangeTime = System.currentTimeMillis();
        } catch (Error e) {
            // Something went wrong when trying to set the skin
            errorLog(e.getMessage());
        }
    }

    @Override
    public void fabrictailor_setSkin(String value, String signature, boolean reload) {
        this.fabrictailor_setSkin(new Property(TailoredPlayer.PROPERTY_TEXTURES, value, signature), reload);
    }

    @Override
    public Optional<String> fabrictailor_getSkinValue() {
        if (this.skinValue == null) {
            try {
                Property property = map.get(TailoredPlayer.PROPERTY_TEXTURES).iterator().next();
                this.skinValue = property.value();
            } catch (Exception ignored) {
                // Player has no skin data, no worries
            }
        }

        return Optional.ofNullable(this.skinValue);
    }

    @Override
    public Optional<String> fabrictailor_getSkinSignature() {
        if (this.skinSignature == null) {
            try {
                Property property = map.get(TailoredPlayer.PROPERTY_TEXTURES).iterator().next();
                this.skinSignature = property.signature();
            } catch (Exception ignored) {
                // Player has no skin data, no worries
            }
        }

        return Optional.ofNullable(this.skinSignature);
    }

    @Override
    public long fabrictailor_getLastSkinChange() {
        return this.lastSkinChangeTime;
    }

    @Override
    public void fabrictailor_clearSkin() {
        try {
            this.map.removeAll(TailoredPlayer.PROPERTY_TEXTURES);
            // Ensure that the skin is completely cleared to prevent the save bug.
            this.skinValue = null;
            this.skinSignature = null;
            this.fabrictailor_reloadSkin();
        } catch (Exception ignored) {
            // Player has no skin data, no worries
        }
    }

    @Override
    public String fabrictailor_getSkinId() {
        String skin = this.skinValue;
        if (skin == null) {
            // Fallback to default skin
            var textures = self.getGameProfile().getProperties().get(TailoredPlayer.PROPERTY_TEXTURES).stream().findAny();

            if (textures.isPresent()) {
                skin = textures.get().value();
            } else {
                return STEVE;
            }
        }

        // Parse base64 skin
        String decoded = new String(Base64.getDecoder().decode(skin));

        // Parse as json, then get textures -> SKIN -> url value
        String url = JsonParser.parseString(decoded)
                .getAsJsonObject()
                .getAsJsonObject(TailoredPlayer.PROPERTY_TEXTURES)
                .getAsJsonObject("SKIN")
                .getAsJsonPrimitive("url")
                .getAsString();

        // Extract id from url
        return url.substring(url.lastIndexOf('/') + 1);
    }

    @Override
    public void fabrictailor_resetLastSkinChange() {
        this.lastSkinChangeTime = 0;
    }

    @Inject(method = "updateOptions", at = @At("TAIL"))
    private void disableCapeIfNeeded(ClientInformation clientInformation, CallbackInfo ci) {
        if (!config.allowCapes) {
            byte playerModel = (byte) clientInformation.modelCustomisation();

            // Fake cape rule to be off
            playerModel = (byte) (playerModel & ~(1));
            this.self.getEntityData().set(getPLAYER_MODEL_PARTS(), playerModel);
        }
    }


    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void writeCustomDataToNbt(CompoundTag tag, CallbackInfo ci) {
        if (this.fabrictailor_getSkinValue().isPresent()) {
            CompoundTag skinDataTag = new CompoundTag();
            skinDataTag.putString("value", this.fabrictailor_getSkinValue().get());
            if (this.fabrictailor_getSkinSignature().isPresent()) {
                skinDataTag.putString("signature", this.fabrictailor_getSkinSignature().get());
            }

            tag.put("fabrictailor:skin_data", skinDataTag);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readCustomDataFromNbt(CompoundTag tag, CallbackInfo ci) {
        CompoundTag skinDataTag = tag.getCompound("fabrictailor:skin_data");
        this.skinValue = skinDataTag.contains("value") ? skinDataTag.getString("value") : null;
        this.skinSignature = skinDataTag.contains("signature") ? skinDataTag.getString("signature") : null;

        if (this.skinValue != null) {
            this.fabrictailor_setSkin(this.skinValue, this.skinSignature, false);
        }
    }
}
