package org.samo_lego.fabrictailor;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.dimension.DimensionType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.samo_lego.fabrictailor.Command.SetskinCommand;
import org.samo_lego.fabrictailor.event.PlayerJoinServerCallback;
import org.samo_lego.fabrictailor.event.TailorEventHandler;

public class FabricTailor implements DedicatedServerModInitializer {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final String MODID = "[FabricTailor]";


	//public static final ComponentType<SkinSaver> skinData = ComponentRegistry.INSTANCE.registerIfAbsent(new Identifier("fabrictailor:skindata"), SkinSaver.class);

	@Override
	public void onInitializeServer() {
		// Info that mod is loading
		log("Starting FabricTailor mod by samo_lego.");

		// Registering /skin command
		CommandRegistrationCallback.EVENT.register(SetskinCommand::register);

		// registering player join event
		PlayerJoinServerCallback.EVENT.register(TailorEventHandler::onPlayerJoin);

		// Add the component to every instance of PlayerEntity
		//EntityComponentCallback.event(PlayerEntity.class).register((player, components) -> components.put(skinData, new SkinSaver()));

	}

	// Logging methods
	public static void log(String msg) {
		LOGGER.info(MODID + " " + msg);
	}
	public static void errorLog(String msg) {
		LOGGER.error(MODID + " Error occured: "+ msg);
	}


	// Main method for setting player skin
	public static boolean setPlayerSkin(ServerPlayerEntity player, String value, String signature) {
		GameProfile gameProfile = player.getGameProfile();
		PropertyMap map = gameProfile.getProperties();

		try {
			Property property = map.get("textures").iterator().next();
			map.remove("textures", property);
		} catch (Exception ignored) {
			// Player has no skin data
		}
		map.put("textures", new Property("textures", value, signature));
		reloadSelfSkin(player);
		return true;
	}

	// Ugly reloading of player's gameprofile
	private static void reloadSelfSkin(ServerPlayerEntity player) {
		player.networkHandler.sendPacket(new PlayerListS2CPacket(PlayerListS2CPacket.Action.REMOVE_PLAYER, player));
		player.networkHandler.sendPacket(new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, player));

		player.networkHandler.sendPacket(new PlayerRespawnS2CPacket(
				DimensionType.THE_NETHER_REGISTRY_KEY,
				player.getEntityWorld().getRegistryKey(),
				0,
				player.interactionManager.getGameMode(),
				player.getEntityWorld().isDebugWorld(),
				false,
				true
		));
		player.networkHandler.sendPacket(new PlayerRespawnS2CPacket(
				DimensionType.OVERWORLD_REGISTRY_KEY,
				player.getEntityWorld().getRegistryKey(),
				0,
				player.interactionManager.getGameMode(),
				player.getEntityWorld().isDebugWorld(),
				false,
				true
		));
		player.teleport(player.getX(), player.getY(), player.getZ(), false);
		// update inventory
		player.inventory.updateItems(); //doesnt work
		player.playerScreenHandler.sendContentUpdates();
		player.currentScreenHandler.sendContentUpdates();
	}
}
