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

    // Helper enum for the switch statement to determine the message type
    private enum BlockReason {
        NORMAL,
        POWERED,
        POWERED_SUSPICIOUS
    }

    @Override
    public ActionResult interact(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        if (!world.isClient()) return ActionResult.PASS;

        var state = world.getBlockState(hitResult.getBlockPos());

        if (!ShelfDetector.isShelf(state)) {
            return ActionResult.PASS;
        }

        return handleInteraction(world, hitResult.getBlockPos(), state, player)
                ? ActionResult.FAIL
                : ActionResult.PASS;
    }

    /**
     * Checks logic and sends actionbar messages. Returns true if interaction should be blocked.
     */
    public static boolean handleInteraction(World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.BlockState state, PlayerEntity player) {
        if (ModConfig.currentMode == ModConfig.ProtectionMode.DISABLED) {
            return false;
        }

        boolean bypassHeld = ModKeyBindings.bypassKey.isPressed();
        boolean isPowered = world.isReceivingRedstonePower(pos);
        boolean isSuspicious = ShelfDetector.isSuspiciousShelfSystem(world, pos);

        if (bypassHeld) {
            sendCooldownMessage(player, Text.translatable("blockshelfitemstealing.message.bypass.active"));
            return false;
        }

        boolean shouldBlock = (ModConfig.currentMode == ModConfig.ProtectionMode.BLOCK_ALL) ||
                (ModConfig.currentMode == ModConfig.ProtectionMode.BLOCK_POWERED_ONLY && isPowered);

        if (shouldBlock) {
            // Determine the reason for blocking to pick the right text
            BlockReason reason;
            if (isPowered) {
                reason = isSuspicious ? BlockReason.POWERED_SUSPICIOUS : BlockReason.POWERED;
            } else {
                reason = BlockReason.NORMAL;
            }

            // Select the appropriate message based on the reason
            MutableText msgText = switch (reason) {
                case POWERED_SUSPICIOUS -> Text.translatable("blockshelfitemstealing.message.blocked.inv_steal").copy();
                case POWERED -> Text.translatable("blockshelfitemstealing.message.blocked.powered").copy();
                case NORMAL -> Text.translatable("blockshelfitemstealing.message.blocked").copy();
            };

            String keyName = ModKeyBindings.bypassKey.getBoundKeyLocalizedText().getString();
            msgText.append(Text.translatable("blockshelfitemstealing.message.blocked.hint", keyName));

            sendCooldownMessage(player, msgText);
            return true;
        }

        return false;
    }

    private static void sendCooldownMessage(PlayerEntity player, Text message) {
        long now = System.currentTimeMillis();
        if (now - lastMessageTime > COOLDOWN_MS) {
            MinecraftClient.getInstance().inGameHud.setOverlayMessage(message, false);
            lastMessageTime = now;
        }
    }
}