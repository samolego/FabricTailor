package org.samo_lego.fabrictailor.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.samo_lego.fabrictailor.client.screen.tabs.LocalSkinTab;
import org.samo_lego.fabrictailor.client.screen.tabs.PlayerSkinTab;
import org.samo_lego.fabrictailor.client.screen.tabs.SkinTabType;
import org.samo_lego.fabrictailor.client.screen.tabs.UrlSkinTab;
import org.samo_lego.fabrictailor.util.TranslatedText;

import java.io.File;

import static org.samo_lego.fabrictailor.client.ClientTailor.ALLOW_DEFAULT_SKIN;
import static org.samo_lego.fabrictailor.mixin.accessors.client.AdvancementsScreenAccessor.getTABS_TEXTURE;

public class ModMenuSettings extends Screen implements ModMenuApi {

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

    private TextFieldWidget skinInput;
    private int startX;
    private int startY;
    protected SkinTabType selectedTab;
    protected CheckboxWidget skinModelCheckbox;
    private ButtonWidget openExplorerButton;

    protected ModMenuSettings(Text title) {
        super(title);
    }

    @Override
    public ConfigScreenFactory<Screen> getModConfigScreenFactory() {
        return parent -> new ModMenuSettings(new LiteralText("FabricTailor"));
    }
    @Override
    protected void init() {
        super.init();
        int verticalSpacing = 8;

        // Button for opening file manager
        this.openExplorerButton = new ButtonWidget(
                width / 2,
                height / 2 + 10,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                new TranslatedText("button.fabrictailor.open_explorer"),
                (buttonWidget) -> {
                    Util.getOperatingSystem().open(new File(""));
                },
                (buttonWidget, matrixStack, i, j) -> {
                    this.renderTooltip(matrixStack, new TranslatedText("hint.fabrictailor.dragAndDrop"), width / 2 - 100, height / 2 + 10);
                }
        );
        this.addDrawableChild(openExplorerButton);

        // Checkbox for slim skin model
        this.skinModelCheckbox = new CheckboxWidget(
                width / 2,
                height / 2 - 12,
                150,
                20,
                new TranslatedText("button.fabrictailor.use_slim"),
                false
        );
        this.addDrawableChild(skinModelCheckbox);

        // Both should be hidden at first (default tab is "player")
        this.openExplorerButton.visible = false;
        this.skinModelCheckbox.visible = false;

        // Text field input
        skinInput = new TextFieldWidget(this.textRenderer, width / 2, height / 2 - 29, BUTTON_WIDTH, 14, new TranslatableText("itemGroup.search").formatted(Formatting.WHITE));
        skinInput.setMaxLength(256);
        skinInput.setVisible(true);
        skinInput.setDrawsBackground(true);
        skinInput.setEditableColor(16777215);

        this.addSelectableChild(skinInput);

        int buttonY = height - BUTTON_HEIGHT - verticalSpacing;
        if (ALLOW_DEFAULT_SKIN || this.client == null || this.client.isInSingleplayer()) {
            // Default skin button
            this.addDrawableChild(
                    new ButtonWidget(
                            width / 2 - BUTTON_WIDTH / 2 - 1, buttonY,
                            BUTTON_WIDTH,
                            BUTTON_HEIGHT,
                            new TranslatedText("button.fabrictailor.set_default_skin"),
                            onClick -> this.onClose()

                    )
            );
        }
    }

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
        RenderSystem.setShaderTexture(0, getTABS_TEXTURE());

        // Tabs
        for(int i = 0; i < TABS.length; ++i) {
            SkinTabType tab = TABS[i];

            if(selectedTab == null) {
                this.selectedTab = tab;
            }
            else if(selectedTab == tab) {
                // Rendering "selected" tab
                this.drawTexture(matrixStack, startX + 224 - i * 27, startY - 28, i == 0 ? 56 : 28, 32, 28, 32);

                // Showing or hiding additional buttons
                this.skinModelCheckbox.visible = tab.hasSkinModels();
                // Making sure we are in singleplayer to show open explorer button
                this.openExplorerButton.visible = tab.showExplorerButton();
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
            SkinTabType tab = TABS[i];
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
            SkinTabType tab = TABS[i];
            if(tab.isSelected(startX + 225 - i * 27, startY - 28, mouseX, mouseY)) {
                this.renderTooltip(matrixStack, tab.getTitle(), mouseX, mouseY);
            }
        }
    }
}