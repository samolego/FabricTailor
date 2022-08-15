package org.samo_lego.fabrictailor.command;

import com.mojang.authlib.properties.Property;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;
import org.samo_lego.fabrictailor.compatibility.TaterzenSkins;
import org.samo_lego.fabrictailor.util.SkinFetcher;
import org.samo_lego.fabrictailor.util.TranslatedText;

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
    private static final MutableComponent SKIN_SET_ERROR = new TranslatedText("command.fabrictailor.skin.set.404").withStyle(ChatFormatting.RED);
    private static final boolean TATERZENS_LOADED = FabricLoader.getInstance().isModLoaded("taterzens");;
    private static final TranslatedText SET_SKIN_ATTEMPT = new TranslatedText("command.fabrictailor.skin.set.attempt");

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean dedicated) {
        dispatcher.register(literal("skin")
            .then(literal("set")
                    .then(literal("URL")
                            .then(literal("classic")
                                    .then(Commands.argument("skin URL", message())
                                            .executes(context -> setSkinUrl(context, false))
                                    )
                            )
                            .then(literal("slim")
                                    .then(Commands.argument("skin URL", message())
                                            .executes(context -> setSkinUrl(context, true))
                                    )
                            )
                            .executes(ctx -> {
                                ctx.getSource().sendFailure(
                                        new TranslatedText("command.fabrictailor.skin.set.404.url").withStyle(ChatFormatting.RED)
                                );
                                return 1;
                            })
                    )
                    .then(literal("upload")
                            .then(literal("classic")
                                    .then(Commands.argument("skin file path", message())
                                            .executes(context -> setSkinFile(context, false))
                                    )
                            )
                            .then(literal("slim")
                                    .then(Commands.argument("skin file path", message())
                                            .executes(context -> setSkinFile(context, true))
                                    )
                            )
                            .executes(ctx -> {
                                ctx.getSource().sendFailure(
                                        new TranslatedText("command.fabrictailor.skin.set.404.path").withStyle(ChatFormatting.RED)
                                );
                                return 1;
                            })
                    )
                    .then(literal("player")
                            .then(Commands.argument("playername", greedyString())
                                    .executes(SkinCommand::setSkinPlayer)
                            )
                            .executes(ctx -> {
                                ctx.getSource().sendFailure(
                                        new TranslatedText("command.fabrictailor.skin.set.404.playername").withStyle(ChatFormatting.RED)
                                );
                                return 1;
                            })
                    )
                    .executes(ctx -> {
                        ctx.getSource().sendFailure(
                                new TranslatedText("command.fabrictailor.skin.set.404").withStyle(ChatFormatting.RED)
                        );
                        return 1;
                    })
            )
            .then(literal("clear").executes(context -> clearSkin(context.getSource().getPlayerOrException()) ? 1 : 0))
        );
    }

    private static int setSkinUrl(CommandContext<CommandSourceStack> context, boolean useSlim) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String skinUrl = getMessage(context, "skin URL").getString();

        player.displayClientMessage(SET_SKIN_ATTEMPT.withStyle(ChatFormatting.AQUA), false);
        THREADPOOL.submit(() -> {
            Property skinData = fetchSkinByUrl(skinUrl, useSlim);
            if(skinData == null) {
                player.displayClientMessage(new TranslatedText("command.fabrictailor.skin.upload.failed").withStyle(ChatFormatting.RED), false);
            } else {
                setSkin(player, skinData);
            }
        });

        return 1;
    }

    private static int setSkinFile(CommandContext<CommandSourceStack> context, boolean useSlim) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String skinFilePath = getMessage(context, "skin file path").getString();

        // Warn about server path for uploads
        MinecraftServer server = player.getServer();
        if(server != null && server.isDedicatedServer()) {
            player.displayClientMessage(
                    new TranslatedText("hint.fabrictailor.server_skin_path").withStyle(ChatFormatting.GOLD),
                    false
            );
        }

        player.displayClientMessage(new TranslatedText("command.fabrictailor.skin.please_wait").withStyle(ChatFormatting.GOLD),
                false
        );

        THREADPOOL.submit(() -> {
            Property skinData = setSkinFromFile(skinFilePath, useSlim);
            if(skinData == null) {
                player.displayClientMessage(SKIN_SET_ERROR, false);
            } else {
                setSkin(player, skinData);
            }
        });

        return 1;
    }

    private static int setSkinPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String playername = getString(context, "playername");

        THREADPOOL.submit(() -> {
            Property skinData = SkinFetcher.fetchSkinByName(playername);

            if(skinData == null) {
                player.displayClientMessage(SKIN_SET_ERROR, false);
            } else {
                setSkin(player, skinData);
            }
        });
        return 1;
    }

    private static void setSkin(ServerPlayer player, @NotNull Property skinData) {
        long lastChange = ((TailoredPlayer) player).getLastSkinChange();
        long now = System.currentTimeMillis();

        if(now - lastChange > config.skinChangeTimer * 1000 || lastChange == 0) {

            if (!TATERZENS_LOADED || !TaterzenSkins.setTaterzenSkin(player, skinData)) {
                ((TailoredPlayer) player).setSkin(skinData, true);
            }

            player.displayClientMessage(new TranslatedText("command.fabrictailor.skin.set.success").withStyle(ChatFormatting.GREEN), false);

        } else {
            // Prevent skin change spamming
            MutableComponent timeLeft = new TextComponent(String.valueOf((config.skinChangeTimer * 1000 - now + lastChange) / 1000))
                    .withStyle(ChatFormatting.LIGHT_PURPLE);
            player.displayClientMessage(
                    new TranslatedText("command.fabrictailor.skin.timer.please_wait", timeLeft)
                            .withStyle(ChatFormatting.RED),
                    false
            );
        }

    }

    public static boolean clearSkin(ServerPlayer player) {

        long lastChange = ((TailoredPlayer) player).getLastSkinChange();
        long now = System.currentTimeMillis();

        if(now - lastChange > config.skinChangeTimer * 1000 || lastChange == 0) {
            ((TailoredPlayer) player).clearSkin();
            player.displayClientMessage(
                    new TranslatedText("command.fabrictailor.skin.clear.success").withStyle(ChatFormatting.GREEN),
                    false
            );
            return true;
        }

        // Prevent skin change spamming
        MutableComponent timeLeft = new TextComponent(String.valueOf((config.skinChangeTimer * 1000 - now + lastChange) / 1000))
                .withStyle(ChatFormatting.LIGHT_PURPLE);
        player.displayClientMessage(
                new TranslatedText("command.fabrictailor.skin.timer.please_wait", timeLeft)
                        .withStyle(ChatFormatting.RED),
                false
        );
        ;
        return false;
    }
}
