package org.samo_lego.fabrictailor.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.InsecureTextureException;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.world.biome.source.BiomeAccess;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.samo_lego.fabrictailor.FabricTailor.errorLog;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin_TailoredPlayer implements TailoredPlayer  {

    private final ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
    private final GameProfile gameProfile = player.getGameProfile();

    private String skinValue;
    private String skinSignature;
    private final PropertyMap map = this.gameProfile.getProperties();


    /**
     * <p>
     * This method has been adapted from the Impersonate mod's <a href="https://github.com/Ladysnake/Impersonate/blob/1.16/src/main/java/io/github/ladysnake/impersonate/impl/ServerPlayerSkins.java">source code</a>
     * under GNU Lesser General Public License.
     *
     * Reloads player's skin for all the players (including the one that has changed the skin)
     *
     * @author Pyrofab
     */
    @Override
    public void reloadSkin() {
        // Refreshing tablist for each player
        if(player.getServer() == null)
            return;
        PlayerManager playerManager = player.getServer().getPlayerManager();
        playerManager.sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.REMOVE_PLAYER, player));
        playerManager.sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, player));

        ServerChunkManager manager = player.getServerWorld().getChunkManager();
        ThreadedAnvilChunkStorage storage = manager.threadedAnvilChunkStorage;
        EntityTrackerAccessor trackerEntry = ((ThreadedAnvilChunkStorageAccessor) storage).getEntityTrackers().get(player.getId());

        trackerEntry.getListeners().forEach(tracking -> trackerEntry.getEntry().startTracking(tracking.getPlayer()));

        // need to change the player entity on the client
        ServerWorld targetWorld = player.getServerWorld();
        player.networkHandler.sendPacket(new PlayerRespawnS2CPacket(targetWorld.getDimension(), targetWorld.getRegistryKey(), BiomeAccess.hashSeed(targetWorld.getSeed()), player.interactionManager.getGameMode(), player.interactionManager.getPreviousGameMode(), targetWorld.isDebugWorld(), targetWorld.isFlat(), true));
        player.networkHandler.requestTeleport(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
        player.server.getPlayerManager().sendCommandTree(player);
        player.networkHandler.sendPacket(new ExperienceBarUpdateS2CPacket(player.experienceProgress, player.totalExperience, player.experienceLevel));
        player.networkHandler.sendPacket(new HealthUpdateS2CPacket(player.getHealth(), player.getHungerManager().getFoodLevel(), player.getHungerManager().getSaturationLevel()));
        for (StatusEffectInstance statusEffect : player.getStatusEffects()) {
            player.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(player.getId(), statusEffect));
        }
        player.sendAbilitiesUpdate();
        playerManager.sendWorldInfo(player, targetWorld);
        playerManager.sendPlayerStatus(player);
    }

    /**
     * Sets the skin to the specified player and reloads it with {@link ServerPlayerEntityMixin_TailoredPlayer#reloadSkin()} reloadSkin().
     *
     * @param value skin texture value
     * @param signature skin texture signature
     * @param reload whether to reload skin
     * @return true if it was successful, otherwise false
     */
    public boolean setSkin(String value, String signature, boolean reload) {
        boolean result = false;

        try {
            this.map.removeAll("textures");
        } catch (Exception ignored) {
            // Player has no skin data, no worries
        }

        try {
            this.map.put("textures", new Property("textures", value, signature));

            // Saving skin data
            this.skinValue = value;
            this.skinSignature = signature;

            // Reloading skin
            if(reload)
                this.reloadSkin();

            result = true;
        } catch (InsecureTextureException ignored) {
            // No skin data
        } catch (Error e) {
            // Something went wrong when trying to set the skin
            errorLog(e.getMessage());
        }
        return result;
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


    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeCustomDataToNbt(NbtCompound tag, CallbackInfo ci) {
        if(this.getSkinValue() != null && this.getSkinSignature() != null) {
            NbtCompound skinDataTag = new NbtCompound();
            skinDataTag.putString("value", this.getSkinValue());
            skinDataTag.putString("signature", this.getSkinSignature());

            tag.put("fabrictailor:skin_data", skinDataTag);
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readCustomDataFromNbt(NbtCompound tag, CallbackInfo ci) {
        NbtCompound skinDataTag = tag.getCompound("fabrictailor:skin_data");
        if(skinDataTag != null) {
            this.skinValue = skinDataTag.contains("value") ? skinDataTag.getString("value") : null;
            this.skinSignature = skinDataTag.contains("signature") ? skinDataTag.getString("signature") : null;

            if(this.skinValue != null && this.skinSignature != null) {
                this.setSkin(this.skinValue, this.skinSignature, false);
            }
        }
    }
}
