package org.samo_lego.fabrictailor;

import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.nbt.CompoundTag;

interface SkinSaveData extends Component {
    String getValue();
    void setValue(String value);
    String getSignature();
    void setSignature(String signature);
}

public class SkinSaver implements SkinSaveData {
    private String value = "";
    private String signature = "";

    @Override
    public String getValue() {
        return this.value;
    }
    @Override
    public void setValue(String value) {
        this.value = value;
    }
    @Override
    public String getSignature() {
        return this.signature;
    }
    @Override
    public void setSignature(String signature) {
        this.signature = signature;
    }

    @Override public void fromTag(CompoundTag tag) {
        this.value = tag.getString("value");
        this.signature = tag.getString("signature");
    }

    @Override public CompoundTag toTag(CompoundTag tag) {
        tag.putString("value", this.value);
        tag.putString("signature", this.signature);
        return tag;
    }
}