package de.hannazockt.blockshelfitemstealing.client.util;

import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Utility for detecting shelves based on registry ID and network size.
 */
public class ShelfDetector {
    /**
     * Checks if the given block state is a shelf.
     */
    public static boolean isShelf(BlockState state) {
        String path = Registries.BLOCK.getId(state.getBlock()).getPath();
        return path.endsWith("_shelf");
    }

    /**
     * Checks if the shelf is connected to at least 2 other shelves (3 in total).
     * Aborts as soon as 3 shelves are found in the network to prevent lag.
     */
    public static boolean isSuspiciousShelfSystem(World world, BlockPos startPos) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();

        queue.add(startPos);
        visited.add(startPos);

        int shelfCount = 0;

        while (!queue.isEmpty() && shelfCount < 3) {
            BlockPos current = queue.poll();
            shelfCount++;

            // If we found 3 shelves (the clicked one + 2 others), it's considered suspicious
            if (shelfCount >= 3) {
                return true;
            }

            // Search for other shelves in all 6 directions
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = current.offset(dir);

                if (visited.contains(neighbor)) continue;

                BlockState neighborState = world.getBlockState(neighbor);
                if (!isShelf(neighborState)) continue;
                if (!world.isReceivingRedstonePower(neighbor)) continue;

                visited.add(neighbor);
                queue.add(neighbor);
            }

        }

        return false;
    }
}