package org.samo_lego.fabrictailor.casts;

import org.jetbrains.annotations.Nullable;

/**
 * Includes additional methods for skin changes.
 */
public interface TailoredPlayer {
    /**
     * Reloads player's skin.
     */
    void reloadSkin();

    /**
     * Sets the skin to the specified player.
     *
     * @param value skin texture value
     * @param signature skin texture signature
     * @param reload whether to send packets around for skin reload
     * @return true if it was successful, otherwise false
     */
    boolean setSkin(String value, String signature, boolean reload);

    /**
     * Gets player's skin value.
     *
     * @return skin value as string
     */
    @Nullable
    String getSkinValue();

    /**
     * Gets player's skin signature.
     *
     * @return skin signature as string
     */
    @Nullable
    String getSkinSignature();
}
