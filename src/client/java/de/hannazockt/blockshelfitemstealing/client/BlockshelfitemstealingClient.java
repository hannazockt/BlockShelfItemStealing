package de.hannazockt.blockshelfitemstealing.client;

import de.hannazockt.blockshelfitemstealing.client.event.ShelfInteractionHandler;
import de.hannazockt.blockshelfitemstealing.client.keybind.ModKeyBindings;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;

/**
 * Main client entrypoint for BlockShelfItemStealing.
 */
@Environment(EnvType.CLIENT)
public class BlockshelfitemstealingClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModKeyBindings.register();
        UseBlockCallback.EVENT.register(new ShelfInteractionHandler());
    }
}