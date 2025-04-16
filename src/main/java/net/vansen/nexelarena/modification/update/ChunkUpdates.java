package net.vansen.nexelarena.modification.update;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

/**
 * Represents a chunk update.
 * <p>
 * This class is used to batch chunk updates in an easily manageable way.
 */
public class ChunkUpdates {

    /**
     * The list of block updates in this chunk.
     */
    public final List<BlockUpdate> updates = new ObjectArrayList<>();

    public final int chunkX;
    public final int chunkZ;

    /**
     * Creates a new ChunkUpdates instance.
     *
     * @param chunkX The x coordinate of the chunk.
     * @param chunkZ The z coordinate of the chunk.
     */
    public ChunkUpdates(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }
}