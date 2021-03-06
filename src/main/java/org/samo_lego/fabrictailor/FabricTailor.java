package org.samo_lego.fabrictailor;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.samo_lego.fabrictailor.command.SkinCommand;

public class FabricTailor implements ModInitializer {

	private static final Logger LOGGER = LogManager.getLogger();

	@Override
	public void onInitialize() {
		// Registering /skin command
		CommandRegistrationCallback.EVENT.register(SkinCommand::register);
	}

	// Logging methods
	public static void errorLog(String error) {
		LOGGER.error("[FabricTailor] An error occurred: " + error);
	}
}
