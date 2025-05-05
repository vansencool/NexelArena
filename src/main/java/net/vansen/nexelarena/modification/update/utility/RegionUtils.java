package net.vansen.nexelarena.modification.update.utility;

import net.minecraft.world.level.block.state.BlockState;
import net.vansen.nexelarena.modification.update.BlockUpdate;
import net.vansen.nexelarena.modification.update.ChunkUpdates;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for creating and managing block and chunk updates in a region.
 */
@SuppressWarnings("unused")
public class RegionUtils {

    /**
     * Creates a list of BlockUpdate instances for all blocks in a specified region.
     * <p>
     * It is important to note if the region is more than one chunk, IT WILL NOT WORK PROPERLY WHEN USED IN NEXEL LEVEL!!
     *
     * @param start The starting location of the region.
     * @param end   The ending location of the region.
     * @return A list of BlockUpdate instances representing the current region.
     * @see RegionUtils#createChunkUpdates(Location, Location)
     */
    public static List<BlockUpdate> createBlockUpdates(@NotNull Location start, @NotNull Location end) {
        World world = start.getWorld();
        List<BlockUpdate> updates = new ArrayList<>();

        int minX = Math.min(start.getBlockX(), end.getBlockX());
        int minY = Math.min(start.getBlockY(), end.getBlockY());
        int minZ = Math.min(start.getBlockZ(), end.getBlockZ());
        int maxX = Math.max(start.getBlockX(), end.getBlockX());
        int maxY = Math.max(start.getBlockY(), end.getBlockY());
        int maxZ = Math.max(start.getBlockZ(), end.getBlockZ());

        int chunkMinX = minX >> 4;
        int chunkMaxX = maxX >> 4;
        int chunkMinZ = minZ >> 4;
        int chunkMaxZ = maxZ >> 4;

        for (int chunkX = chunkMinX; chunkX <= chunkMaxX; chunkX++) {
            for (int chunkZ = chunkMinZ; chunkZ <= chunkMaxZ; chunkZ++) {
                var chunk = ((CraftWorld) world).getHandle().getChunk(chunkX, chunkZ);

                for (int x = Math.max(minX, chunkX << 4); x <= Math.min(maxX, (chunkX << 4) + 15); x++) {
                    for (int y = minY; y <= maxY; y++) {
                        for (int z = Math.max(minZ, chunkZ << 4); z <= Math.min(maxZ, (chunkZ << 4) + 15); z++) {
                            BlockState state = chunk.getBlockState(x & 15, y, z & 15);
                            updates.add(new BlockUpdate(x, y, z, state));
                        }
                    }
                }
            }
        }
        return updates;
    }

    /**
     * Creates a ChunkUpdates instance for all chunks in a specified region.
     *
     * @param start The starting location of the region.
     * @param end   The ending location of the region.
     * @return A list of ChunkUpdates instances representing the region.
     */
    public static List<ChunkUpdates> createChunkUpdates(@NotNull Location start, @NotNull Location end) {
        World world = start.getWorld();
        List<ChunkUpdates> chunkUpdatesList = new ArrayList<>();

        int minX = Math.min(start.getBlockX(), end.getBlockX());
        int minY = Math.min(start.getBlockY(), end.getBlockY());
        int minZ = Math.min(start.getBlockZ(), end.getBlockZ());
        int maxX = Math.max(start.getBlockX(), end.getBlockX());
        int maxY = Math.max(start.getBlockY(), end.getBlockY());
        int maxZ = Math.max(start.getBlockZ(), end.getBlockZ());

        int chunkMinX = minX >> 4;
        int chunkMaxX = maxX >> 4;
        int chunkMinZ = minZ >> 4;
        int chunkMaxZ = maxZ >> 4;

        for (int chunkX = chunkMinX; chunkX <= chunkMaxX; chunkX++) {
            for (int chunkZ = chunkMinZ; chunkZ <= chunkMaxZ; chunkZ++) {
                ChunkUpdates chunkUpdates = new ChunkUpdates(chunkX, chunkZ);
                var chunk = ((CraftWorld) world).getHandle().getChunk(chunkX, chunkZ);

                for (int x = Math.max(minX, chunkX << 4); x <= Math.min(maxX, (chunkX << 4) + 15); x++) {
                    for (int y = minY; y <= maxY; y++) {
                        for (int z = Math.max(minZ, chunkZ << 4); z <= Math.min(maxZ, (chunkZ << 4) + 15); z++) {
                            BlockState state = chunk.getBlockState(x & 15, y, z & 15);
                            chunkUpdates.updates.add(new BlockUpdate(x, y, z, state));
                        }
                    }
                }
                chunkUpdatesList.add(chunkUpdates);
            }
        }
        return chunkUpdatesList;
    }

    /**
     * Creates a single BlockUpdate for a specific block.
     *
     * @param location The location of the block.
     * @param newState The new block state to set.
     * @return A BlockUpdate instance for the block.
     */
    public static BlockUpdate createSingleBlockUpdate(@NotNull Location location, @NotNull org.bukkit.block.BlockState newState) {
        return new BlockUpdate(location.getBlockX(), location.getBlockY(), location.getBlockZ(), ((CraftBlockState) newState).getHandle());
    }

    /**
     * Creates a list of BlockUpdate instances for a region, setting all blocks to a specific state.
     * <p>
     * It is important to note if the region is more than one chunk, IT WILL NOT WORK PROPERLY WHEN USED IN NEXEL LEVEL!!
     *
     * @param start    The starting location of the region.
     * @param end      The ending location of the region.
     * @param newState The new block state to set for all blocks in the region.
     * @return A list of BlockUpdate instances representing the region with the new state.
     */
    public static List<BlockUpdate> setBlockState(@NotNull Location start, @NotNull Location end, @NotNull org.bukkit.block.BlockState newState) {
        List<BlockUpdate> updates = new ArrayList<>();

        int minX = Math.min(start.getBlockX(), end.getBlockX());
        int minY = Math.min(start.getBlockY(), end.getBlockY());
        int minZ = Math.min(start.getBlockZ(), end.getBlockZ());
        int maxX = Math.max(start.getBlockX(), end.getBlockX());
        int maxY = Math.max(start.getBlockY(), end.getBlockY());
        int maxZ = Math.max(start.getBlockZ(), end.getBlockZ());

        int chunkMinX = minX >> 4;
        int chunkMaxX = maxX >> 4;
        int chunkMinZ = minZ >> 4;
        int chunkMaxZ = maxZ >> 4;

        for (int chunkX = chunkMinX; chunkX <= chunkMaxX; chunkX++) {
            for (int chunkZ = chunkMinZ; chunkZ <= chunkMaxZ; chunkZ++) {
                for (int x = Math.max(minX, chunkX << 4); x <= Math.min(maxX, (chunkX << 4) + 15); x++) {
                    for (int y = minY; y <= maxY; y++) {
                        for (int z = Math.max(minZ, chunkZ << 4); z <= Math.min(maxZ, (chunkZ << 4) + 15); z++) {
                            updates.add(new BlockUpdate(x, y, z, ((CraftBlockState) newState).getHandle()));
                        }
                    }
                }
            }
        }
        return updates;
    }

    /**
     * Creates a list of ChunkUpdates for a region, setting all blocks to a specific state.
     *
     * @param start    The starting location of the region.
     * @param end      The ending location of the region.
     * @param newState The new block state to set for all blocks in the region.
     * @return A list of ChunkUpdates instances representing the region with the new state.
     */
    public static List<ChunkUpdates> setChunkState(@NotNull Location start, @NotNull Location end, @NotNull org.bukkit.block.BlockState newState) {
        World world = start.getWorld();
        List<ChunkUpdates> chunkUpdatesList = new ArrayList<>();

        int minX = Math.min(start.getBlockX(), end.getBlockX());
        int minY = Math.min(start.getBlockY(), end.getBlockY());
        int minZ = Math.min(start.getBlockZ(), end.getBlockZ());
        int maxX = Math.max(start.getBlockX(), end.getBlockX());
        int maxY = Math.max(start.getBlockY(), end.getBlockY());
        int maxZ = Math.max(start.getBlockZ(), end.getBlockZ());

        int chunkMinX = minX >> 4;
        int chunkMaxX = maxX >> 4;
        int chunkMinZ = minZ >> 4;
        int chunkMaxZ = maxZ >> 4;

        for (int chunkX = chunkMinX; chunkX <= chunkMaxX; chunkX++) {
            for (int chunkZ = chunkMinZ; chunkZ <= chunkMaxZ; chunkZ++) {
                ChunkUpdates chunkUpdates = new ChunkUpdates(chunkX, chunkZ);

                for (int x = Math.max(minX, chunkX << 4); x <= Math.min(maxX, (chunkX << 4) + 15); x++) {
                    for (int y = minY; y <= maxY; y++) {
                        for (int z = Math.max(minZ, chunkZ << 4); z <= Math.min(maxZ, (chunkZ << 4) + 15); z++) {
                            chunkUpdates.updates.add(new BlockUpdate(x, y, z, ((CraftBlockState) newState).getHandle()));
                        }
                    }
                }
                chunkUpdatesList.add(chunkUpdates);
            }
        }
        return chunkUpdatesList;
    }
}
