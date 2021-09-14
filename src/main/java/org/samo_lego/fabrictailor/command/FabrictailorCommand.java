package org.samo_lego.fabrictailor.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import org.samo_lego.fabrictailor.FabricTailor;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;
import org.samo_lego.fabrictailor.util.TranslatedText;

import static net.minecraft.server.command.CommandManager.literal;
import static org.samo_lego.fabrictailor.FabricTailor.config;
import static org.samo_lego.fabrictailor.FabricTailor.configFile;

public class FabrictailorCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(literal("fabrictailor")
            .requires(src -> src.hasPermissionLevel(2))
            .then(literal("setDefaultSkin").executes(FabrictailorCommand::setDefaultSkin))
            .then(literal("reloadConfig").executes(FabrictailorCommand::reloadConfig))
        );
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

        config.saveConfigFile(configFile);

        context.getSource().sendFeedback(
                new TranslatedText("command.fabrictailor.config.defaultSkin").formatted(Formatting.GREEN),
                false
        );

        return 1;
    }
}
