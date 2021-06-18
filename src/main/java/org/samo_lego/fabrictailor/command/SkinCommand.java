package org.samo_lego.fabrictailor.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import org.samo_lego.fabrictailor.util.TranslatedText;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.command.argument.MessageArgumentType.getMessage;
import static net.minecraft.command.argument.MessageArgumentType.message;
import static net.minecraft.server.command.CommandManager.literal;
import static org.samo_lego.fabrictailor.util.SkinFetcher.*;

public class SkinCommand {
    public static LiteralCommandNode<ServerCommandSource> skinNode;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        skinNode = dispatcher.register(literal("skin")
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

    static int setSkinUrl(CommandContext<ServerCommandSource> context, boolean useSlim) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        String skinUrl = getMessage(context, "skin URL").getString();

        fetchSkinByUrl(player, skinUrl, useSlim);

        return 0;
    }

    static int setSkinFile(CommandContext<ServerCommandSource> context, boolean useSlim) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        String skinFilePath = getMessage(context, "skin file path").getString();

        setSkinFromFile(player, skinFilePath, useSlim);

        return 0;
    }

    static int setSkinPlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        String playername = getString(context, "playername");

        fetchSkinByName(player, playername, true);

        return 0;
    }
}
