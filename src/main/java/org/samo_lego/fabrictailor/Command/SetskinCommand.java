package org.samo_lego.fabrictailor.Command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

public class SetskinCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean b) {
        dispatcher.register(CommandManager.literal("setskin")
                .executes(ctx -> {
                    Entity player = ctx.getSource().getEntityOrThrow();
                    player.sendSystemMessage(
                        new LiteralText(
                                "§6/setskin is deprecated. Please use /skin"
                        ),
                        player.getUuid()
                    );
                    return 1;
                })
        );
    }
}
