package org.samo_lego.fabrictailor.client;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.samo_lego.fabrictailor.util.TranslatedText;

public class SkinTab extends DrawableHelper {

    private final TailorStyle type;
    private final TranslatedText TITLE;
    private final TranslatedText DESCRIPTION;
    private final ItemStack ICON;
    private final int width;
    private final int height;
    private String skinCommand = "/skin set ";

    public SkinTab(TailorStyle type, int width, int height) {
        this.TITLE = new TranslatedText(type.getTitle());
        this.width = width;
        this.height = height;
        this.type = type;

        switch(type) {
            case PLAYER -> {
                this.ICON = new ItemStack(Items.PLAYER_HEAD);
                this.DESCRIPTION = new TranslatedText("description.fabrictailor.title_player");
                this.skinCommand += "player ";
            }
            case LOCAL -> {
                this.ICON = new ItemStack(Items.MAGENTA_GLAZED_TERRACOTTA);
                this.DESCRIPTION = new TranslatedText("description.fabrictailor.title_local");
                this.skinCommand += "upload ";
            }
            default -> {
                this.ICON = new ItemStack(Items.GLOBE_BANNER_PATTERN);
                this.DESCRIPTION = new TranslatedText("description.fabrictailor.title_url");
                this.skinCommand += "URL ";
            }
        }
    }

    public TranslatedText getTitle() {
        return this.TITLE;
    }

    public TranslatedText getDescription() {
        return this.DESCRIPTION;
    }
    public ItemStack getIcon() {
        return this.ICON;
    }

    public boolean isSelected(int startX, int startY, int mouseX, int mouseY) {
        return mouseX > startX && mouseX < startX + this.width && mouseY > startY && mouseY < startY + this.height;
    }

    public String getSkinCommand() {
        return this.skinCommand;
    }
    public boolean hasModels() {
        return this.type != TailorStyle.PLAYER;
    }

    public TailorStyle getType() {
        return this.type;
    }


    public enum TailorStyle {
        LOCAL("tab.fabrictailor.title_local"),
        URL("tab.fabrictailor.title_url"),
        PLAYER("tab.fabrictailor.title_player");

        private final String TITLE;

        TailorStyle(String title) {
            this.TITLE = title;
        }

        public String getTitle() {
            return this.TITLE;
        }
    }
}
