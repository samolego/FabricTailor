package org.samo_lego.fabrictailor.casts;

import com.mojang.authlib.properties.Property;
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
     * @param skinData skin texture data
     * @param reload whether to send packets around for skin reload
     */
    void setSkin(Property skinData, boolean reload);

    /**
     * Sets the skin to the specified player.
     *
     * @param value skin texture value
     * @param signature skin texture signature
     * @param reload whether to send packets around for skin reload
     */
    void setSkin(String value, String signature, boolean reload);

    /**
     * Gets player's skin value.
     *
     * @return skin value as string, null if player has no skin set.
     */
    @Nullable
    String getSkinValue();

    /**
     * Gets player's skin signature.
     *
     * @return skin signature as string, null if player has no skin set.
     */
    @Nullable
    String getSkinSignature();

    /**
     * Gets the most recent time when player changed their skin.
     * @return time of skin change.
     */
    long getLastSkinChange();

    /**
     * Resets the skin timer.
     */
    void resetLastSkinChange();

    /**
     * Clears player's skin.
     */
    void clearSkin();


    /**
     * Helper function to get texture hash from skin
     * that was set with the mod.
     * <p>
     * Can be used with <a href="https://mc-heads.net/avatar/{textureid}">https://mc-heads.net/avatar/%7Btextureid%7D</a>
     * to get the head texture.
     * </p>
     *
     * @return player's skin id (hash)
     */
    String getSkinId();
}
