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
    private String value;
    private String signature;

    public SkinSaver(String value, String signature) {
        this.value = value;
        this.signature = signature;
    }
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
        CompoundTag skinDataTag = tag.getCompound("skin_data");
        this.value = skinDataTag.getString("value");
        this.signature = skinDataTag.getString("signature");
    }

    @Override public CompoundTag toTag(CompoundTag tag) {
        CompoundTag skinDataTag = new CompoundTag();
        skinDataTag.putString("value", this.value);
        skinDataTag.putString("signature", this.signature);

        tag.put("skin_data", skinDataTag);
        return tag;
    }
}