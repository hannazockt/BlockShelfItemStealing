package de.hannazockt.blockshelfitemstealing.client.event;

import de.hannazockt.blockshelfitemstealing.client.config.ModConfig;
import de.hannazockt.blockshelfitemstealing.client.keybind.ModKeyBindings;
import de.hannazockt.blockshelfitemstealing.client.util.ShelfDetector;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

/**
 * Primary interception layer using Fabric API UseBlockCallback.
 */
public class ShelfInteractionHandler implements UseBlockCallback {

    private static long lastMessageTime = 0;
    private static final long COOLDOWN_MS = 2000;
			private static final String BLOCKED_MESSAGE_KEY = "blockshelfitemstealing.message.blocked";

    private enum BlockReason {
        NORMAL,
        POWERED,
        POWERED_SUSPICIOUS
    }

    @Override
    public ActionResult interact(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {

        // Check if is a client
        if (!world.isClient()) {
            return ActionResult.PASS;
        }

        var state = world.getBlockState(hitResult.getBlockPos());

        // Shelf check
        if (!ShelfDetector.isShelf(state)) {
            return ActionResult.PASS;
        }

        boolean blocked = handleInteraction(world, hitResult.getBlockPos(), state, player);

        return blocked ? ActionResult.FAIL : ActionResult.PASS;
    }

    /**
     * Main protection logic.
     * @return true if interaction should be blocked
     */
    public static boolean handleInteraction(World world,
                                            net.minecraft.util.math.BlockPos pos,
                                            net.minecraft.block.BlockState state,
                                            PlayerEntity player) {

        // Return false if protection is disabled
        if (ModConfig.currentMode == ModConfig.ProtectionMode.DISABLED) {
            return false;
        }

        // Bypass key logic
        if (ModKeyBindings.bypassKey.isPressed()) {
            sendCooldownMessage(player,
                    Text.translatable("blockshelfitemstealing.message.bypass.active"));
            return false;
        }

        boolean isPowered = world.isReceivingRedstonePower(pos);

        // Powered only mode check
        if (ModConfig.currentMode == ModConfig.ProtectionMode.BLOCK_POWERED_ONLY && !isPowered) {
            return false;
        }

						// Check if 3 powered shelfs are besides each other to check if it would steal your entire hotbar
        boolean isSuspicious = ShelfDetector.isSuspiciousShelfSystem(world, pos);

        BlockReason reason = determineReason(isPowered, isSuspicious);

        MutableText message = createBlockMessage(reason);

        String keyName = ModKeyBindings.bypassKey
                .getBoundKeyLocalizedText()
                .getString();

        message.append(Text.translatable(
                BLOCKED_MESSAGE_KEY + ".hint",
                keyName
        ));

        sendCooldownMessage(player, message);

        return true;
    }

    private static BlockReason determineReason(boolean powered, boolean suspicious) {

        if (!powered) {
            return BlockReason.NORMAL;
        }

        if (suspicious) {
            return BlockReason.POWERED_SUSPICIOUS;
        }

        return BlockReason.POWERED;
    }

    private static MutableText createBlockMessage(BlockReason reason) {

        return switch (reason) {
            case POWERED_SUSPICIOUS ->
                    Text.translatable(BLOCKED_MESSAGE_KEY + ".inv_steal").copy();

            case POWERED ->
                    Text.translatable(BLOCKED_MESSAGE_KEY + ".powered").copy();

            case NORMAL ->
                    Text.translatable(BLOCKED_MESSAGE_KEY).copy();
        };
    }

    private static void sendCooldownMessage(PlayerEntity player, Text message) {

        long now = System.currentTimeMillis();

        if (now - lastMessageTime <= COOLDOWN_MS) {
            return;
        }

        MinecraftClient.getInstance()
                .inGameHud
                .setOverlayMessage(message, false);

        lastMessageTime = now;
    }
}
