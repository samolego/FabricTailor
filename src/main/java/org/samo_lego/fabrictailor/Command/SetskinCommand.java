package org.samo_lego.fabrictailor.Command;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.impl.networking.server.EntityTrackerStreamAccessor;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.text.LiteralText;
import net.minecraft.world.chunk.ChunkManager;
import org.samo_lego.fabrictailor.mixin.EntityTrackerAccessor;
import org.samo_lego.fabrictailor.mixin.ThreadedAnvilChunkStorageAccessor;

import java.util.Objects;

import static net.minecraft.server.command.CommandManager.literal;

public class SetskinCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean b) {
        dispatcher.register(literal("vanish")
                .requires(source -> source.hasPermissionLevel(4))
                .then(literal("toggle")
                        .executes(ctx -> toggleVanish(ctx.getSource().getPlayer()))
                )
                .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                            player.sendMessage(
                                    new LiteralText(
                                            player.isInvisible() ?
                                                    "§6You are vanished." :
                                                    "§6You're not vanished."
                                    ),
                                    true
                            );
                            return 1;
                        }
                )
        );
    }

    private static int toggleVanish(ServerPlayerEntity player) {

        // Storing vanished status in boolean to ease the use
        final boolean isVanished = player.isInvisible();

        // Sending player ADD/REMOVE packet
        Objects.requireNonNull(player.getServer()).getPlayerManager().sendToAll(
                new PlayerListS2CPacket(
                        isVanished ? PlayerListS2CPacket.Action.ADD_PLAYER : PlayerListS2CPacket.Action.REMOVE_PLAYER,
                        player
                )
        );

        ChunkManager manager = player.world.getChunkManager();
        assert manager instanceof ServerChunkManager;
        ThreadedAnvilChunkStorage storage = ((ServerChunkManager)manager).threadedAnvilChunkStorage;
        EntityTrackerAccessor trackerEntry = ((ThreadedAnvilChunkStorageAccessor) storage).getEntityTrackers().get(player.getEntityId());

        // Starting / stopping the player tracking
        ((EntityTrackerStreamAccessor) trackerEntry).fabric_getTrackingPlayers().forEach(
                tracking -> {
                    if (isVanished)
                        trackerEntry.getEntry().startTracking(tracking);
                    else
                        trackerEntry.getEntry().stopTracking(tracking);
                }
        );

        player.setInvisible(!isVanished);
        System.out.println("Player invisible: " + player.isInvisible());

        player.sendMessage(
                new LiteralText(
                        isVanished ?
                                "§6You are now unvanished." :
                                "§6Puff! You have vanished from the world."
                ),
                true
        );
        return 1;
    }
}
