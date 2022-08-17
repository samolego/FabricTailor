package org.samo_lego.fabrictailor.mixin;

import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.InsecureTextureException;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.biome.BiomeManager;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;
import org.samo_lego.fabrictailor.mixin.accessors.AChunkMap;
import org.samo_lego.fabrictailor.mixin.accessors.ATrackedEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Base64;

import static org.samo_lego.fabrictailor.FabricTailor.config;
import static org.samo_lego.fabrictailor.FabricTailor.errorLog;
import static org.samo_lego.fabrictailor.mixin.accessors.APlayer.getPLAYER_MODEL_PARTS;

@Mixin(ServerPlayer.class)
public class MServerPlayerEntity_TailoredPlayer implements TailoredPlayer {

    private static final String STEVE = "MHF_STEVE";
    private final ServerPlayer player = (ServerPlayer) (Object) this;
    private final GameProfile gameProfile = player.getGameProfile();

    private String skinValue;
    private String skinSignature;
    private final PropertyMap map = this.gameProfile.getProperties();
    private long lastSkinChangeTime = 0;


    /**
     * <p>
     * This method has been adapted from the Impersonate mod's <a href="https://github.com/Ladysnake/Impersonate/blob/1.16/src/main/java/io/github/ladysnake/impersonate/impl/ServerPlayerSkins.java">source code</a>
     * under GNU Lesser General Public License.
     * <p>
     * Reloads player's skin for all the players (including the one that has changed the skin)
     * </p>
     *
     * @author Pyrofab
     */
    @Override
    public void reloadSkin() {
        // Refreshing tablist for each player
        if(player.getServer() == null)
            return;
        PlayerList playerManager = player.getServer().getPlayerList();
        playerManager.broadcastAll(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, player));
        playerManager.broadcastAll(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, player));

        ServerChunkCache manager = player.getLevel().getChunkSource();
        ChunkMap storage = manager.chunkMap;
        ATrackedEntity trackerEntry = ((AChunkMap) storage).getEntityTrackers().get(player.getId());

        trackerEntry.getSeenBy().forEach(tracking -> trackerEntry.getServerEntity().addPairing(tracking.getPlayer()));

        // need to change the player entity on the client
        ServerLevel targetWorld = player.getLevel();
        player.connection.send(new ClientboundRespawnPacket(
                targetWorld.dimensionTypeId(),
                targetWorld.dimension(),
                BiomeManager.obfuscateSeed(targetWorld.getSeed()),
                player.gameMode.getGameModeForPlayer(),
                player.gameMode.getPreviousGameModeForPlayer(),
                targetWorld.isDebug(),
                targetWorld.isFlat(),
                true,
                this.player.getLastDeathLocation()));
        player.connection.teleport(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
        player.server.getPlayerList().sendPlayerPermissionLevel(player);
        player.connection.send(new ClientboundSetExperiencePacket(player.experienceProgress, player.totalExperience, player.experienceLevel));
        player.connection.send(new ClientboundSetHealthPacket(player.getHealth(), player.getFoodData().getFoodLevel(), player.getFoodData().getSaturationLevel()));
        for (MobEffectInstance statusEffect : player.getActiveEffects()) {
            player.connection.send(new ClientboundUpdateMobEffectPacket(player.getId(), statusEffect));
        }
        player.onUpdateAbilities();
        playerManager.sendLevelInfo(player, targetWorld);
        playerManager.sendAllPlayerInfo(player);
    }

    /**
     * Sets the skin to the specified player and reloads it with {@link MServerPlayerEntity_TailoredPlayer#reloadSkin()} reloadSkin().
     *
     * @param skinData skin texture data
     * @param reload   whether to send packets around for skin reload
     */
    public void setSkin(Property skinData, boolean reload) {
        try {
            this.map.removeAll("textures");
        } catch (Exception ignored) {
            // Player has no skin data, no worries
        }

        try {
            this.map.put("textures", skinData);

            // Saving skin data
            this.skinValue = skinData.getValue();
            this.skinSignature = skinData.getSignature();

            // Reloading skin
            if(reload)
                this.reloadSkin();

            this.lastSkinChangeTime = System.currentTimeMillis();

        } catch (InsecureTextureException ignored) {
            // No skin data
        } catch (Error e) {
            // Something went wrong when trying to set the skin
            errorLog(e.getMessage());
        }
    }

    @Override
    public void setSkin(String value, String signature, boolean reload) {
        this.setSkin(new Property("textures", value, signature), reload);
    }

    @Override
    public String getSkinValue() {
        if(this.skinValue == null) {
            try {
                Property property = map.get("textures").iterator().next();
                this.skinValue = property.getValue();
            } catch (Exception ignored) {
                // Player has no skin data, no worries
            }
        }
        return this.skinValue;
    }

    @Override
    public String getSkinSignature() {
        if(this.skinSignature == null) {
            try {
                Property property = map.get("textures").iterator().next();
                this.skinSignature = property.getSignature();
            } catch (Exception ignored) {
                // Player has no skin data, no worries
            }
        }
        return this.skinSignature;
    }

    @Override
    public long getLastSkinChange() {
        return this.lastSkinChangeTime;
    }

    @Override
    public void clearSkin() {
        try {
            this.map.removeAll("textures");
            // Ensure that the skin is completely cleared to prevent the save bug.
            this.skinValue = null;
            this.skinSignature = null;
            this.reloadSkin();
        } catch (Exception ignored) {
            // Player has no skin data, no worries
        }
    }

    @Override
    public String getSkinId() {
        String skin = this.skinValue;
        if (skin == null) {
            // Fallback to default skin
            var textures = player.getGameProfile().getProperties().get("textures").stream().findAny();

            if (textures.isPresent()) {
                skin = textures.get().getValue();
            } else {
                return STEVE;
            }
        }

        // Parse base64 skin
        String decoded = new String(Base64.getDecoder().decode(skin));

        // Parse as json, then get textures -> SKIN -> url value
        String url = JsonParser.parseString(decoded)
                .getAsJsonObject()
                .getAsJsonObject("textures")
                .getAsJsonObject("SKIN")
                .getAsJsonPrimitive("url")
                .getAsString();

        // Extract id from url
        return url.substring(url.lastIndexOf('/') + 1);
    }

    @Override
    public void resetLastSkinChange() {
        this.lastSkinChangeTime = 0;
    }

    @Inject(method = "updateOptions", at = @At("TAIL"))
    private void disableCapeIfNeeded(ServerboundClientInformationPacket packet, CallbackInfo ci) {
        if (!config.allowCapes) {
            byte playerModel = (byte) packet.modelCustomisation();

            // Fake cape rule to be off
            playerModel = (byte) (playerModel & ~(1));
            this.player.getEntityData().set(getPLAYER_MODEL_PARTS(), playerModel);
        }
    }


    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void writeCustomDataToNbt(CompoundTag tag, CallbackInfo ci) {
        if (this.getSkinValue() != null) {
            CompoundTag skinDataTag = new CompoundTag();
            skinDataTag.putString("value", this.getSkinValue());
            if (this.getSkinSignature() != null) {
                skinDataTag.putString("signature", this.getSkinSignature());
            }

            tag.put("fabrictailor:skin_data", skinDataTag);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readCustomDataFromNbt(CompoundTag tag, CallbackInfo ci) {
        CompoundTag skinDataTag = tag.getCompound("fabrictailor:skin_data");
        if(skinDataTag != null) {
            this.skinValue = skinDataTag.contains("value") ? skinDataTag.getString("value") : null;
            this.skinSignature = skinDataTag.contains("signature") ? skinDataTag.getString("signature") : null;

            if (this.skinValue != null) {
                this.setSkin(this.skinValue, this.skinSignature, false);
            }
        }
    }
}
