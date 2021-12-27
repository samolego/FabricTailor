package org.samo_lego.fabrictailor;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.samo_lego.fabrictailor.command.FabrictailorCommand;
import org.samo_lego.fabrictailor.command.SkinCommand;
import org.samo_lego.fabrictailor.config.TailorConfig;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FabricTailor implements ModInitializer {

	private static final Logger LOGGER = LogManager.getLogger();
	public static final String MOD_ID = "fabrictailor";
	public static TailorConfig config;
	public static File configFile;
	public static final ExecutorService THREADPOOL = Executors.newCachedThreadPool();

	@Override
	public void onInitialize() {
		// Registering /skin command
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			SkinCommand.register(dispatcher, dedicated);
			FabrictailorCommand.register(dispatcher, dedicated);
		});

		configFile = new File(FabricLoader.getInstance().getConfigDir() + "/fabrictailor.json");
		reloadConfig();
		config.saveConfigFile(configFile);
	}

	public static void errorLog(String error) {
		LOGGER.error("[FabricTailor] An error occurred: " + error);
	}

	public static void reloadConfig() {
		// Ugly trick for server detection
		config = TailorConfig.loadConfigFile(configFile, new File("./server.properties").exists());
	}
}
