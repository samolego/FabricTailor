package org.samo_lego.fabrictailor;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.samo_lego.fabrictailor.command.SkinCommand;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FabricTailor implements ModInitializer {

	private static final Logger LOGGER = LogManager.getLogger();

	public static ExecutorService THREADPOOL;

	@Override
	public void onInitialize() {
		// Registering /skin command
		CommandRegistrationCallback.EVENT.register(SkinCommand::register);

		// Stop server event
		ServerLifecycleEvents.SERVER_STOPPED.register(this::onStopServer);
		ServerLifecycleEvents.SERVER_STARTING.register(this::onStartServer);
	}

	// Logging methods
	public static void errorLog(String error) {
		LOGGER.error("[FabricTailor] An error occurred: " + error);
	}

	/**
	 * Called on server start.
	 */
	private void onStartServer(MinecraftServer server) {
		// Initialising executor service
		THREADPOOL = Executors.newCachedThreadPool();
	}
    /**
     * Called on server stop.
     * @param server server that is stopping
     */
    private void onStopServer(MinecraftServer server) {
        try {
            THREADPOOL.shutdownNow();
            if (!THREADPOOL.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                Thread.currentThread().interrupt();
            }
        } catch (InterruptedException e) {
            errorLog(e.getMessage());
            THREADPOOL.shutdownNow();
        }
    }
}
