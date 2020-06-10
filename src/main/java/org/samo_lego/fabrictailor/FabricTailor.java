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
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.samo_lego.fabrictailor.Command.SetskinCommand;
import org.samo_lego.fabrictailor.event.PlayerJoinServerCallback;

import static org.samo_lego.fabrictailor.event.TailorEventHandler.onPlayerJoin;

public class FabricTailor implements ModInitializer {
	public static final String MODID = "fabrictailor";
	private static final Logger LOGGER = LogManager.getLogger();

	private static final ComponentType<SkinSaveData> SKIN_DATA = ComponentRegistry.INSTANCE.registerIfAbsent(new Identifier(MODID,"skin_data"), SkinSaveData.class);

	@Override
	public void onInitialize() {
		// Info that mod is loading
		infoLog("Starting FabricTailor mod by samo_lego.");

		// Registering /skin command
		CommandRegistrationCallback.EVENT.register(SetskinCommand::register);

		// registering player join event
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
		LOGGER.error("[FabricTailor] " + error);
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

		// We need to save data as well
		// Cardinal Components
		// Thanks Pyro and UPcraft for helping me out :)
		SKIN_DATA.get(player).setValue(value);
		SKIN_DATA.get(player).setSignature(signature);

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
		//player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-1, -1, player.inventory.getMainHandStack()));
		//player.inventory.insertStack(ItemStack.EMPTY);
		//player.inventory.updateItems(); //doesnt work
		//player.playerScreenHandler.sendContentUpdates(); //doesnt work
		//player.currentScreenHandler.sendContentUpdates(); //doesnt work
	}
}
