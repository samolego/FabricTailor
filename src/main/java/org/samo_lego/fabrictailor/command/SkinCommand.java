package org.samo_lego.fabrictailor.command;

import com.mojang.authlib.properties.Property;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;
import org.samo_lego.fabrictailor.compatibility.TaterzensCompatibility;
import org.samo_lego.fabrictailor.util.SkinFetcher;
import org.samo_lego.fabrictailor.util.TranslatedText;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.command.argument.MessageArgumentType.getMessage;
import static net.minecraft.command.argument.MessageArgumentType.message;
import static net.minecraft.server.command.CommandManager.literal;
import static org.samo_lego.fabrictailor.util.SkinFetcher.*;

public class SkinCommand {
    private static final MutableText SKIN_SET_ERROR = new TranslatedText("command.fabrictailor.skin.set.404").formatted(Formatting.RED);
    private static final boolean TATERZENS_LOADED = FabricLoader.getInstance().isModLoaded("taterzens");;
    private static final TranslatedText SET_SKIN_ATTEMPT = new TranslatedText("command.fabrictailor.skin.set.attempt");

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(literal("skin")
            .then(literal("set")
                    .then(literal("URL")
                            .then(literal("classic")
                                    .then(CommandManager.argument("skin URL", message())
                                            .executes(context -> setSkinUrl(context, false))
                                    )
                            )
                            .then(literal("slim")
                                    .then(CommandManager.argument("skin URL", message())
                                            .executes(context -> setSkinUrl(context, true))
                                    )
                            )
                            .executes(ctx -> {
                                ctx.getSource().sendError(
                                        new TranslatedText("command.fabrictailor.skin.set.404.url").formatted(Formatting.RED)
                                );
                                return 1;
                            })
                    )
                    .then(literal("upload")
                            .then(literal("classic")
                                    .then(CommandManager.argument("skin file path", message())
                                            .executes(context -> setSkinFile(context, false))
                                    )
                            )
                            .then(literal("slim")
                                    .then(CommandManager.argument("skin file path", message())
                                            .executes(context -> setSkinFile(context, true))
                                    )
                            )
                            .executes(ctx -> {
                                ctx.getSource().sendError(
                                        new TranslatedText("command.fabrictailor.skin.set.404.path").formatted(Formatting.RED)
                                );
                                return 1;
                            })
                    )
                    .then(literal("player")
                            .then(CommandManager.argument("playername", greedyString())
                                    .executes(SkinCommand::setSkinPlayer)
                            )
                            .executes(ctx -> {
                                ctx.getSource().sendError(
                                        new TranslatedText("command.fabrictailor.skin.set.404.playername").formatted(Formatting.RED)
                                );
                                return 1;
                            })
                    )
                    .executes(ctx -> {
                        ctx.getSource().sendError(
                                new TranslatedText("command.fabrictailor.skin.set.404").formatted(Formatting.RED)
                        );
                        return 1;
                    })
            )
            .then(literal("clear").executes(context -> clearSkin(context.getSource().getPlayer()) ? 1 : 0))
        );
    }

    private static int setSkinUrl(CommandContext<ServerCommandSource> context, boolean useSlim) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        String skinUrl = getMessage(context, "skin URL").getString();

        player.sendMessage(SET_SKIN_ATTEMPT.formatted(Formatting.AQUA), false);
        Property skinData = fetchSkinByUrl(skinUrl, useSlim);
        if(skinData == null) {
            player.sendMessage(new TranslatedText("command.fabrictailor.skin.upload.malformed_url").formatted(Formatting.RED), false);
            return -1;
        }
        return setSkin(player, skinData) ? 1 : 0;
    }

    private static int setSkinFile(CommandContext<ServerCommandSource> context, boolean useSlim) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        String skinFilePath = getMessage(context, "skin file path").getString();

        // Warn about server path for uploads
        MinecraftServer server = player.getServer();
        if(server != null && server.isDedicated()) {
            player.sendMessage(
                    new TranslatedText("hint.fabrictailor.server_skin_path").formatted(Formatting.GOLD),
                    false
            );
        }

        player.sendMessage(new TranslatedText("command.fabrictailor.skin.please_wait").formatted(Formatting.GOLD),
                false
        );

        Property skinData = setSkinFromFile(skinFilePath, useSlim);
        if(skinData == null) {
            player.sendMessage(SKIN_SET_ERROR, false);
            return -1;
        }

        return setSkin(player, skinData) ? 1 : 0;
    }

    private static int setSkinPlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        String playername = getString(context, "playername");

        Property skinData = SkinFetcher.fetchSkinByName(playername);

        if(skinData == null) {
            player.sendMessage(SKIN_SET_ERROR, false);
            return -1;
        }
        return setSkin(player, skinData) ? 1 : 0;
    }

    private static boolean setSkin(ServerPlayerEntity player, @NotNull Property skinData) {
        MutableText result;
        boolean success = false;
        if(
                TATERZENS_LOADED && TaterzensCompatibility.setTaterzenSkin(player, skinData) ||
                        (((TailoredPlayer) player).setSkin(skinData, true))
        ) {
            result = new TranslatedText("command.fabrictailor.skin.set.success").formatted(Formatting.GREEN);
            success = true;
        } else {
            result = SKIN_SET_ERROR;
        }
        player.sendMessage(result, false);
        return success;
    }

    public static boolean clearSkin(ServerPlayerEntity player) {
        if(((TailoredPlayer) player).setSkin("", "", true)) {
            player.sendMessage(
                    new TranslatedText("command.fabrictailor.skin.clear.success").formatted(Formatting.GREEN),
                    false
            );
            return true;
        }

        player.sendMessage(new TranslatedText("command.fabrictailor.skin.clear.error").formatted(Formatting.RED),
                false
        );
        return false;
    }
}
