package net.vansen.nexelarena.collections;

import net.minecraft.world.level.block.state.BlockState;
import net.vansen.nexelarena.consumer.QuadConsumer;
import org.jetbrains.annotations.NotNull;

/**
 * This interface represents a map of blocks at x-y-z coordinates.
 */
@SuppressWarnings("unused")
public interface BlockMap {

    /**
     * Puts a block state at the given x, y, z coordinates.
     *
     * @param x     the x coordinate
     * @param y     the y coordinate
     * @param z     the z coordinate
     * @param state the block state to put
     */
    void put(int x, int y, int z, BlockState state);

    /**
     * Makes a entirely new map (new arrays internally).
     * THIS DOES NOT FILL THE MAP WITH NULL!
     */
    void clear();

    /**
     * Returns the values of the map.
     *
     * @return the values of the map
     */
    BlockState[] values();

    /**
     * Returns the internal array of block states without copying it.
     * <p>
     * This method is not recommended to use, as it can lead to unexpected behavior if the array is modified.
     *
     * @return the internal array of block states
     */
    BlockState[] valuesNonCopy();

    /**
     * The size of the map.
     *
     * @return the size of the map
     */
    int size();

    /**
     * Applies the given action to each entry in the map.
     *
     * @param action the action to apply
     */
    void forEach(@NotNull QuadConsumer<Integer, Integer, Integer, BlockState> action);
}
