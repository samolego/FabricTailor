package org.samo_lego.fabrictailor.client;

import carpet.script.language.Sys;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.block.Blocks;
import net.minecraft.block.SkullBlock;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


@Environment(EnvType.CLIENT)
public class SkinChangeScreen extends Screen {

    public static final int BUTTONHEIGHT = 20;
    private static final Identifier TABS_TEXTURE = new Identifier("textures/gui/advancements/tabs.png");
    private static final Identifier SELECTED_TABS_TEXTURE = new Identifier("textures/gui/advancements/tabs.png");
    private static final Identifier BACKGROUND_TEXTURE = new Identifier("textures/gui/advancements/window.png");
    private static final SkinTab[] TABS = new SkinTab[]{
            new SkinTab("tab.fabrictailor.title_player", new ItemStack(Items.PLAYER_HEAD), 27, 28),
            new SkinTab("tab.fabrictailor.title_url", new ItemStack(Items.GLOBE_BANNER_PATTERN), 27, 28),
            new SkinTab("tab.fabrictailor.title_local", new ItemStack(Items.JIGSAW), 27, 28)
    };

    private TextFieldWidget skinInput;
    private int startX;
    private int startY;
    private MatrixStack matrixStack;
    private SkinTab selectedTab;

    protected SkinChangeScreen() {
        super(new TranslatableText("gui.fabrictailor.change_skin"));
    }

    @Override
    protected void init() {
        super.init();
        this.client.keyboard.setRepeatEvents(true);
        int buttonWidth = 100;
        int horizontalSpacing = 4;
        int verticalSpacing = 8;

        skinInput = new TextFieldWidget(this.textRenderer, width / 2, height / 3, width / 4, 14, new TranslatableText("itemGroup.search"));
        skinInput.setMaxLength(256);
        skinInput.setVisible(true);
        skinInput.setHasBorder(true);
        skinInput.setEditableColor(16777215);

        addChild(
                skinInput
        );

        addButton(
                new ButtonWidget(
                        width / 2 + horizontalSpacing / 2, height - BUTTONHEIGHT - verticalSpacing,
                        buttonWidth,
                        BUTTONHEIGHT,
                        new TranslatableText("button.fabrictailor.set_skin"),
                        onClick -> {
                            this.onClose();
                        }
                )
        );

        addButton(
                new ButtonWidget(
                        width / 2 - buttonWidth - horizontalSpacing / 2, height - BUTTONHEIGHT - verticalSpacing,
                        buttonWidth,
                        BUTTONHEIGHT,
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
        this.matrixStack = matrixStack;

        super.render(matrixStack, mouseX, mouseY, delta);
        drawCenteredText(matrixStack, textRenderer, title, width / 2, 15, 0xffffff);

        this.startX = (this.width - 252) / 2;
        this.startY = (this.height - 140) / 2;

        // Background
        this.client.getTextureManager().bindTexture(BACKGROUND_TEXTURE);
        this.drawTexture(matrixStack, startX, startY, 0, 0, 252, 140);

        // Render skin input
        skinInput.render(matrixStack, startX, startY, delta);

        this.drawTabs(matrixStack, startX, startY, mouseX, mouseY);
        this.drawSkullIcons(matrixStack, startX, startY, mouseX, mouseY);
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
        RenderSystem.defaultBlendFunc();
    }


    private void drawSkullIcons(MatrixStack matrixStack, int startX, int startY, int mouseX, int mouseY) {
        // Skull icons
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
                    System.out.println("Selecting: " + tab.getTitle().getKey());
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
