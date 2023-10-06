package org.samo_lego.fabrictailor.client.screen;

import com.mojang.authlib.properties.PropertyMap;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.joml.Quaternionf;
import org.samo_lego.fabrictailor.client.screen.tabs.*;
import org.samo_lego.fabrictailor.mixin.client.AAbstractClientPlayer;
import org.samo_lego.fabrictailor.util.TextTranslations;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.samo_lego.fabrictailor.client.ClientTailor.ALLOW_DEFAULT_SKIN;
import static org.samo_lego.fabrictailor.client.ClientTailor.TAILORED_SERVER;

@Environment(EnvType.CLIENT)
public class SkinChangeScreen extends Screen {

    public static final int BUTTON_HEIGHT = 20;
    public static final int BUTTON_WIDTH = 100;

    /**
     * Skin tabs:
     * cape, local, url, player
     */
    private static final List<SkinTabType> TABS = Arrays.asList(
            new PlayerSkinTab(),
            new UrlSkinTab(),
            new LocalSkinTab(),
            new CapeTab()
    );

    private EditBox skinInput;
    private int startX;
    private int startY;
    protected SkinTabType selectedTab;
    protected Checkbox skinModelCheckbox;
    private Button openExplorerButton;

    public SkinChangeScreen() {
        super(TextTranslations.create("options.skinCustomisation.title"));
    }

    /**
     * Initializes skin changing screen.
     */
    @Override
    protected void init() {
        super.init();
        int verticalSpacing = 8;

        // Button for opening file manager
        this.openExplorerButton = Button.builder(TextTranslations.create("button.fabrictailor.open_explorer"),
                        (buttonWidget) -> Util.getPlatform().openFile(new File("")))
                .tooltip(Tooltip.create(TextTranslations.create("hint.fabrictailor.dragAndDrop")))
                .pos(this.width / 2, this.height / 2 + 10)
                .tooltip(Tooltip.create(TextTranslations.create("hint.fabrictailor.dragAndDrop")))
                .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        this.addRenderableWidget(openExplorerButton);

        // Checkbox for slim skin model
        this.skinModelCheckbox = new Checkbox(
                width / 2,
                height / 2 - 12,
                150,
                20,
                TextTranslations.create("button.fabrictailor.use_slim"),
                false
        );
        this.addRenderableWidget(skinModelCheckbox);

        // Both should be hidden at first (default tab is "player")
        this.openExplorerButton.visible = false;
        this.skinModelCheckbox.visible = false;

        // Text field input
        skinInput = new EditBox(this.font, width / 2, height / 2 - 29, BUTTON_WIDTH, 14, Component.translatable("itemGroup.search").withStyle(ChatFormatting.WHITE));
        skinInput.setMaxLength(256);
        skinInput.setVisible(true);
        skinInput.setBordered(true);
        skinInput.setTextColor(16777215);

        this.addWidget(skinInput);

        // "Set skin" button
        this.addRenderableWidget(
                Button.builder(TextTranslations.create("button.fabrictailor.apply"),
                                onClick -> {
                                    this.applyNewSkin();
                                    this.onClose();
                                }).pos(width / 2, height / 2 + 30)
                        .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                        .build()
        );

        int buttonY = height - BUTTON_HEIGHT - verticalSpacing;
        boolean allowDefaultSkinButton = ALLOW_DEFAULT_SKIN || this.minecraft.hasSingleplayerServer();

        this.addRenderableWidget(
                Button.builder(TextTranslations.create("button.fabrictailor.clear_skin"),
                                onClick -> {
                                    this.clearSkin();
                                    this.onClose();
                                }).pos(width / 2 - BUTTON_WIDTH - (allowDefaultSkinButton ? BUTTON_WIDTH / 2 : 0) - 2, buttonY)
                        .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(CommonComponents.GUI_CANCEL,
                                onClick -> this.onClose())
                        .pos(width / 2 + (allowDefaultSkinButton ? BUTTON_WIDTH / 2 : 0) + 2, buttonY)
                        .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                        .build()
        );

        if (allowDefaultSkinButton) {
            // Default skin button
            this.addRenderableWidget(
                    Button.builder(TextTranslations.create("button.fabrictailor.set_default_skin"),
                                    onClick -> {
                                        minecraft.player.connection.sendUnsignedCommand("skin default");
                                        this.onClose();
                                    })
                            .pos(width / 2 - BUTTON_WIDTH / 2 - 1, buttonY)
                            .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                            .build());
        }
    }

    private void clearSkin() {
        if (TAILORED_SERVER) {
            this.minecraft.player.connection.sendUnsignedCommand("skin clear");
        } else {
            ((AAbstractClientPlayer) this.minecraft.player).ft_getPlayerInfo().getProfile().getProperties().removeAll(SkinManager.PROPERTY_TEXTURES);
            // Reload skin - todo
        }
    }

    private void applyNewSkin() {
        new CompletableFuture<>().completeAsync(() -> {
            final var packetInfo = this.selectedTab.getSkinChangePacket(minecraft.player, skinInput.getValue(), this.skinModelCheckbox.selected());
            packetInfo.ifPresent(packet -> {
                if (TAILORED_SERVER) {
                    ClientPlayNetworking.send(packet.getFirst(), packet.getSecond());
                } else {
                    // Change skin clientside only todo: reload skin
                    PropertyMap map = ((AAbstractClientPlayer) this.minecraft.player).ft_getPlayerInfo().getProfile().getProperties();

                    /*try {
                        map.removeAll(SkinManager.PROPERTY_TEXTURES);
                    } catch (Exception ignored) {
                        // Player has no skin data, no worries
                    }

                    var skinData = packet.getSecond().readProperty();
                    map.put(SkinManager.PROPERTY_TEXTURES, skinData);
                    var skiloc = ((AAbstractClientPlayer) Minecraft.getInstance().player).ft_getPlayerInfo().getSkinLocation();

                    // Reload skin
                    //HttpTexture.
                    Minecraft.getInstance().getSkinManager().registerSkins(((AAbstractClientPlayer) this.minecraft.player).ft_getPlayerInfo().getProfile(), (type, resourceLocation, minecraftProfileTexture) -> {
                    }, true);
                    //this.minecraft.getEntityRenderDispatcher().onResourceManagerReload(ResourceManager.Empty.INSTANCE);

                    //var skinInfo = this.minecraft.getSkinManager().getInsecureSkinInformation(this.minecraft.player.getGameProfile());
                    //MinecraftProfileTexture skinTexture = skinInfo.get(MinecraftProfileTexture.Type.SKIN);
                    //ResourceLocation resourceLocation = this.minecraft.getSkinManager().registerTexture(skinTexture, MinecraftProfileTexture.Type.SKIN);
                    // todo*/
                }
            });
            return null;
        });
    }

    /**
     * Renders the skin changing screen.
     */
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        // Darkens background
        this.renderBackground(guiGraphics, 0, 0, 0.5f);
        super.render(guiGraphics, mouseX, mouseY, delta);

        // Screen title
        guiGraphics.drawCenteredString(font, title, width / 2, 15, 0xffffff);

        // Starting position of the window texture
        this.startX = (this.width - 252) / 2;
        this.startY = (this.height - 140) / 2;

        // Window texture
        RenderSystem.enableBlend();
        guiGraphics.blit(AdvancementsScreen.WINDOW_LOCATION, startX, startY, 0, 0, 252, 140);


        // Render input field
        skinInput.render(guiGraphics, startX, startY, delta);

        // Other renders
        this.drawTabs(guiGraphics, startX, startY);
        this.drawIcons(guiGraphics, startX, startY);
        this.drawWidgetTooltips(guiGraphics, startX, startY, mouseX, mouseY);


        if (this.selectedTab.showModelBackwards()) {
            int x = startX + 64;
            int y = startY + 120;
            int size = 50;
            float mousex = -(((float) width / 2) - 75 - mouseX);
            float mousey = ((float) height / 2) - mouseY;
            var entity = minecraft.player;
            float f = (float) Math.atan(mousex / 40.0f);
            float g = (float) Math.atan(mousey / 40.0f);
            PoseStack poseStack = RenderSystem.getModelViewStack();
            poseStack.pushPose();
            poseStack.translate(x, y, 1050.0);
            poseStack.scale(1.0f, 1.0f, -1.0f);
            RenderSystem.applyModelViewMatrix();
            PoseStack poseStack2 = new PoseStack();
            poseStack2.translate(0.0, 0.0, 1000.0);
            poseStack2.scale(size, size, size);
            Quaternionf quaternion = Axis.ZP.rotationDegrees(180.0f);
            Quaternionf quaternion2 = Axis.XP.rotationDegrees(g * 20.0f);
            quaternion.mul(quaternion2);
            poseStack2.mulPose(quaternion);
            float h = entity.yBodyRot;
            float i = entity.getYRot();
            float j = entity.getXRot();
            float k = entity.yHeadRotO;
            float l = entity.yHeadRot;
            entity.yBodyRot = f * 20.0f;
            entity.setYRot(f * 40.0f);
            entity.setXRot(-g * 20.0f);
            entity.yHeadRot = entity.getYRot();
            entity.yHeadRotO = entity.getYRot();
            Lighting.setupForEntityInInventory();
            EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            quaternion2.conjugate();
            entityRenderDispatcher.overrideCameraOrientation(quaternion2);
            entityRenderDispatcher.setRenderShadow(false);
            var bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
            RenderSystem.runAsFancy(() -> entityRenderDispatcher.render(entity, 0.0, 0.0, 0.0, 0.0f, 1.0f, poseStack2, bufferSource, 0xF000F0));
            bufferSource.endBatch();
            entityRenderDispatcher.setRenderShadow(true);
            entity.yBodyRot = h;
            entity.setYRot(i);
            entity.setXRot(j);
            entity.yHeadRotO = k;
            entity.yHeadRot = l;
            poseStack.popPose();
            RenderSystem.applyModelViewMatrix();
            Lighting.setupFor3DItems();

        } else {
            // Drawing Player
            // Luckily vanilla code is available
            int x = this.startX + 24;
            int y = this.startY - 76;
            InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, x, y, x + 75, y + 208, 48, 1.0f, mouseX + 2, mouseY - 16, this.minecraft.player);
        }
    }

    /**
     * Draws tabs.
     *
     * @param guiGraphics
     * @param startX      x where skin window starts
     * @param startY      y where skin window starts
     */
    private void drawTabs(GuiGraphics guiGraphics, int startX, int startY) {
        if (this.selectedTab == null) {
            this.selectedTab = TABS.get(0);
        }

        // Tabs
        for (int i = 0; i < TABS.size(); ++i) {
            SkinTabType tab = TABS.get(i);

            final var selected = this.selectedTab == tab;
            if (selected) {
                // Rendering "selected" tab

                // Showing or hiding additional buttons
                this.skinModelCheckbox.visible = tab.hasSkinModels();
                this.openExplorerButton.visible = tab.showExplorerButton();
            }

            tab.getTabType().draw(guiGraphics, startX, startY, selected, tab.getTabType().getMax() - i - 1);
        }

        // Rendering title
        guiGraphics.drawString(this.font, this.selectedTab.getTitle(), startX + 10, startY + 5, 0xFFFFFF);

        // Rendering description above input field
        guiGraphics.drawString(this.font, this.selectedTab.getDescription(), width / 2, height / 2 - 40, 0xFFFFFF);
    }


    /**
     * Draws ItemStack icons on the tabs.
     *
     * @param startX x where skin window starts
     * @param startY y where skin window starts
     */
    private void drawIcons(GuiGraphics guiGraphics, int startX, int startY) {
        // Icons
        for (int i = 0; i < TABS.size(); ++i) {
            SkinTabType tab = TABS.get(i);
            tab.getTabType().drawIcon(guiGraphics, startX, startY, tab.getTabType().getMax() - i - 1, tab.getIcon());
        }
    }


    /**
     * Draws tooltips when hovering over tabs.
     *
     * @param guiGraphics
     * @param startX      x where skin window starts
     * @param startY      y where skin window starts
     * @param mouseX      mouse x
     * @param mouseY      mouse y
     */
    private void drawWidgetTooltips(GuiGraphics guiGraphics, int startX, int startY, int mouseX, int mouseY) {
        for (int i = 0; i < TABS.size(); ++i) {
            SkinTabType tab = TABS.get(i);

            if (tab.getTabType().isMouseOver(startX, startY, tab.getTabType().getMax() - i - 1, mouseX, mouseY)) {
                guiGraphics.renderTooltip(this.font, tab.getTitle(), mouseX, mouseY);
                break;
            }
        }
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
            for (int i = 0; i < TABS.size(); ++i) {
                SkinTabType tab = TABS.get(i);

                final boolean mouseOver = tab.getTabType().isMouseOver(startX, startY, tab.getTabType().getMax() - i - 1, mouseX, mouseY);

                if (mouseOver) {
                    this.selectedTab = tab;
                    break;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }


    /**
     * Used for skin drag and drop.
     *
     * @param paths paths of the files; only first is used
     */
    @Override
    public void onFilesDrop(List<Path> paths) {
        if (!(this.selectedTab instanceof PlayerSkinTab)) {
            this.skinInput.setValue(paths.iterator().next().toString());
        }
    }
}
