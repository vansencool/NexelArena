package net.vansen.nexelarena.modification.block.impl;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.vansen.nexelarena.NexelArena;
import net.vansen.nexelarena.modification.block.BlockSet;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class HighLevelBlockSet implements BlockSet {

    @Override
    public void set(int x, int y, int z, @NotNull BlockState state, @NotNull ChunkAccess chunk, @NotNull LevelChunkSection section) {
        try {
            chunk.setBlockState(new BlockPos(x, y, z), state, false);
        } catch (Exception e) {
            NexelArena.instance()
                    .getSLF4JLogger()
                    .error("Failed to set block at {} {} {}, if you think this is a bug, please report it at https://github.com/vansencool/NexelArena", x, y, z, e);
        }
    }
}
