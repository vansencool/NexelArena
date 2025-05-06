package net.vansen.nexelarena.modification.block.impl;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.vansen.nexelarena.modification.block.BlockSet;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class HighLevelBlockSet implements BlockSet {

    @Override
    public void set(int x, int y, int z, @NotNull BlockState state, @NotNull ChunkAccess chunk, @NotNull LevelChunkSection section) {
        chunk.setBlockState(new BlockPos(x, y, z), state, false);
    }
}
