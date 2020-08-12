package org.samo_lego.fabrictailor;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.event.EntityComponentCallback;
import nerdhub.cardinal.components.api.util.EntityComponents;
import nerdhub.cardinal.components.api.util.RespawnCopyStrategy;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.impl.networking.server.EntityTrackerStreamAccessor;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.chunk.ChunkManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.samo_lego.fabrictailor.Command.SetskinCommand;
import org.samo_lego.fabrictailor.Command.SkinCommand;
import org.samo_lego.fabrictailor.event.PlayerJoinServerCallback;
import org.samo_lego.fabrictailor.mixin.EntityTrackerAccessor;
import org.samo_lego.fabrictailor.mixin.ThreadedAnvilChunkStorageAccessor;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.samo_lego.fabrictailor.event.TailorEventHandler.onPlayerJoin;

public class FabricTailor implements ModInitializer {
	public static final String MODID = "fabrictailor";

	private static final Logger LOGGER = LogManager.getLogger();

	public static final ExecutorService THREADPOOL = Executors.newCachedThreadPool();

	private static final ComponentType<SkinSaveData> SKIN_DATA = ComponentRegistry.INSTANCE.registerIfAbsent(new Identifier(MODID,"skin_data"), SkinSaveData.class);

	@Override
	public void onInitialize() {
		// Registering /skin command
		CommandRegistrationCallback.EVENT.register(SetskinCommand::register);
		CommandRegistrationCallback.EVENT.register(SkinCommand::register);

		// Registering player join event
		// It passes the skin data to method as well, in order to apply skin at join
		PlayerJoinServerCallback.EVENT.register(player -> onPlayerJoin(player, SKIN_DATA.get(player).getValue(), SKIN_DATA.get(player).getSignature()));


		// Add the component to every instance of PlayerEntity
		EntityComponentCallback.event(PlayerEntity.class).register((player, components) -> components.put(SKIN_DATA, new SkinSaver("", "")));
		EntityComponents.setRespawnCopyStrategy(SKIN_DATA, RespawnCopyStrategy.ALWAYS_COPY);
	}

	// Logging methods
	public static void infoLog(String info) {
		LOGGER.info("[FabricTailor] " + info);
	}
	public static void errorLog(String error) {
		LOGGER.error("[FabricTailor] An error occurred: " + error);
	}

	// Main method for setting player skin

	/**
	 * Sets the skin to the specified player and reloads it with {@link org.samo_lego.fabrictailor.FabricTailor#reloadSkin(ServerPlayerEntity)}
	 * @param player player whose skin needs to be changed
	 * @param value skin texture value
	 * @param signature skin texture signature
	 * @return true if it was successful, otherwise false
	 */
	public static boolean setPlayerSkin(ServerPlayerEntity player, String value, String signature) {
		boolean result;
		GameProfile gameProfile = player.getGameProfile();
		PropertyMap map = gameProfile.getProperties();

		try {
			Property property = map.get("textures").iterator().next();
			map.remove("textures", property);
		} catch (Exception ignored) {
			// Player has no skin data, no worries
		}

		try {
			if(!value.equals("") && !signature.equals(""))
				map.put("textures", new Property("textures", value, signature));

			// Reloading is needed in order to see the new skin
			reloadSkin(player);

			// We need to save data as well
			// Cardinal Components
			// Thanks Pyro and UPcraft for helping me out :)
			SKIN_DATA.get(player).setValue(value);
			SKIN_DATA.get(player).setSignature(signature);

			result = true;
		} catch (Error e) {
			// Something went wrong when trying to set the skin
			errorLog(e.getMessage());
			result = false;
		}

		return result;
	}

	/**
	 * <p>
	 * This method has been adapted from the Impersonate mod's <a href="https://github.com/Ladysnake/Impersonate/blob/1.16/src/main/java/io/github/ladysnake/impersonate/impl/ServerPlayerSkins.java">source code</a>
	 * under GNU Lesser General Public License.
	 *
	 * Reloads player's skin for all the players (including the one that has changed the skin)
	 * @param player player that wants to have the skin reloaded
	 *
	 * @author Pyrofab
	 */
	private static void reloadSkin(ServerPlayerEntity player) {
		// Refreshing tablist for each player
		PlayerManager playerManager = Objects.requireNonNull(player.getServer()).getPlayerManager();
		playerManager.sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.REMOVE_PLAYER, player));
		playerManager.sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, player));

		ChunkManager manager = player.world.getChunkManager();
		assert manager instanceof ServerChunkManager;
		ThreadedAnvilChunkStorage storage = ((ServerChunkManager)manager).threadedAnvilChunkStorage;
		EntityTrackerAccessor trackerEntry = ((ThreadedAnvilChunkStorageAccessor) storage).getEntityTrackers().get(player.getEntityId());

		((EntityTrackerStreamAccessor) trackerEntry).fabric_getTrackingPlayers().forEach(tracking -> trackerEntry.getEntry().startTracking(tracking));

		// need to change the player entity on the client
		ServerWorld targetWorld = (ServerWorld) player.world;
		player.networkHandler.sendPacket(new PlayerRespawnS2CPacket(targetWorld.getDimension(), targetWorld.getRegistryKey(), BiomeAccess.hashSeed(targetWorld.getSeed()), player.interactionManager.getGameMode(), player.interactionManager.method_30119(), targetWorld.isDebugWorld(), targetWorld.isFlat(), true));
		player.networkHandler.requestTeleport(player.getX(), player.getY(), player.getZ(), player.yaw, player.pitch);
		player.server.getPlayerManager().sendCommandTree(player);
		player.networkHandler.sendPacket(new ExperienceBarUpdateS2CPacket(player.experienceProgress, player.totalExperience, player.experienceLevel));
		player.networkHandler.sendPacket(new HealthUpdateS2CPacket(player.getHealth(), player.getHungerManager().getFoodLevel(), player.getHungerManager().getSaturationLevel()));
		for (StatusEffectInstance statusEffect : player.getStatusEffects()) {
			player.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(player.getEntityId(), statusEffect));
		}
		player.sendAbilitiesUpdate();
		player.server.getPlayerManager().sendWorldInfo(player, targetWorld);
		player.server.getPlayerManager().sendPlayerStatus(player);
	}
}
