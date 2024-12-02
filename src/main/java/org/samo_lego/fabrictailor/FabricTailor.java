package org.samo_lego.fabrictailor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.samo_lego.fabrictailor.command.FabrictailorCommand;
import org.samo_lego.fabrictailor.command.SkinCommand;
import org.samo_lego.fabrictailor.compatibility.CarpetFunctions;
import org.samo_lego.fabrictailor.config.TailorConfig;
import org.samo_lego.fabrictailor.network.NetworkHandler;
import org.samo_lego.fabrictailor.network.payload.DefaultSkinPayload;
import org.samo_lego.fabrictailor.network.payload.FabricTailorHelloPayload;
import org.samo_lego.fabrictailor.network.payload.HDSkinPayload;
import org.samo_lego.fabrictailor.network.payload.VanillaSkinPayload;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FabricTailor implements ModInitializer {

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
		ServerConfigurationConnectionEvents.CONFIGURE.register(NetworkHandler::onConfigured);
		
		
		PayloadTypeRegistry.configurationS2C().register(FabricTailorHelloPayload.TYPE, FabricTailorHelloPayload.CODEC);

		PayloadTypeRegistry.playC2S().register(VanillaSkinPayload.TYPE, VanillaSkinPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(VanillaSkinPayload.TYPE, NetworkHandler::changeVanillaSkinPacket);

		PayloadTypeRegistry.playC2S().register(HDSkinPayload.TYPE, HDSkinPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(HDSkinPayload.TYPE, NetworkHandler::changeHDSkinPacket);

		PayloadTypeRegistry.playC2S().register(DefaultSkinPayload.TYPE, DefaultSkinPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(DefaultSkinPayload.TYPE, NetworkHandler::defaultSkinPacket);
	}

	public static void reloadConfig() {
		TailorConfig newConfig = TailorConfig.loadConfigFile(configFile, !Minecraft.getInstance().isLocalServer());
		config.reload(newConfig);
	}
}
