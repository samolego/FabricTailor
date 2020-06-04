package org.samo_lego.fabrictailor;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mineskin.MineskinClient;
import org.samo_lego.fabrictailor.Command.SkinCommand;
import org.samo_lego.fabrictailor.event.PlayerJoinServerCallback;
import org.samo_lego.fabrictailor.event.TailorEventHandler;

public class FabricTailor implements DedicatedServerModInitializer {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final String MODID = "[FabricTailor]";

	public static MineskinClient skinClient;

	public static final ComponentType<SkinSaver> skinData =
			ComponentRegistry.INSTANCE.registerIfAbsent(new Identifier("fabrictailor:skindata"), SkinSaver.class);

	@Override
	public void onInitializeServer() {
		// Info that mod is loading
		log("Starting FabricTailor mod by samo_lego.");

		// Registering /skin command
		CommandRegistrationCallback.EVENT.register(SkinCommand::register);

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
		System.out.println("Setting skin for : " + player.getName().asString());

		GameProfile gameProfile = player.getGameProfile();
		PropertyMap map = gameProfile.getProperties();

		try {
			Property property = map.get("textures").iterator().next();
			map.remove("textures", property);
		} catch (Exception ignored) {
			// Player has no skin data
		}
		map.put("textures", new Property("textures", value, signature));
		return true;
	}
}
