package org.samo_lego.fabrictailor.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.samo_lego.fabrictailor.client.SkinTab.TailorStyle;

import java.io.File;
import java.nio.file.Path;
import java.util.List;


@Environment(EnvType.CLIENT)
public class SkinChangeScreen extends Screen {

    public static final int BUTTON_HEIGHT = 20;
    public static final int BUTTON_WIDTH = 100;
    private static final Identifier TABS_TEXTURE = new Identifier("textures/gui/advancements/tabs.png");
    private static final Identifier BACKGROUND_TEXTURE = new Identifier("textures/gui/advancements/window.png");

    /**
     * Skin tabs:
     * local, url, player
     */
    private static final SkinTab[] TABS = new SkinTab[]{
            new SkinTab(TailorStyle.PLAYER, 27, 28),
            new SkinTab(TailorStyle.URL, 27, 28),
            new SkinTab(TailorStyle.LOCAL, 27, 28)
    };

    private TextFieldWidget skinInput;
    private int startX;
    private int startY;
    private SkinTab selectedTab;
    private CheckboxWidget skinModelCheckbox;
    private ButtonWidget openExplorerButton;

    protected SkinChangeScreen() {
        super(new TranslatableText("gui.fabrictailor.change_skin"));
    }

    /**
     * Initializes skin changing screen.
     */
    @Override
    protected void init() {
        super.init();
        this.client.keyboard.setRepeatEvents(true);
        int verticalSpacing = 8;

        // Button for opening file manager
        this.openExplorerButton = new ButtonWidget(
                width / 2,
                height / 2 + 10,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                new TranslatableText("button.fabrictailor.open_explorer"),
                (buttonWidget) -> {
                    Util.getOperatingSystem().open(new File(""));
                },
                (buttonWidget, matrixStack, i, j) -> {
                    this.renderTooltip(matrixStack, new TranslatableText("hint.fabrictailor.dragAndDrop"), width / 2 - 100, height / 2 + 10);
                }
        );
        this.addDrawable(openExplorerButton);

        // Checkbox for slim skin model
        this.skinModelCheckbox = new CheckboxWidget(
                width / 2,
                height / 2 - 12,
                150,
                20,
                new TranslatableText("button.fabrictailor.use_slim"),
                false
        );
        this.addDrawable(skinModelCheckbox);

        // Both should be hidden at first (default tab is "player")
        this.openExplorerButton.visible = false;
        this.skinModelCheckbox.visible = false;

        // Text field input
        skinInput = new TextFieldWidget(this.textRenderer, width / 2, height / 2 - 29, BUTTON_WIDTH, 14, new TranslatableText("itemGroup.search"));
        skinInput.setMaxLength(256);
        skinInput.setVisible(true);
        skinInput.setDrawsBackground(true);
        skinInput.setEditableColor(16777215);

        addSelectableChild(skinInput);

        // "Set skin" button
        this.addDrawable(
                new ButtonWidget(
                        width / 2,
                        height / 2 + 30,
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT,
                        new TranslatableText("button.fabrictailor.set_skin"),
                        onClick -> {
                            String skin = skinInput.getText();
                            String skinCommand = this.selectedTab.getSkinCommand();
                            if(this.selectedTab.hasModels())
                                skinCommand += this.skinModelCheckbox.isChecked() ? "slim" : "classic";

                            // Works, but ugly
                            // :concern:
                            client.player.networkHandler.sendPacket(new ChatMessageC2SPacket(skinCommand+ " " + skin));
                            this.onClose();
                        }
                )
        );

        this.addDrawable(
                new ButtonWidget(
                        width / 2 - BUTTON_WIDTH - 2, height - BUTTON_HEIGHT - verticalSpacing,
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT,
                        new TranslatableText("button.fabrictailor.clear_skin"),
                        onClick -> {
                            // Same as above; works, but ugly
                            client.player.networkHandler.sendPacket(new ChatMessageC2SPacket("/skin clear"));
                            this.onClose();
                        }
                )
        );

        // "Cancel" button which closes the screen
        this.addDrawable(
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
    }

    /**
     * Renders the skin changing screen.
     *
     * @param matrixStack
     * @param mouseX
     * @param mouseY
     * @param delta
     */
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        // Darkens background
        this.renderBackground(matrixStack, 0);
        super.render(matrixStack, mouseX, mouseY, delta);

        // Screen title
        drawCenteredText(matrixStack, textRenderer, title, width / 2, 15, 0xffffff);

        // Starting position of the window texture
        this.startX = (this.width - 252) / 2;
        this.startY = (this.height - 140) / 2;

        // Window texture
        this.client.getTextureManager().bindTexture(BACKGROUND_TEXTURE);
        this.drawTexture(matrixStack, startX, startY, 0, 0, 252, 140);

        // Drawing Player
        // Luckily vanilla code is available
        InventoryScreen.drawEntity(startX + 50, startY + 120, 50, (float)width / 2 - 75 - mouseX, (float)height / 2 - mouseY, client.player);

        // Render input field
        skinInput.render(matrixStack, startX, startY, delta);

        // Other renders
        this.drawTabs(matrixStack, startX, startY, mouseX, mouseY);
        this.drawIcons(startX, startY);
        this.drawWidgetTooltip(matrixStack, startX, startY, mouseX, mouseY);
    }

    /**
     * Draws tabs.
     *
     * @param matrixStack
     * @param startX x where skin window starts
     * @param startY y where skin window starts
     * @param mouseX mouse x
     * @param mouseY mouse y
     */
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
                // Rendering "selected" tab
                this.drawTexture(matrixStack, startX + 224 - i * 27, startY - 28, i == 0 ? 56 : 28, 32, 28, 32);

                // Showing or hiding additional buttons
                this.skinModelCheckbox.visible = i != 0;
                // Making sure we are in singleplayer to show open explorer button
                this.openExplorerButton.visible = i == 2 && client.getServer() != null;
            }
            else {
                // rendering other tabs
                this.drawTexture(matrixStack, startX + 224 - i * 27, startY - 28, i == 0 ? 56 : 28, 0, 28, i == 0 ? 31 : 29);
            }
        }
        // Rendering title
        this.textRenderer.drawWithShadow(matrixStack, this.selectedTab.getTitle(), startX + 10, startY + 5, 16777215);

        // Rendering description above input field
        this.textRenderer.drawWithShadow(matrixStack, this.selectedTab.getDescription(), (float) width / 2, (float) height / 2 - 40, 16777215);

        RenderSystem.defaultBlendFunc();
    }


    /**
     * Draws ItemStack icons on the tabs.
     *
     * @param startX x where skin window starts
     * @param startY y where skin window starts
     */
    private void drawIcons(int startX, int startY) {
        // Icons
        for(int i = 0; i < TABS.length; ++i) {
            SkinTab tab = TABS[i];
            itemRenderer.renderInGui(tab.getIcon(), startX + 231 - i * 27, startY - 18);
        }
        RenderSystem.disableBlend();
    }


    /**
     * Draws tooltips when hovering over tabs.
     *
     * @param matrixStack
     * @param startX x where skin window starts
     * @param startY y where skin window starts
     * @param mouseX mouse x
     * @param mouseY mouse y
     */
    private void drawWidgetTooltip(MatrixStack matrixStack, int startX, int startY, int mouseX, int mouseY) {
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

    /**
     * Checks if one of the tabs was clicked
     * and selects it accordingly.
     *
     * @param mouseX mouse x
     * @param mouseY mouse y
     * @param button button that was clicke
     * @return super.mouseClicked()
     */
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

    /**
     * Used for skin drag and drop.
     *
     * @param paths paths of the files; only first is used
     */
    @Override
    public void filesDragged(List<Path> paths) {
        if(this.selectedTab.getType().equals(TailorStyle.LOCAL)) {
            this.skinInput.setText(paths.iterator().next().toString());
        }
    }
}
