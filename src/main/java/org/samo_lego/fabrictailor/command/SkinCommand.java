package org.samo_lego.fabrictailor.command;

import com.mojang.authlib.properties.Property;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;
import org.samo_lego.fabrictailor.compatibility.TaterzenSkins;
import org.samo_lego.fabrictailor.mixin.accessors.AEntitySelector;
import org.samo_lego.fabrictailor.util.SkinFetcher;
import org.samo_lego.fabrictailor.util.TextTranslations;

import java.util.function.Supplier;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.arguments.MessageArgument.getMessage;
import static net.minecraft.commands.arguments.MessageArgument.message;
import static org.samo_lego.fabrictailor.FabricTailor.THREADPOOL;
import static org.samo_lego.fabrictailor.FabricTailor.config;
import static org.samo_lego.fabrictailor.util.SkinFetcher.fetchSkinByUrl;
import static org.samo_lego.fabrictailor.util.SkinFetcher.setSkinFromFile;

public class SkinCommand {
    private static final MutableComponent SKIN_SET_ERROR = TextTranslations.create("command.fabrictailor.skin.set.404").withStyle(ChatFormatting.RED);
    private static final MutableComponent SET_SKIN_ATTEMPT = TextTranslations.create("command.fabrictailor.skin.set.attempt");

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("skin")
                .requires(src -> Permissions.check(src, "fabrictailor.command.skin", true))
                .then(literal("set")
                        .requires(src -> Permissions.check(src, "fabrictailor.command.skin.set", true))
                        .then(literal("URL")
                                .requires(src -> Permissions.check(src, "fabrictailor.command.skin.set.url", true))
                                .then(literal("classic")
                                        .requires(src -> Permissions.check(src, "fabrictailor.command.skin.set.url.classic", true))
                                        .then(Commands.argument("skin URL", message())
                                                .executes(context -> setSkinUrl(context, false))
                                        )
                                )
                                .then(literal("slim")
                                        .requires(src -> Permissions.check(src, "fabrictailor.command.skin.set.url.slim", true))
                                        .then(Commands.argument("skin URL", message())
                                            .executes(context -> setSkinUrl(context, true))
                                    )
                            )
                            .executes(ctx -> {
                                ctx.getSource().sendFailure(
                                        TextTranslations.create("command.fabrictailor.skin.set.404.url").withStyle(ChatFormatting.RED)
                                );
                                return 0;
                            })
                    )
                    .then(literal("upload")
                            .requires(src -> Permissions.check(src, "fabrictailor.command.skin.set.upload", true))
                            .then(literal("classic")
                                    .requires(src -> Permissions.check(src, "fabrictailor.command.skin.set.upload.classic", true))
                                    .then(Commands.argument("skin file path", message())
                                            .executes(context -> setSkinFile(context, false))
                                    )
                            )
                            .then(literal("slim")
                                    .requires(src -> Permissions.check(src, "fabrictailor.command.skin.set.upload.slim", true))
                                    .then(Commands.argument("skin file path", message())
                                            .executes(context -> setSkinFile(context, true))
                                    )
                            )
                            .executes(ctx -> {
                                ctx.getSource().sendFailure(
                                        TextTranslations.create("command.fabrictailor.skin.set.404.path").withStyle(ChatFormatting.RED)
                                );
                                return 0;
                            })
                    )
                    .then(literal("custom")
                            .requires(src -> !config.customSkinServer.isEmpty() && Permissions.check(src, "fabrictailor.command.skin.set.custom", true))
                            .then(literal("classic")
                                    .requires(src -> Permissions.check(src, "fabrictailor.command.skin.set.custom.classic", true))
                                    .then(Commands.argument("name", greedyString())
                                            .executes(context -> setSkinCustom(context, false))
                                    )
                            )
                            .then(literal("slim")
                                    .requires(src -> Permissions.check(src, "fabrictailor.command.skin.set.custom.slim", true))
                                    .then(Commands.argument("name", greedyString())
                                            .executes(context -> setSkinCustom(context, true))
                                    )
                            )
                            .executes(ctx -> {
                                ctx.getSource().sendFailure(
                                        TextTranslations.create("command.fabrictailor.skin.set.404.playername").withStyle(ChatFormatting.RED)
                                );
                                return 0;
                            })
                    )
                    .then(literal("player")
                            .requires(src -> Permissions.check(src, "fabrictailor.command.skin.set.player", true))
                            .then(Commands.argument("target", EntityArgument.player())
                                    .executes(SkinCommand::setSkinPlayer)
                            )
                            .executes(ctx -> {
                                ctx.getSource().sendFailure(
                                        TextTranslations.create("command.fabrictailor.skin.set.404.playername").withStyle(ChatFormatting.RED)
                                );
                                return 0;
                            })
                    )
                    .executes(ctx -> {
                        ctx.getSource().sendFailure(
                                TextTranslations.create("command.fabrictailor.skin.set.404").withStyle(ChatFormatting.RED)
                        );
                        return 0;
                    })
            )
                .then(literal("clear")
                        .requires(src -> Permissions.check(src, "fabrictailor.command.skin.clear", true))
                        .executes(context -> clearSkin(context.getSource().getPlayerOrException()) ? 1 : 0))
        );
    }

    private static int setSkinCustom(CommandContext<CommandSourceStack> context, boolean useSlim) throws CommandSyntaxException {
        final ServerPlayer player = context.getSource().getPlayerOrException();
        final String playername = getString(context, "name");

        final String skinUrl = config.customSkinServer.replace("{player}", playername);

        setSkin(player, () -> fetchSkinByUrl(skinUrl, useSlim));
        return 1;
    }

    private static int setSkinUrl(CommandContext<CommandSourceStack> context, boolean useSlim) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String skinUrl = getMessage(context, "skin URL").getString();

        setSkin(player, () -> fetchSkinByUrl(skinUrl, useSlim));
        return 1;
    }

    private static int setSkinFile(CommandContext<CommandSourceStack> context, boolean useSlim) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String skinFilePath = getMessage(context, "skin file path").getString();

        // Warn about server path for uploads
        MinecraftServer server = player.getServer();
        if (server != null && server.isDedicatedServer() && config.logging.skinChangeFeedback) {
                player.displayClientMessage(
                    TextTranslations.create("hint.fabrictailor.server_skin_path").withStyle(ChatFormatting.GOLD),
                    false
            );
        }

        setSkin(player, () -> setSkinFromFile(skinFilePath, useSlim));
        
        if (config.logging.skinChangeFeedback) {
            player.displayClientMessage(TextTranslations.create("command.fabrictailor.skin.please_wait").withStyle(ChatFormatting.GOLD),
                    false
            );
        }
        
        return 1;
    }

    private static int setSkinPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        EntitySelector selector = context.getArgument("target", EntitySelector.class);
        String input = ((AEntitySelector) selector).getPlayerName();

        try {
            String name = selector.findSinglePlayer(context.getSource()).getScoreboardName();
            setSkin(player, () -> SkinFetcher.fetchSkinByName(name));
        } catch (CommandSyntaxException e) {
            if (input == null) throw e;
            setSkin(player, () -> SkinFetcher.fetchSkinByName(input));
        }

        return 1;
    }

    public static void setSkin(ServerPlayer player, Supplier<Property> skinProvider) {
        long lastChange = ((TailoredPlayer) player).fabrictailor_getLastSkinChange();
        long now = System.currentTimeMillis();

        if (now - lastChange > config.skinChangeTimer * 1000 || lastChange == 0) {
            player.displayClientMessage(SET_SKIN_ATTEMPT.withStyle(ChatFormatting.AQUA), false);
            THREADPOOL.submit(() -> {
                Property skinData = skinProvider.get();

                if (skinData == null) {
                    player.displayClientMessage(SKIN_SET_ERROR, false);
                } else {
                    ((TailoredPlayer) player).fabrictailor_setSkin(skinData, true);
                    
                    if (config.logging.skinChangeFeedback) {
                        player.displayClientMessage(TextTranslations.create("command.fabrictailor.skin.set.success").withStyle(ChatFormatting.GREEN), false);
                    }
                }
            });
        } else {
            // Prevent skin change spamming
            MutableComponent timeLeft = Component.literal(String.valueOf((config.skinChangeTimer * 1000 - now + lastChange) / 1000))
                    .withStyle(ChatFormatting.LIGHT_PURPLE);
            player.displayClientMessage(
                    TextTranslations.create("command.fabrictailor.skin.timer.please_wait", timeLeft)
                            .withStyle(ChatFormatting.RED),
                    false
            );
        }

    }

    public static boolean clearSkin(ServerPlayer player) {

        long lastChange = ((TailoredPlayer) player).fabrictailor_getLastSkinChange();
        long now = System.currentTimeMillis();

        if (now - lastChange > config.skinChangeTimer * 1000 || lastChange == 0) {
            ((TailoredPlayer) player).fabrictailor_clearSkin();
            if (config.logging.skinChangeFeedback) {
                player.displayClientMessage(
                        TextTranslations.create("command.fabrictailor.skin.clear.success").withStyle(ChatFormatting.GREEN),
                        false
                );
            }
            
            return true;
        }

        // Prevent skin change spamming
        MutableComponent timeLeft = Component.literal(String.valueOf((config.skinChangeTimer * 1000 - now + lastChange) / 1000))
                .withStyle(ChatFormatting.LIGHT_PURPLE);
        player.displayClientMessage(
                TextTranslations.create("command.fabrictailor.skin.timer.please_wait", timeLeft)
                        .withStyle(ChatFormatting.RED),
                false
        );
        return false;
    }
}
