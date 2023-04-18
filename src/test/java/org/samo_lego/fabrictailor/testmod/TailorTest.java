package org.samo_lego.fabrictailor.testmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.samo_lego.fabrictailor.command.SkinCommand;
import org.samo_lego.fabrictailor.util.SkinFetcher;


public class TailorTest implements ModInitializer {
    @Override
    public void onInitialize() {
        // Add command for "/hdskin"
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            this.serversideSkinCmd(dispatcher);
        });
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, environment) -> {
            this.hdSkinCmd(dispatcher);
            this.capeCmd(dispatcher);
        });
    }

    private void hdSkinCmd(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("hdskin").executes(this::hdSkinCmd));
    }

    private int hdSkinCmd(CommandContext<FabricClientCommandSource> context) {
        return 0;
    }

    private void capeCmd(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("cape").executes(this::capeCmd));
    }

    private int capeCmd(CommandContext<FabricClientCommandSource> context) {
        return 0;
    }

    private void serversideSkinCmd(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("serverside_skin")
                .then(Commands.literal("player").executes(ctx -> {
                    SkinCommand.setSkin(ctx.getSource().getPlayer(), () -> SkinFetcher.fetchSkinByName("Notch"));
                    return 1;
                }))
                .then(Commands.literal("url").executes(ctx -> {
                    SkinCommand.setSkin(ctx.getSource().getPlayer(), () -> SkinFetcher.fetchSkinByUrl("https://skinmc.net/en/api/v1/skins/uuid/853c80ef-3c37-49fd-aa49-938b674adae6", false));
                    return 1;
                })));

    }
}
