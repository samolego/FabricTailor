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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.samo_lego.fabrictailor.Command.SetskinCommand;
import org.samo_lego.fabrictailor.event.PlayerJoinServerCallback;

import java.util.Iterator;
import java.util.Objects;

import static org.samo_lego.fabrictailor.event.TailorEventHandler.onPlayerJoin;

public class FabricTailor implements ModInitializer {
	public static final String MODID = "fabrictailor";
	private static final Logger LOGGER = LogManager.getLogger();

	private static final ComponentType<SkinSaveData> SKIN_DATA = ComponentRegistry.INSTANCE.registerIfAbsent(new Identifier(MODID,"skin_data"), SkinSaveData.class);

	@Override
	public void onInitialize() {
		// Registering /skin command
		CommandRegistrationCallback.EVENT.register(SetskinCommand::register);

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
	 * Reloads player's skin for all the players (including the one that has changed the skin)
	 * @param player player that wants to have the skin reloaded
	 */
	private static void reloadSkin(ServerPlayerEntity player) {

		for(ServerPlayerEntity other : Objects.requireNonNull(player.getServer()).getPlayerManager().getPlayerList()) {
			// Refreshing tablist for each player
			other.networkHandler.sendPacket(new PlayerListS2CPacket(PlayerListS2CPacket.Action.REMOVE_PLAYER, other));
			other.networkHandler.sendPacket(new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, other));

			if(player == other) {
				// We found the player whose skin was changed, we need to change his dimension
				// in order for him to be able to see new skin
				ServerWorld world = player.getServerWorld();

				// Getting world which is different from player's world
				Iterator<ServerWorld> worlds = Objects.requireNonNull(player.getServer()).getWorlds().iterator();
				ServerWorld newWorld = null;

				while(worlds.hasNext()) {
					newWorld = worlds.next();
					if(newWorld != world)
						break;
				}

				// Changing dimension
				player.teleport(newWorld, player.getX(), player.getY(), player.getZ(), player.yaw, player.pitch);
				player.teleport(world, player.getX(), player.getY(), player.getZ(), player.yaw, player.pitch);
				continue;
			}

			// This might seem redundant but it's actually needed
			// Refreshing tablist in order to update player's skin data
			// Needed just for all players but the one that has changed the skin
			other.networkHandler.sendPacket(new PlayerListS2CPacket(PlayerListS2CPacket.Action.REMOVE_PLAYER, player));
			other.networkHandler.sendPacket(new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, player));
		}
	}
}
