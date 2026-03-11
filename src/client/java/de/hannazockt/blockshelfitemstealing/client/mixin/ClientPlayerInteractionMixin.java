package de.hannazockt.blockshelfitemstealing.client.mixin;

import de.hannazockt.blockshelfitemstealing.client.event.ShelfInteractionHandler;
import de.hannazockt.blockshelfitemstealing.client.util.ShelfDetector;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Secondary safety net interception layer.
 */
@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionMixin {

    @Shadow @Final private MinecraftClient client;

    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    private void onInteractBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        ClientWorld world = this.client.world;
        if (world == null) return;

        var pos = hitResult.getBlockPos();
        var state = world.getBlockState(pos);

        if (ShelfDetector.isShelf(state)) {
            boolean block = ShelfInteractionHandler.handleInteraction(world, pos, player);
            if (block) {
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }
}