package net.vansen.nexelarena.modification.update;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a chunk update.
 * <p>
 * This class is used to batch chunk updates in an easily manageable way.
 */
public class ChunkUpdates {

    /**
     * Map of section updates indexed by section index
     */
    private final Map<Integer, SectionUpdate> sectionUpdates = new ConcurrentHashMap<>();

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

    /**
     * Adds a block update to this chunk, organizing it by section.
     *
     * @param update       The block update to add
     * @param sectionIndex The section index for this update
     */
    public void addBlockUpdate(BlockUpdate update, int sectionIndex) {
        SectionUpdate sectionUpdate = sectionUpdates.computeIfAbsent(sectionIndex, SectionUpdate::new);
        sectionUpdate.updates.add(update);
    }

    /**
     * Gets all section updates in this chunk.
     *
     * @return Collection of section updates
     */
    public Collection<SectionUpdate> getSectionUpdates() {
        return sectionUpdates.values();
    }

    /**
     * Gets the number of block updates in this chunk.
     *
     * @return The number of block updates
     */
    public int getBlockUpdateCount() {
        return sectionUpdates.values().stream()
                .mapToInt(sectionUpdate -> sectionUpdate.updates.size())
                .sum();
    }
}
