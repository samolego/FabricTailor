package org.samo_lego.fabrictailor.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import org.samo_lego.fabrictailor.FabricTailor;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;
import org.samo_lego.fabrictailor.util.TranslatedText;

import static net.minecraft.server.command.CommandManager.literal;
import static org.samo_lego.fabrictailor.FabricTailor.config;

public class FabrictailorCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        LiteralCommandNode<ServerCommandSource> rootNode = dispatcher.register(literal("fabrictailor")
                .requires(src -> src.hasPermissionLevel(2))
                .then(literal("setDefaultSkin").executes(FabrictailorCommand::setDefaultSkin))
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


    private static int reloadConfig(CommandContext<ServerCommandSource> context) {
        FabricTailor.reloadConfig();

        context.getSource().sendFeedback(
                new TranslatedText("command.fabrictailor.config.reloadSuccess").formatted(Formatting.GREEN),
                false
        );
        return 1;
    }


    private static int setDefaultSkin(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        config.defaultSkin.value = ((TailoredPlayer) player).getSkinValue();
        config.defaultSkin.signature = ((TailoredPlayer) player).getSkinSignature();

        config.save();

        context.getSource().sendFeedback(
                new TranslatedText("command.fabrictailor.config.defaultSkin").formatted(Formatting.GREEN),
                false
        );

        return 1;
    }
}
