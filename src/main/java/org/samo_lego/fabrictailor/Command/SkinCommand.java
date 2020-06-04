package org.samo_lego.fabrictailor.Command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.net.MalformedURLException;
import java.net.URL;

import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.command.arguments.MessageArgumentType.getMessage;
import static net.minecraft.command.arguments.MessageArgumentType.message;
import static org.samo_lego.fabrictailor.FabricTailor.setPlayerSkin;
import static org.samo_lego.fabrictailor.FabricTailor.skinClient;

public class SkinCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean b) {
        dispatcher.register(CommandManager.literal("setskin")
            .then(CommandManager.argument("URL", message())
                    .executes(ctx -> skin(ctx.getSource(), getMessage(ctx, "URL").getString(), false))
                    .then(CommandManager.argument("slim", string())
                            .executes(ctx -> skin(ctx.getSource(), StringArgumentType.getString(ctx, "URL"), true)))
            )
            .executes(ctx -> {
                Entity player = ctx.getSource().getEntityOrThrow();
                player.sendSystemMessage(
                    new LiteralText(
                            "You have to provide URL of the skin."
                    ),
                    player.getUuid()
                );
                return 1;
            })
        );
    }

    private static int skin(ServerCommandSource source, String skinUrl, boolean slim) throws CommandSyntaxException {
        Entity player = source.getEntityOrThrow();
        int returnValue = 1;
        try {
            URL url = new URL(skinUrl);

            //System.out.println(skinClient.generateUrl(url));
            //SkinOptions.create("", slim ? Model.SLIM : Model.DEFAULT, Visibility.PRIVATE),
            skinClient.generateUrl(skinUrl, skin -> {
                System.out.println("Setting skin " + skinUrl);
                if(setPlayerSkin((ServerPlayerEntity) player, skin.data.texture.value, skin.data.texture.signature)) {
                    player.sendSystemMessage(
                            new LiteralText(
                                    "§aSkin data set."
                            ),
                            player.getUuid()
                    );
                }

            });
        } /*catch (IOException e) {
            player.sendSystemMessage(
                    new LiteralText(
                            "§cUnexpected exception"
                    ),
                    player.getUuid()
            );
            errorLog("Unexpected IOException while creating skin " + e.getMessage());
            returnValue = -1;
        }*/ catch (MalformedURLException e) {
            player.sendSystemMessage(
                new LiteralText(
                        "§cMalformed url!"
                ),
                player.getUuid()
            );
            returnValue = -1;
        }
        return returnValue;
    }

}
