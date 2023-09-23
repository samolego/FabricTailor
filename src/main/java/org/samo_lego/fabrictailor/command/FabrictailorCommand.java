package org.samo_lego.fabrictailor.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import org.samo_lego.fabrictailor.FabricTailor;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;
import org.samo_lego.fabrictailor.util.TextTranslations;

import static net.minecraft.commands.Commands.literal;
import static org.samo_lego.fabrictailor.FabricTailor.config;

public class FabrictailorCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> root = dispatcher.register(literal("fabrictailor")
                .requires(src -> Permissions.check(src, "fabrictailor.command.fabrictailor", 2))
                .then(literal("setDefaultSkin")
                        .requires(src -> Permissions.check(src, "fabrictailor.command.fabrictailor.setDefaultSkin", 2))
                        .executes(FabrictailorCommand::setDefaultSkin))
        );

        LiteralCommandNode<CommandSourceStack> configNode = literal("config")
                .requires(src -> Permissions.check(src, "fabrictailor.command.fabrictailor.config", 2))
                .then(literal("reload")
                        .requires(src -> Permissions.check(src, "fabrictailor.command.fabrictailor.config.reload", 2))
                        .executes(FabrictailorCommand::reloadConfig)
                )
                .build();
        LiteralCommandNode<CommandSourceStack> editNode = literal("edit")
                .requires(src -> Permissions.check(src, "fabrictailor.command.fabrictailor.config.edit", 2))
                .build();

        // Generate command for in-game editing
        config.generateCommand(editNode);

        configNode.addChild(editNode);
        root.addChild(configNode);
    }


    private static int reloadConfig(CommandContext<CommandSourceStack> context) {
        FabricTailor.reloadConfig();

        context.getSource().sendSuccess(() ->
                        TextTranslations.create("command.fabrictailor.config.reloadSuccess").withStyle(ChatFormatting.GREEN),
                false
        );
        return 1;
    }


    private static int setDefaultSkin(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        config.defaultSkin.value = ((TailoredPlayer) player).getSkinValue();
        config.defaultSkin.signature = ((TailoredPlayer) player).getSkinSignature();

        config.save();

        context.getSource().sendSuccess(() ->
                        TextTranslations.create("command.fabrictailor.config.defaultSkin").withStyle(ChatFormatting.GREEN),
                false
        );

        return 1;
    }
}
