package org.samo_lego.fabrictailor.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import org.samo_lego.fabrictailor.util.TranslatedText;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.samo_lego.fabrictailor.mixin.accessors.client.AdvancementsScreenAccessor.getTABS_LOCATION;
import static org.samo_lego.fabrictailor.mixin.accessors.client.AdvancementsScreenAccessor.getWINDOW_LOCATION;

@Environment(EnvType.CLIENT)
public class SkinChangeScreen extends Screen {

    public static final int BUTTON_HEIGHT = 20;
    public static final int BUTTON_WIDTH = 100;

    /**
     * Skin tabs:
     * local, url, player
     */
    private static final SkinTabType[] TABS = new SkinTabType[] {
            new PlayerSkinTab(),
            new UrlSkinTab(),
            new LocalSkinTab()
    };

    private EditBox skinInput;
    private int startX;
    private int startY;
    protected SkinTabType selectedTab;
    protected Checkbox skinModelCheckbox;
    private Button openExplorerButton;

    public SkinChangeScreen() {
        super(new TranslatedText("gui.fabrictailor.change_skin"));
    }

    /**
     * Initializes skin changing screen.
     */
    @Override
    protected void init() {
        super.init();
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        int verticalSpacing = 8;

        // Button for opening file manager
        this.openExplorerButton = new Button(
                width / 2,
                height / 2 + 10,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                new TranslatedText("button.fabrictailor.open_explorer"),
                (buttonWidget) -> {
                    Util.getPlatform().openFile(new File(""));
                },
                (buttonWidget, matrixStack, i, j) -> {
                    this.renderTooltip(matrixStack, new TranslatedText("hint.fabrictailor.dragAndDrop"), width / 2 - 100, height / 2 + 10);
                }
        );
        this.addRenderableWidget(openExplorerButton);

        // Checkbox for slim skin model
        this.skinModelCheckbox = new Checkbox(
                width / 2,
                height / 2 - 12,
                150,
                20,
                new TranslatedText("button.fabrictailor.use_slim"),
                false
        );
        this.addRenderableWidget(skinModelCheckbox);

        // Both should be hidden at first (default tab is "player")
        this.openExplorerButton.visible = false;
        this.skinModelCheckbox.visible = false;

        // Text field input
        skinInput = new EditBox(this.font, width / 2, height / 2 - 29, BUTTON_WIDTH, 14, new TranslatableComponent("itemGroup.search").withStyle(ChatFormatting.WHITE));
        skinInput.setMaxLength(256);
        skinInput.setVisible(true);
        skinInput.setBordered(true);
        skinInput.setTextColor(16777215);

        this.addWidget(skinInput);

        // "Set skin" button
        this.addRenderableWidget(
                new Button(
                        width / 2,
                        height / 2 + 30,
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT,
                        new TranslatedText("button.fabrictailor.set_skin"),
                        onClick -> {
                            new CompletableFuture<>().completeAsync(() -> {
                                ServerboundCustomPayloadPacket skinChangePacket = this.selectedTab.getSkinChangePacket(skinInput.getValue(), this.skinModelCheckbox.selected());
                                if (skinChangePacket != null)
                                    minecraft.player.connection.send(skinChangePacket);
                                return null;
                            });
                            this.onClose();
                        }
                )
        );

        int buttonY = height - BUTTON_HEIGHT - verticalSpacing;
        int clearX = ALLOW_DEFAULT_SKIN || this.client.isInSingleplayer() ? width / 2 - 3 * BUTTON_WIDTH / 2 - 2 : width / 2 - BUTTON_WIDTH - 2;
        this.addRenderableWidget(
                new Button(
                        width / 2 - BUTTON_WIDTH - 2, height - BUTTON_HEIGHT - verticalSpacing,
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT,
                        new TranslatedText("button.fabrictailor.clear_skin"),
                        onClick -> {
                            minecraft.player.connection.send(new ServerboundChatPacket("/skin clear"));
                            this.onClose();
                        }
                )
        );

        // "Cancel" button which closes the screen
        int cancelX = ALLOW_DEFAULT_SKIN || this.client.isInSingleplayer() ? width / 2 + BUTTON_WIDTH / 2 + 2 : width / 2 + 2;
        this.addRenderableWidget(
                new Button(
                        width / 2 + 2, height - BUTTON_HEIGHT - verticalSpacing,
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT,
                        CommonComponents.GUI_CANCEL,
                        onClick -> {
                            this.onClose();
                        }
                )
        );

        if (ALLOW_DEFAULT_SKIN || this.client.isInSingleplayer()) {
            // Default skin button
            this.addDrawableChild(
                    new ButtonWidget(
                            width / 2 - BUTTON_WIDTH / 2 - 1, buttonY,
                            BUTTON_WIDTH,
                            BUTTON_HEIGHT,
                            new TranslatedText("button.fabrictailor.set_default_skin"),
                            onClick -> {
                                client.player.networkHandler.sendPacket(new ChatMessageC2SPacket("/fabrictailor setDefaultSkin"));
                                this.onClose();
                            }

                    )
            );
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.skinInput != null) {
            this.skinInput.tick();
        }

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
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float delta) {
        // Darkens background
        this.renderBackground(matrixStack, 0);
        super.render(matrixStack, mouseX, mouseY, delta);

        // Screen title
        drawCenteredString(matrixStack, font, title, width / 2, 15, 0xffffff);

        // Starting position of the window texture
        this.startX = (this.width - 252) / 2;
        this.startY = (this.height - 140) / 2;

        // Window texture
        RenderSystem.setShaderTexture(0, getWINDOW_LOCATION());
        this.blit(matrixStack, startX, startY, 0, 0, 252, 140);

        // Drawing Player
        // Luckily vanilla code is available
        InventoryScreen.renderEntityInInventory(startX + 50, startY + 120, 50, (float)width / 2 - 75 - mouseX, (float)height / 2 - mouseY, minecraft.player);

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
    private void drawTabs(PoseStack matrixStack, int startX, int startY, int mouseX, int mouseY) {
        // Setting texture
        RenderSystem.setShaderTexture(0, getTABS_LOCATION());

        // Tabs
        for(int i = 0; i < TABS.length; ++i) {
            SkinTabType tab = TABS[i];

            if(selectedTab == null) {
                this.selectedTab = tab;
            }
            else if(selectedTab == tab) {
                // Rendering "selected" tab
                this.blit(matrixStack, startX + 224 - i * 27, startY - 28, i == 0 ? 56 : 28, 32, 28, 32);

                // Showing or hiding additional buttons
                this.skinModelCheckbox.visible = tab.hasSkinModels();
                // Making sure we are in singleplayer to show open explorer button
                this.openExplorerButton.visible = tab.showExplorerButton();
            }
            else {
                // rendering other tabs
                this.blit(matrixStack, startX + 224 - i * 27, startY - 28, i == 0 ? 56 : 28, 0, 28, i == 0 ? 31 : 29);
            }
        }
        // Rendering title
        this.font.drawShadow(matrixStack, this.selectedTab.getTitle(), startX + 10, startY + 5, 16777215);

        // Rendering description above input field
        this.font.drawShadow(matrixStack, this.selectedTab.getDescription(), (float) width / 2, (float) height / 2 - 40, 16777215);

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
            SkinTabType tab = TABS[i];
            itemRenderer.renderAndDecorateFakeItem(tab.getIcon(), startX + 231 - i * 27, startY - 18);
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
    private void drawWidgetTooltip(PoseStack matrixStack, int startX, int startY, int mouseX, int mouseY) {
        for(int i = 0; i < TABS.length; ++i) {
            SkinTabType tab = TABS[i];
            if(tab.isSelected(startX + 225 - i * 27, startY - 28, mouseX, mouseY)) {
                this.renderTooltip(matrixStack, tab.getTitle(), mouseX, mouseY);
            }
        }
    }

    @Override
    public void renderBackground(PoseStack matrices) {
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
                SkinTabType tab = TABS[i];
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
        minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    /**
     * Used for skin drag and drop.
     *
     * @param paths paths of the files; only first is used
     */
    @Override
    public void onFilesDrop(List<Path> paths) {
        if(this.selectedTab instanceof LocalSkinTab) {
            this.skinInput.setValue(paths.iterator().next().toString());
        }
    }
}
