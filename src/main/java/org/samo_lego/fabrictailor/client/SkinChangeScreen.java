package org.samo_lego.fabrictailor.client;

import carpet.script.language.Sys;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.io.File;

import org.samo_lego.fabrictailor.client.SkinTab.TailorStyle;


@Environment(EnvType.CLIENT)
public class SkinChangeScreen extends Screen {

    public static final int BUTTON_HEIGHT = 20;
    public static final int BUTTON_WIDTH = 100;
    private static final Identifier TABS_TEXTURE = new Identifier("textures/gui/advancements/tabs.png");
    private static final Identifier BACKGROUND_TEXTURE = new Identifier("textures/gui/advancements/window.png");

    private static final SkinTab[] TABS = new SkinTab[]{
            new SkinTab(TailorStyle.PLAYER, 27, 28),
            new SkinTab(TailorStyle.URL, 27, 28),
            new SkinTab(TailorStyle.LOCAL, 27, 28)
    };

    private TextFieldWidget skinInput;
    private int startX;
    private int startY;
    private SkinTab selectedTab;

    protected SkinChangeScreen() {
        super(new TranslatableText("gui.fabrictailor.change_skin"));
    }

    @Override
    protected void init() {
        super.init();
        this.client.keyboard.setRepeatEvents(true);
        int verticalSpacing = 8;

        skinInput = new TextFieldWidget(this.textRenderer, width / 2, height / 2, BUTTON_WIDTH, 14, new TranslatableText("itemGroup.search"));
        skinInput.setMaxLength(256);
        skinInput.setVisible(true);
        skinInput.setHasBorder(true);
        skinInput.setEditableColor(16777215);

        addChild(
                skinInput
        );

        addButton(
                new ButtonWidget(
                        width / 2,
                        height / 2 + 28,
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT,
                        new TranslatableText("button.fabrictailor.set_skin"),
                        onClick -> {
                            String skin = skinInput.getText();

                            if(this.client.getServer() != null) {
                                // Integrated server - singleplayer
                                System.out.println("Skin: " + skin);
                                System.out.println("Client!");

                                // Works, but ugly
                                client.player.networkHandler.sendPacket(new ChatMessageC2SPacket(this.selectedTab.getSkinCommand() + skin));
                            }
                            else {
                                // Multiplayer - send command or client-side skin
                                client.player.networkHandler.sendPacket(new ChatMessageC2SPacket(this.selectedTab.getSkinCommand() + skin));
                            }
                            this.onClose();
                        }
                )
        );

        addButton(
                new ButtonWidget(
                        width / 2 - BUTTON_WIDTH - 2, height - BUTTON_HEIGHT - verticalSpacing,
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT,
                        new TranslatableText("button.fabrictailor.clear_skin"),
                        onClick -> {
                            this.onClose();
                        }
                )
        );
        addButton(
                new ButtonWidget(
                        width / 2 + 2, height - BUTTON_HEIGHT - verticalSpacing,
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT,
                        ScreenTexts.CANCEL,
                        onClick -> {
                            this.onClose();
                        }
                )
        );


        addButton(
                new ButtonWidget(
                        this.width / 2 - 154,
                        this.height - 48,
                        150,
                        20,
                        new TranslatableText("button.fabrictailor.choose_skin"),
                        (buttonWidget) -> {
                            Util.getOperatingSystem().open(new File(""));
                        },
                        (buttonWidget, matrixStack, i, j) -> {
                            this.renderTooltip(matrixStack, new TranslatableText("Tooltip"), i, j);
                        }
                )
        );
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        //renderBackgroundTexture(0); // renders dirt background
        this.renderBackground(matrixStack, 0);

        super.render(matrixStack, mouseX, mouseY, delta);
        drawCenteredText(matrixStack, textRenderer, title, width / 2, 15, 0xffffff);

        this.startX = (this.width - 252) / 2;
        this.startY = (this.height - 140) / 2;

        // Background
        this.client.getTextureManager().bindTexture(BACKGROUND_TEXTURE);
        this.drawTexture(matrixStack, startX, startY, 0, 0, 252, 140);

        // Drawing Player
        // Luckily vanilla code is available
        InventoryScreen.drawEntity(startX + 50, startY + 120, 50, (float)width / 2 - 75 - mouseX, (float)height / 2 - mouseY, client.player);

        // Render input field
        skinInput.render(matrixStack, startX, startY, delta);


        // Other renders
        this.drawTabs(matrixStack, startX, startY, mouseX, mouseY);
        this.drawIcons(matrixStack, startX, startY, mouseX, mouseY);
        this.drawWidgetTooltip(matrixStack, startX, startY, mouseX, mouseY);
    }

    private void drawTabs(MatrixStack matrixStack, int startX, int startY, int mouseX, int mouseY) {
        // Setting texture
        this.client.getTextureManager().bindTexture(TABS_TEXTURE);

        // Tabs
        for(int i = 0; i < TABS.length; ++i) {
            SkinTab tab = TABS[i];

            if(selectedTab == null) {
                this.selectedTab = tab;
            }
            else if(selectedTab == tab) {
                this.drawTexture(matrixStack, startX + 224 - i * 27, startY - 28, i == 0 ? 56 : 28, 32, 28, 32);
            }
            else {
                this.drawTexture(matrixStack, startX + 224 - i * 27, startY - 28, i == 0 ? 56 : 28, 0, 28, i == 0 ? 31 : 29);
            }
        }
        this.textRenderer.drawWithShadow(matrixStack, this.selectedTab.getTitle(), startX + 10, startY + 5, 16777215);
        this.textRenderer.drawWithShadow(matrixStack, this.selectedTab.getDescription(), (float) width / 2, (float) height / 2 - 10, 16777215);


        RenderSystem.defaultBlendFunc();
    }


    private void drawIcons(MatrixStack matrixStack, int startX, int startY, int mouseX, int mouseY) {
        // Icons
        for(int i = 0; i < TABS.length; ++i) {
            SkinTab tab = TABS[i];
            itemRenderer.renderInGui(tab.getIcon(), startX + 231 - i * 27, startY - 18);
        }
        RenderSystem.disableBlend();
    }


    private void drawWidgetTooltip(MatrixStack matrixStack, int startX, int startY, int mouseX, int mouseY) {
        /*if (this.selectedTab != null) {
            RenderSystem.enableDepthTest();
            this.selectedTab.drawWidgetTooltip(matrixStack, i - k - 9, j - l - 18, k, l);
            RenderSystem.disableDepthTest();
        }*/

        for(int i = 0; i < TABS.length; ++i) {
            SkinTab tab = TABS[i];
            if(tab.isSelected(startX + 225 - i * 27, startY - 28, mouseX, mouseY)) {
                this.renderTooltip(matrixStack, tab.getTitle(), mouseX, mouseY);
            }
        }
    }

    @Override
    public void renderBackground(MatrixStack matrices) {
        this.renderBackground(matrices, 0);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for(int i = 0; i < TABS.length; ++i) {
                SkinTab tab = TABS[i];
                if(tab.isSelected(startX + 225 - i * 27, startY - 28, (int) mouseX, (int) mouseY)) {
                    this.selectedTab = tab;
                    break;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void removed() {
        client.keyboard.setRepeatEvents(false);
    }
}
