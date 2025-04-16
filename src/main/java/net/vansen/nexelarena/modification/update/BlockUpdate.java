package net.vansen.nexelarena.modification.update;

import net.minecraft.world.level.block.state.BlockState;

/**
 * Represents a block update.
 */
public class BlockUpdate {
    public final int x;
    public final int y;
    public final int z;
    public final BlockState state;

    /**
     * Creates a new BlockUpdate instance.
     *
     * @param x     The x coordinate of the block.
     * @param y     The y coordinate of the block.
     * @param z     The z coordinate of the block.
     * @param state The state of the block.
     */
    public BlockUpdate(int x, int y, int z, BlockState state) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.state = state;
    }
}