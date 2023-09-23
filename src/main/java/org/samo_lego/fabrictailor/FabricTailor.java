package org.samo_lego.fabrictailor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.samo_lego.fabrictailor.command.FabrictailorCommand;
import org.samo_lego.fabrictailor.command.SkinCommand;
import org.samo_lego.fabrictailor.compatibility.CarpetFunctions;
import org.samo_lego.fabrictailor.config.TailorConfig;
import org.samo_lego.fabrictailor.network.NetworkHandler;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.samo_lego.fabrictailor.network.SkinPackets.*;

public class FabricTailor implements ModInitializer {

	private static final Logger LOGGER = LogManager.getLogger();
	public static final String MOD_ID = "fabrictailor";
	public static TailorConfig config;
	public static File configFile;
	public static final ExecutorService THREADPOOL = Executors.newCachedThreadPool();

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> {
			// Registering /skin command
			SkinCommand.register(dispatcher);
			FabrictailorCommand.register(dispatcher);
		});

		configFile = new File(FabricLoader.getInstance().getConfigDir() + "/fabrictailor.json");
		config = TailorConfig.loadConfigFile(configFile, FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER);
		config.save();

		if (FabricLoader.getInstance().isModLoaded("carpet")) {
			CarpetFunctions.init();
		}

		ServerPlayConnectionEvents.INIT.register(NetworkHandler::onInit);
		ServerPlayNetworking.registerGlobalReceiver(FABRICTAILOR_VANILLA_CHANGE, NetworkHandler::changeVanillaSkinPacket);
		ServerPlayNetworking.registerGlobalReceiver(FABRICTAILOR_HD_CHANGE, NetworkHandler::changeHDSkinPacket);
		ServerPlayNetworking.registerGlobalReceiver(FABRICTAILOR_DEFAULT_SKIN, NetworkHandler::defaultSkinPacket);
	}

	public static void errorLog(String error) {
		LOGGER.error("[FabricTailor] An error occurred: " + error);
	}

	public static void reloadConfig() {
		// Ugly check if we are running server environment
		TailorConfig newConfig = TailorConfig.loadConfigFile(configFile, new File("./server.properties").exists());
		config.reload(newConfig);
	}
}
