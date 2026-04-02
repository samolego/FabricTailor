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
import org.samo_lego.fabrictailor.config.TailorConfig;
import org.samo_lego.fabrictailor.util.TextTranslations;

import static net.minecraft.commands.Commands.literal;
import static org.samo_lego.fabrictailor.FabricTailor.config;

public class FabrictailorCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // Generate command for in-game editing
        config.generateReloadableConfigCommand(FabricTailor.MOD_ID, dispatcher, TailorConfig::new);

        var setDefautlSkin = literal("setDefaultSkin")
                        .requires(src -> Permissions.check(src, "fabrictailor.command.fabrictailor.setDefaultSkin", 2))
                        .executes(FabrictailorCommand::setDefaultSkin)
                .build();

        dispatcher.getRoot().getChild(FabricTailor.MOD_ID).addChild(setDefautlSkin);
    }

    private static int setDefaultSkin(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        config.defaultSkin.value = ((TailoredPlayer) player).fabrictailor_getSkinValue().orElse("");
        config.defaultSkin.signature = ((TailoredPlayer) player).fabrictailor_getSkinSignature().orElse("");

        config.save();

        context.getSource().sendSuccess(() ->
                        TextTranslations.create("command.fabrictailor.config.defaultSkin").withStyle(ChatFormatting.GREEN),
                false
        );

        return 1;
    }
}
