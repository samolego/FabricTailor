package org.samo_lego.fabrictailor.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import org.samo_lego.fabrictailor.FabricTailor;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;
import org.samo_lego.fabrictailor.util.TranslatedText;

import static net.minecraft.commands.Commands.literal;
import static org.samo_lego.fabrictailor.FabricTailor.config;
import static org.samo_lego.fabrictailor.FabricTailor.configFile;

public class FabrictailorCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean dedicated) {
        dispatcher.register(literal("fabrictailor")
            .requires(src -> src.hasPermission(2))
            .then(literal("setDefaultSkin").executes(FabrictailorCommand::setDefaultSkin))
            .then(literal("reloadConfig").executes(FabrictailorCommand::reloadConfig))
        );

        LiteralCommandNode<ServerCommandSource> configNode = literal("config")
                .then(literal("reload")
                        .executes(FabrictailorCommand::reloadConfig)
                )
                .build();
        LiteralCommandNode<ServerCommandSource> editNode = literal("edit").build();

        // Generate command for in-game editing
        config.generateCommand(editNode);

        configNode.addChild(editNode);
        rootNode.addChild(configNode);
    }


    private static int reloadConfig(CommandContext<CommandSourceStack> context) {
        FabricTailor.reloadConfig();

        context.getSource().sendSuccess(
                new TranslatedText("command.fabrictailor.config.reloadSuccess").withStyle(ChatFormatting.GREEN),
                false
        );
        return 1;
    }


    private static int setDefaultSkin(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        config.defaultSkin.value = ((TailoredPlayer) player).getSkinValue();
        config.defaultSkin.signature = ((TailoredPlayer) player).getSkinSignature();

        config.save();

        context.getSource().sendSuccess(
                new TranslatedText("command.fabrictailor.config.defaultSkin").withStyle(ChatFormatting.GREEN),
                false
        );

        return 1;
    }
}
