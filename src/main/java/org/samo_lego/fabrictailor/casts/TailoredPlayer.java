package org.samo_lego.fabrictailor.casts;

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
     * @return true if it was successful, otherwise false
     */
    boolean setSkin(String value, String signature);

    /**
     * Gets player's skin value.
     *
     * @return skin value as string
     */
    String getSkinValue();

    /**
     * Gets player's skin signature.
     *
     * @return skin signature as string
     */
    String getSkinSignature();
}
