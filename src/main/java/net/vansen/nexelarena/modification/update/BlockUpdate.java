package net.vansen.nexelarena.modification.update;

import net.minecraft.world.level.block.state.BlockState;

public class BlockUpdate {
    public final int x;
    public final int y;
    public final int z;
    public final BlockState state;

    public BlockUpdate(int x, int y, int z, BlockState state) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.state = state;
    }
}