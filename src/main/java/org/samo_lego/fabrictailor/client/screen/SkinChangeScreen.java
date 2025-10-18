package org.samo_lego.fabrictailor.client.screen;

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;
import org.samo_lego.fabrictailor.client.screen.tabs.CapeTab;
import org.samo_lego.fabrictailor.client.screen.tabs.LocalSkinTab;
import org.samo_lego.fabrictailor.client.screen.tabs.PlayerSkinTab;
import org.samo_lego.fabrictailor.client.screen.tabs.SkinTabType;
import org.samo_lego.fabrictailor.client.screen.tabs.UrlSkinTab;
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
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);

        // Screen title
        guiGraphics.drawCenteredString(this.font, title, width / 2, 15, -1);

        // Starting position of the window texture
        this.startX = (this.width - 252) / 2;
        this.startY = (this.height - 140) / 2;

        // Window texture
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, AdvancementsScreen.WINDOW_LOCATION, startX, startY, 0, 0, 252, 140, 256, 256);


        this.skinInput.render(guiGraphics, startX, startY, delta);

        // Other renders
        this.drawTabs(guiGraphics, startX, startY);
        this.drawIcons(guiGraphics, startX, startY);
        this.drawWidgetTooltips(guiGraphics, startX, startY, mouseX, mouseY);

        int x = this.startX + 24;
        int y = this.startY - 76;
        if (this.selectedTab.showModelBackwards()) {
            renderEntityInInventoryFollowsMouseBackwards(guiGraphics, x, y, x + 75, y + 208, 48, 1.0f, mouseX + 2, mouseY - 16, this.minecraft.player);
        } else {
            // Drawing Player
            // Luckily vanilla code is available
            InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, x, y, x + 75, y + 208, 48, 1.0f, mouseX + 2, mouseY - 16, this.minecraft.player);
        }
    }

    public void renderEntityInInventoryFollowsMouseBackwards(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, float f, float g, float h, LivingEntity livingEntity) {
        float mousex = -(((float) width / 2) - 75 - g);
        float mousey = ((float) height / 2) - h;
        float n = (float) (i + k) / 2.0F;
        float o = (float) (j + l) / 2.0F;
        float p = (float) Math.atan(mousex / 40.0f);
        float q = (float) Math.atan(mousey / 40.0f);
        guiGraphics.enableScissor(i, j, k, l);
        //float p = (float) Math.atan((double) ((n - g) / 40.0F));
        //float q = (float) Math.atan((double) ((o - h) / 40.0F));
        Quaternionf quaternionf = (new Quaternionf()).rotateZ((float) Math.PI);
        Quaternionf quaternionf2 = (new Quaternionf()).rotateX(q * 20.0F * (float) (Math.PI / 180.0));
        quaternionf.mul(quaternionf2);
        float r = livingEntity.yBodyRot;
        float s = livingEntity.getYRot();
        float t = livingEntity.getXRot();
        float u = livingEntity.yHeadRotO;
        float v = livingEntity.yHeadRot;
        livingEntity.yBodyRot = p * 40.0F;
        livingEntity.setYRot(p * 40.0F);
        livingEntity.setXRot(-q * 40.0F);
        livingEntity.yHeadRot = livingEntity.getYRot();
        livingEntity.yHeadRotO = livingEntity.getYRot();
        float w = livingEntity.getScale();
        Vector3f vector3f = new Vector3f(0.0F, livingEntity.getBbHeight() / 2.0F + f * w, 0.0F);
        float x = (float) m / w;
        InventoryScreen.renderEntityInInventory(guiGraphics, i, j, k, l, x, vector3f, quaternionf, quaternionf2, livingEntity);
        livingEntity.yBodyRot = r;
        livingEntity.setYRot(s);
        livingEntity.setXRot(t);
        livingEntity.yHeadRotO = u;
        livingEntity.yHeadRot = v;
        guiGraphics.disableScissor();
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
        guiGraphics.drawString(this.font, this.selectedTab.getTitle(), startX + 10, startY + 5, 0xFFFFFFFF);

        // Rendering description above input field
        guiGraphics.drawString(this.font, this.selectedTab.getDescription(), width / 2, height / 2 - 40, 0xFFFFFFFF);
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

            ClientTooltipComponent clientTooltipComponent = ClientTooltipComponent.create(tab.getTitle().getVisualOrderText());
            if (tab.getTabType().isMouseOver(startX, startY, tab.getTabType().getMax() - i - 1, mouseX, mouseY)) {
                guiGraphics.renderTooltip(this.font, List.of(clientTooltipComponent), mouseX, mouseY, DefaultTooltipPositioner.INSTANCE, null);
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
