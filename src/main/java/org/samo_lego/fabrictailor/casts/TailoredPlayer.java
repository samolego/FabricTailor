package org.samo_lego.fabrictailor.casts;

import com.mojang.authlib.properties.Property;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Includes additional methods for skin changes.
 */
public interface TailoredPlayer {
    /**
     * Reloads player's skin.
     */
    void fabrictailor_reloadSkin();

    /**
     * @deprecated use {@link #fabrictailor_reloadSkin()} instead.
     */
    @Deprecated
    default void reloadSkin() {
        this.fabrictailor_reloadSkin();
    }


    /**
     * Sets the skin to the specified player.
     *
     * @param skinData skin texture data
     * @param reload   whether to send packets around for skin reload
     */
    void fabrictailor_setSkin(Property skinData, boolean reload);

    /**
     * @deprecated use {@link #fabrictailor_setSkin(Property, boolean)} instead.
     */
    @Deprecated
    default void setSkin(Property skinData, boolean reload) {
        this.fabrictailor_setSkin(skinData, reload);
    }

    /**
     * Sets the skin to the specified player.
     *
     * @param value     skin texture value
     * @param signature skin texture signature
     * @param reload    whether to send packets around for skin reload
     */
    void fabrictailor_setSkin(String value, String signature, boolean reload);


    /**
     * @deprecated use {@link #fabrictailor_setSkin(String, String, boolean)} instead.
     */
    @Deprecated
    default void setSkin(String value, String signature, boolean reload) {
        this.fabrictailor_setSkin(value, signature, reload);
    }

    /**
     * Gets player's skin value.
     *
     * @return skin value as string, null if player has no skin set.
     */
    Optional<String> fabrictailor_getSkinValue();


    /**
     * @deprecated use {@link #fabrictailor_getSkinValue()} instead.
     */
    @Deprecated
    @Nullable
    default String getSkinValue() {
        return this.fabrictailor_getSkinValue().orElse(null);
    }

    /**
     * Gets player's skin signature.
     *
     * @return skin signature as string, null if player has no skin set.
     */
    Optional<String> fabrictailor_getSkinSignature();


    /**
     * @deprecated use {@link #fabrictailor_getSkinSignature()} instead.
     */
    @Deprecated
    @Nullable
    default String getSkinSignature() {
        return this.fabrictailor_getSkinSignature().orElse(null);
    }

    /**
     * Gets the most recent time when player changed their skin.
     *
     * @return time of skin change.
     */
    long fabrictailor_getLastSkinChange();


    /**
     * @deprecated use {@link #fabrictailor_getLastSkinChange()} instead.
     */
    @Deprecated
    default long getLastSkinChange() {
        return this.fabrictailor_getLastSkinChange();
    }

    /**
     * Resets the skin timer.
     *
     * @deprecated use {@link #fabrictailor_resetLastSkinChange()} ()} instead.
     */
    @Deprecated
    default void resetLastSkinChange() {
        this.fabrictailor_resetLastSkinChange();
    }

    /**
     * Resets the skin timer.
     */
    void fabrictailor_resetLastSkinChange();

    /**
     * Clears player's skin.
     *
     * @deprecated use {@link #fabrictailor_clearSkin()} instead.
     */
    @Deprecated
    default void clearSkin() {
        this.fabrictailor_clearSkin();
    }

    void fabrictailor_clearSkin();


    /**
     * Helper function to get texture hash from skin
     * that was set with the mod.
     * <p>
     * Can be used with <a href="https://mc-heads.net/avatar/">https://mc-heads.net/avatar/%7Btextureid%7D</a>
     * to get the head texture.
     * </p>
     *
     * @return player's skin id (hash)
     */
    String fabrictailor_getSkinId();

    /**
     * @deprecated use {@link #fabrictailor_getSkinId()} instead.
     */
    @Deprecated
    default String getSkinId() {
        return this.fabrictailor_getSkinId();
    }
}
