package de.hannazockt.blockshelfitemstealing.client.config;

/**
 * Simple configuration holding the protection mode.
 */
public class ModConfig {
    public enum ProtectionMode {
        BLOCK_POWERED_ONLY,
        BLOCK_ALL,
        DISABLED
    }

    // Default mode; since no persistence was requested, this remains static in memory.
    public static ProtectionMode currentMode = ProtectionMode.BLOCK_ALL;
}