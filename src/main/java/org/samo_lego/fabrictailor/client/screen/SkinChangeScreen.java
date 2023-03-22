package org.samo_lego.fabrictailor.client.screen;

import com.mojang.authlib.minecraft.InsecureTextureException;
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
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.joml.Quaternionf;
import org.samo_lego.fabrictailor.client.screen.tabs.*;
import org.samo_lego.fabrictailor.util.TextTranslations;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.samo_lego.fabrictailor.client.ClientTailor.ALLOW_DEFAULT_SKIN;
import static org.samo_lego.fabrictailor.client.ClientTailor.TAILORED_SERVER;
import static org.samo_lego.fabrictailor.mixin.accessors.client.AAdvancementsScreen.getTABS_LOCATION;
import static org.samo_lego.fabrictailor.mixin.accessors.client.AAdvancementsScreen.getWINDOW_LOCATION;

@Environment(EnvType.CLIENT)
public class SkinChangeScreen extends Screen {

    public static final int BUTTON_HEIGHT = 20;
    public static final int BUTTON_WIDTH = 100;

    /**
     * Skin tabs:
     * local, url, player
     */
    private static final List<SkinTabType> TABS = new ArrayList<>(Arrays.asList(
            new PlayerSkinTab(),
            new UrlSkinTab(),
            new LocalSkinTab(),
            new CapeTab()
    ));

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
                        (buttonWidget) -> {
                            Util.getPlatform().openFile(new File(""));
                        })
                .pos(this.width / 2, this.height / 2 + 10)
                .tooltip(Tooltip.create(TextTranslations.create("hint.fabrictailor.dragAndDrop")))
                .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

                /*(buttonWidget, matrixStack, i, j) -> {
                    this.renderTooltip(matrixStack, TextTranslations.create("hint.fabrictailor.dragAndDrop"), width / 2 - 100, height / 2 + 10);
                }*/
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
            this.minecraft.player.getGameProfile().getProperties().removeAll("textures");
        }
    }

    private void applyNewSkin() {
        new CompletableFuture<>().completeAsync(() -> {
            final var packetInfo = this.selectedTab.getSkinChangePacket(minecraft.player, skinInput.getValue(), this.skinModelCheckbox.selected());
            packetInfo.ifPresent(packet -> {
                if (TAILORED_SERVER) {
                    ClientPlayNetworking.send(packet.getFirst(), packet.getSecond());
                } else {
                    // Change skin clientside only  todo test
                    PropertyMap map = this.minecraft.player.getGameProfile().getProperties();

                    try {
                        map.removeAll("textures");
                    } catch (Exception ignored) {
                        // Player has no skin data, no worries
                    }

                    try {
                        var skinData = packet.getSecond().readProperty();
                        map.put("textures", skinData);
                    } catch (InsecureTextureException ignored) {
                        // No skin data
                    }

                    // Reload skin
                    //HttpTexture.
                    Minecraft.getInstance().getSkinManager().registerSkins(this.minecraft.player.getGameProfile(), (type, resourceLocation, minecraftProfileTexture) -> {
                    }, true);
                    //this.minecraft.getEntityRenderDispatcher().onResourceManagerReload(ResourceManager.Empty.INSTANCE);

                    //var skinInfo = this.minecraft.getSkinManager().getInsecureSkinInformation(this.minecraft.player.getGameProfile());
                    //MinecraftProfileTexture skinTexture = skinInfo.get(MinecraftProfileTexture.Type.SKIN);
                    //ResourceLocation resourceLocation = this.minecraft.getSkinManager().registerTexture(skinTexture, MinecraftProfileTexture.Type.SKIN);
                    // todo
                }
            });
            return null;
        });
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
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, delta);

        // Screen title
        drawCenteredString(matrixStack, font, title, width / 2, 15, 0xffffff);

        // Starting position of the window texture
        this.startX = (this.width - 252) / 2;
        this.startY = (this.height - 140) / 2;

        // Window texture
        RenderSystem.setShaderTexture(0, getWINDOW_LOCATION());
        blit(matrixStack, startX, startY, 0, 0, 252, 140);


        // Render input field
        skinInput.render(matrixStack, startX, startY, delta);

        // Other renders
        this.drawTabs(matrixStack, startX, startY, mouseX, mouseY);
        this.drawIcons(matrixStack, startX, startY);
        this.drawWidgetTooltip(matrixStack, startX, startY, mouseX, mouseY);


        if (this.selectedTab.showModelBackwards()) {
            int x = startX + 50;
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

            float mousex = (float) width / 2 - 75 - mouseX;
            float mousey = ((float) height / 2 - mouseY);
            var player = minecraft.player;

            /*float f = (float)Math.atan(mousex / 40.0f);
            float g = (float)Math.atan(mousey / 40.0f);

            float h = player.yBodyRot;
            float i = player.getYRot();
            float j = player.getXRot();
            float k = player.yHeadRotO;
            float l = player.yHeadRot;
            player.yBodyRot = f * 20.0f;
            player.setYRot(f * 40.0f);
            player.setXRot(-g * 20.0f);
            player.yHeadRot = player.getYRot();
            player.yHeadRotO = player.getYRot();*/


            float l = (float)Math.atan(mousey / 40.0f);
            final var quaternionf = new Quaternionf().rotateZ((float)Math.PI);
            final var quaternionf2 = new Quaternionf().rotateX(l * 20.0f * ((float)Math.PI / 180));
            InventoryScreen.renderEntityInInventory(matrixStack, startX + 50, startY + 120, 50, quaternionf, quaternionf2, player);

            /*player.yBodyRot = h;
            player.setYRot(i);
            player.setXRot(j);
            player.yHeadRotO = k;
            player.yHeadRot = l;*/
        }
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
        for (int i = 0; i < TABS.size(); ++i) {
            SkinTabType tab = TABS.get(i);

            if (selectedTab == null) {
                this.selectedTab = tab;
            } else if (selectedTab == tab) {
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
    private void drawIcons(PoseStack poseStack, int startX, int startY) {
        // Icons
        for (int i = 0; i < TABS.size(); ++i) {
            SkinTabType tab = TABS.get(i);
            itemRenderer.renderAndDecorateFakeItem(poseStack, tab.getIcon(), startX + 231 - i * 27, startY - 18);
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
        for (int i = 0; i < TABS.size(); ++i) {
            SkinTabType tab = TABS.get(i);
            if (tab.isSelected(startX + 225 - i * 27, startY - 28, mouseX, mouseY)) {
                this.renderTooltip(matrixStack, tab.getTitle(), mouseX, mouseY);
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
                if (tab.isSelected(startX + 225 - i * 27, startY - 28, (int) mouseX, (int) mouseY)) {
                    this.selectedTab = tab;
                    break;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void removed() {
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
