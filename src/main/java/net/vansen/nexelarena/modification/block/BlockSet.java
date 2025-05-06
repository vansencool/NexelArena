package net.vansen.nexelarena.modification.block;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.jetbrains.annotations.NotNull;

/**
 * This interface is used to set a block, you can implement this and pass it to NexelLevel.
 */
public interface BlockSet {

    /**
     * Sets a block in the chunk/section.
     *
     * @param x       The x coordinate of the block.
     * @param y       The y coordinate of the block.
     * @param z       The z coordinate of the block.
     * @param state   The state of the block.
     * @param chunk   The chunk to set the block in.
     * @param section The section to set the block in.
     */
    void set(int x, int y, int z, @NotNull BlockState state, @NotNull ChunkAccess chunk, @NotNull LevelChunkSection section);
}
