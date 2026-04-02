package org.samo_lego.fabrictailor.client.screen;

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.Util;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;
import org.samo_lego.fabrictailor.client.screen.tabs.CapeTab;
import org.samo_lego.fabrictailor.client.screen.tabs.LocalSkinTab;
import org.samo_lego.fabrictailor.client.screen.tabs.PlayerSkinTab;
import org.samo_lego.fabrictailor.client.screen.tabs.SkinTabType;
import org.samo_lego.fabrictailor.client.screen.tabs.UrlSkinTab;
import org.samo_lego.fabrictailor.mixin.accessors.AAdvancementsScreen;
import org.samo_lego.fabrictailor.mixin.accessors.AInventoryScreen;
import org.samo_lego.fabrictailor.mixin.client.AAbstractClientPlayer;
import org.samo_lego.fabrictailor.network.payload.DefaultSkinPayload;
import org.samo_lego.fabrictailor.util.Logging;
import org.samo_lego.fabrictailor.util.TextTranslations;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
        this.skinModelCheckbox = Checkbox.builder(TextTranslations.create("button.fabrictailor.use_slim"), this.font)
                .pos(width / 2,
                        height / 2 - 12)
                .maxWidth(BUTTON_WIDTH)
                .selected(false)
                .build();
        this.addRenderableWidget(skinModelCheckbox);

        // Both should be hidden at first (default tab is "player")
        this.openExplorerButton.visible = false;
        this.skinModelCheckbox.visible = false;

        // Text field input
        skinInput = new EditBox(this.font, width / 2, height / 2 - 29, BUTTON_WIDTH, 14, Component.translatable("itemGroup.search").withStyle(ChatFormatting.WHITE));
        skinInput.setMaxLength(256);
        skinInput.setVisible(true);
        skinInput.setBordered(true);
        skinInput.setTextColor(0xFFFFFFFF);
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
                                        var profile = ((AAbstractClientPlayer) this.minecraft.player).ft_getPlayerInfo().getProfile();

                                        // could return an empty collection, Iterator#next in this case produces NoSuchElementException
                                        Optional<Property> optionalProperty = profile.properties()
                                                .get(TailoredPlayer.PROPERTY_TEXTURES)
                                                .stream()
                                                .findFirst();
                                        if (optionalProperty.isPresent()) {
                                            CustomPacketPayload payload = new DefaultSkinPayload(optionalProperty.get());
                                            ClientPlayNetworking.send(payload);
                                        }
                                        this.onClose();
                                    })
                            .pos(width / 2 - BUTTON_WIDTH / 2 - 1, buttonY)
                            .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                            .build());
        }
    }

    private void clearSkin() {
        if (TAILORED_SERVER) {
            this.minecraft.player.connection.sendCommand("skin clear");
        } else {
            ((AAbstractClientPlayer) this.minecraft.player).ft_getPlayerInfo().getProfile().properties().removeAll(TailoredPlayer.PROPERTY_TEXTURES);
            // Reload skin - todo
        }
    }

    private void applyNewSkin() {
        Logging.debug("Applying new skin! On tab: " + this.selectedTab.getTitle().getString() + " with parameter: " + skinInput.getValue() + ". Slim skin: " + this.skinModelCheckbox.selected());
        new CompletableFuture<>().completeAsync(() -> {
            final var packetInfo = this.selectedTab.getSkinChangePacket(minecraft.player, skinInput.getValue(), this.skinModelCheckbox.selected());
            packetInfo.ifPresent(packet -> {
                if (TAILORED_SERVER) {
                    ClientPlayNetworking.send(packet);
                } else {
                    // Change skin clientside only todo: reload skin
                    PropertyMap map = ((AAbstractClientPlayer) this.minecraft.player).ft_getPlayerInfo().getProfile().properties();

                    /*try {
                        map.removeAll(TailoredPlayer.PROPERTY_TEXTURES);
                    } catch (Exception ignored) {
                        // Player has no skin data, no worries
                    }

                    var skinData = packet.getSecond().readProperty();
                    map.put(TailoredPlayer.PROPERTY_TEXTURES, skinData);
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
    public void extractRenderState(GuiGraphicsExtractor graphicsExtractor, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphicsExtractor, mouseX, mouseY, delta);

        // Screen title
        graphicsExtractor.centeredText(this.font, title, width / 2, 15, -1);

        // Starting position of the window texture
        this.startX = (this.width - 252) / 2;
        this.startY = (this.height - 140) / 2;

        // Window texture
        graphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, AAdvancementsScreen.getWINDOW_LOCATION(), startX, startY, 0, 0, 252, 140, 256, 256);


        this.skinInput.extractWidgetRenderState(graphicsExtractor, startX, startY, delta);

        // Other renders
        this.drawTabs(graphicsExtractor, startX, startY);
        this.drawIcons(graphicsExtractor, startX, startY);
        this.drawWidgetTooltips(graphicsExtractor, startX, startY, mouseX, mouseY);

        int x = this.startX + 24;
        int y = this.startY - 76;
        if (this.selectedTab.showModelBackwards()) {
            extractEntityInInventoryFollowsMouseBackwards(graphicsExtractor, x, y, x + 75, y + 208, 48, 1.0f, mouseX + 2, mouseY - 16, this.minecraft.player);
        } else {
            // Drawing Player
            // Luckily vanilla code is available
            InventoryScreen.extractEntityInInventoryFollowsMouse(graphicsExtractor, x, y, x + 75, y + 208, 48, 1.0f, mouseX + 2, mouseY - 16, this.minecraft.player);
        }
    }

    public static void extractEntityInInventoryFollowsMouseBackwards(final GuiGraphicsExtractor graphicsExtractor, final int x0, final int y0, final int x1, final int y1, final int size, final float offsetY, final float mouseX, final float mouseY, final LivingEntity entity) {
        float centerX = (float)(x0 + x1) / 2.0F;
        float centerY = (float)(y0 + y1) / 2.0F;
        float xAngle = (float)Math.atan((mouseX - centerX) / 40.0F);
        float yAngle = (float)Math.atan((centerY - mouseY) / 40.0F);
        Quaternionf rotation = (new Quaternionf()).rotateZ((float)Math.PI);
        Quaternionf xRotation = (new Quaternionf()).rotateX(yAngle * 20.0F * ((float)Math.PI / 180F));
        var backwardsBodyRot = new Quaternionf().rotateAxis((180.0F + xAngle * 20.0F) * ((float)Math.PI / 180F), 0.0F, 1.0F, 0.0F);
        rotation.mul(xRotation);
        rotation.mul(backwardsBodyRot);

        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer<? super LivingEntity, ?> renderer = entityRenderDispatcher.getRenderer(entity);
        EntityRenderState renderState = renderer.createRenderState(entity, 1.0F);
        renderState.shadowPieces.clear();
        renderState.outlineColor = 0;

        if (renderState instanceof LivingEntityRenderState livingRenderState) {
            livingRenderState.bodyRot = 180.0F + xAngle * 20.0F;
            livingRenderState.yRot = xAngle * 20.0F;
            if (livingRenderState.pose != Pose.FALL_FLYING) {
                livingRenderState.xRot = -yAngle * 20.0F;
            } else {
                livingRenderState.xRot = 0.0F;
            }

            livingRenderState.boundingBoxWidth /= livingRenderState.scale;
            livingRenderState.boundingBoxHeight /= livingRenderState.scale;
            livingRenderState.scale = 1.0F;
        }

        Vector3f translation = new Vector3f(0.0F, renderState.boundingBoxHeight / 2.0F + offsetY, 0.0F);
        graphicsExtractor.entity(renderState, (float)size, translation, rotation, xRotation, x0, y0, x1, y1);
    }

    /**
     * Draws tabs.
     *
     * @param graphicsExtractor
     * @param startX      x where skin window starts
     * @param startY      y where skin window starts
     */
    private void drawTabs(GuiGraphicsExtractor graphicsExtractor, int startX, int startY) {
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

            int index = tab.getTabType().getMax() - i - 1;
            int tabX = startX + tab.getTabType().getX(index);
            int tabY = startY + tab.getTabType().getY(index);

            tab.getTabType().extractRenderState(graphicsExtractor, tabX, tabY, selected, index);
        }

        // Rendering title
        graphicsExtractor.text(this.font, this.selectedTab.getTitle(), startX + 10, startY + 5, 0xFFFFFFFF);

        // Rendering description above input field
        graphicsExtractor.text(this.font, this.selectedTab.getDescription(), width / 2, height / 2 - 40, 0xFFFFFFFF);
    }


    /**
     * Draws ItemStack icons on the tabs.
     *
     * @param startX x where skin window starts
     * @param startY y where skin window starts
     */
    private void drawIcons(GuiGraphicsExtractor graphicsExtractor, int startX, int startY) {
        // Icons
        for (int i = 0; i < TABS.size(); ++i) {
            SkinTabType tab = TABS.get(i);
            tab.getTabType().extractIcon(graphicsExtractor, startX, startY, tab.getTabType().getMax() - i - 1, tab.getIcon());
        }
    }


    /**
     * Draws tooltips when hovering over tabs.
     *
     * @param graphicsExtractor
     * @param startX      x where skin window starts
     * @param startY      y where skin window starts
     * @param mouseX      mouse x
     * @param mouseY      mouse y
     */
    private void drawWidgetTooltips(GuiGraphicsExtractor graphicsExtractor, int startX, int startY, int mouseX, int mouseY) {
        for (int i = 0; i < TABS.size(); ++i) {
            SkinTabType tab = TABS.get(i);

            ClientTooltipComponent clientTooltipComponent = ClientTooltipComponent.create(tab.getTitle().getVisualOrderText());
            if (tab.getTabType().isMouseOver(startX, startY, tab.getTabType().getMax() - i - 1, mouseX, mouseY)) {
                //graphicsExtractor.tooltip(this.font, List.of(clientTooltipComponent), mouseX, mouseY, DefaultTooltipPositioner.INSTANCE, null);
                graphicsExtractor.setTooltipForNextFrame(this.font, tab.getTitle(), mouseX, mouseY);
                break;
            }
        }
    }

    /**
     * Checks if one of the tabs was clicked
     * and selects it accordingly.
     *
     * @param mouseButtonEvent The mouse event
     *
     * @return super.mouseClicked()
     */
    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        if (mouseButtonEvent.button() == 0) {
            for (int i = 0; i < TABS.size(); ++i) {
                SkinTabType tab = TABS.get(i);

                final boolean mouseOver = tab.getTabType().isMouseOver(startX, startY, tab.getTabType().getMax() - i - 1, mouseButtonEvent.x(), mouseButtonEvent.y());

                if (mouseOver) {
                    this.selectedTab = tab;
                    break;
                }
            }
        }
        return super.mouseClicked(mouseButtonEvent, bl);
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
