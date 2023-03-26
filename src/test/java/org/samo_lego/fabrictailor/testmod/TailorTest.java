package org.samo_lego.fabrictailor.testmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import org.samo_lego.fabrictailor.command.SkinCommand;
import org.samo_lego.fabrictailor.util.SkinFetcher;

import static net.minecraft.commands.Commands.literal;

public class TailorTest implements ModInitializer {
    @Override
    public void onInitialize() {
        // Add command for "/hdskin"
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            this.hdSkinCmd(dispatcher);
            this.capeCmd(dispatcher);
            this.serversideSkinCmd(dispatcher);
        });
    }

    private void hdSkinCmd(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("hdskin").executes(this::hdSkinCmd));
    }

    private int hdSkinCmd(CommandContext<CommandSourceStack> context) {
        return 0;
    }

    private void capeCmd(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("cape").executes(this::capeCmd));
    }

    private int capeCmd(CommandContext<CommandSourceStack> context) {
        return 0;
    }

    private void serversideSkinCmd(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("serverside_skin")
                .then(literal("player").executes(ctx-> {
                    SkinCommand.setSkin(ctx.getSource().getPlayer(), () -> SkinFetcher.fetchSkinByName("Notch"));
                    return 1;
                }))
                .then(literal("url").executes(ctx-> {
                    SkinCommand.setSkin(ctx.getSource().getPlayer(), () -> SkinFetcher.fetchSkinByUrl("https://skinmc.net/en/api/v1/skins/uuid/853c80ef-3c37-49fd-aa49-938b674adae6", false));
                    return 1;
                })));

    }
}
