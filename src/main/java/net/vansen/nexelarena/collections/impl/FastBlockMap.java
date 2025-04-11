package net.vansen.nexelarena.collections.impl;

import net.minecraft.world.level.block.state.BlockState;
import net.vansen.nexelarena.collections.BlockMap;
import net.vansen.nexelarena.consumer.QuadConsumer;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Fast implementation of a block map, using 4 arrays to store the x, y, z coordinates and the block state.
 * <p>
 * Technically, compared to other implementations (not meant for block maps), this implementation is much faster, around 5-6x (or more) faster.
 * <p>
 * This implementation is not thread safe and should only be used in a single thread.
 */
@SuppressWarnings("unused")
public class FastBlockMap implements BlockMap {
    private static final int INITIAL_CAPACITY = 1024;

    private int[] xArray;
    private int[] yArray;
    private int[] zArray;
    private BlockState[] states;
    private int size = 0;

    /**
     * Creates a new FastBlockMap instance with the default initial capacity.
     */
    public FastBlockMap() {
        this.xArray = new int[INITIAL_CAPACITY];
        this.yArray = new int[INITIAL_CAPACITY];
        this.zArray = new int[INITIAL_CAPACITY];
        this.states = new BlockState[INITIAL_CAPACITY];
    }

    /**
     * Creates a new FastBlockMap instance with the given initial capacity.
     *
     * @param initialCapacity the initial capacity of the map
     */
    public FastBlockMap(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Initial capacity cannot be negative");
        }
        if (initialCapacity > 1_000_000_0) {
            initialCapacity = INITIAL_CAPACITY;
        }
        this.xArray = new int[initialCapacity];
        this.yArray = new int[initialCapacity];
        this.zArray = new int[initialCapacity];
        this.states = new BlockState[initialCapacity];
    }

    @Override
    public void put(int x, int y, int z, BlockState state) {
        if (size == xArray.length) {
            grow();
        }

        xArray[size] = x;
        yArray[size] = y;
        zArray[size] = z;
        states[size] = state;
        size++;
    }

    @Override
    public BlockState[] values() {
        return Arrays.copyOf(states, size);
    }

    @Override
    public BlockState[] valuesNonCopy() {
        return states;
    }

    @Override
    public void clear() {
        this.xArray = new int[INITIAL_CAPACITY];
        this.yArray = new int[INITIAL_CAPACITY];
        this.zArray = new int[INITIAL_CAPACITY];
        this.states = new BlockState[INITIAL_CAPACITY];
        size = 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void forEach(@NotNull QuadConsumer<Integer, Integer, Integer, BlockState> action) {
        for (int i = 0; i < size; i++) {
            action.accept(xArray[i], yArray[i], zArray[i], states[i]);
        }
    }

    private void grow() {
        int newCapacity = xArray.length * 2;

        int[] newX = new int[newCapacity];
        int[] newY = new int[newCapacity];
        int[] newZ = new int[newCapacity];
        BlockState[] newStates = new BlockState[newCapacity];

        System.arraycopy(xArray, 0, newX, 0, size);
        System.arraycopy(yArray, 0, newY, 0, size);
        System.arraycopy(zArray, 0, newZ, 0, size);
        System.arraycopy(states, 0, newStates, 0, size);

        xArray = newX;
        yArray = newY;
        zArray = newZ;
        states = newStates;
    }
}