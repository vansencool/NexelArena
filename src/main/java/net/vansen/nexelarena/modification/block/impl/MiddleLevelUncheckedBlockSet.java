package net.vansen.nexelarena.modification.block.impl;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.vansen.nexelarena.modification.block.BlockSet;
import org.jetbrains.annotations.NotNull;

public class MiddleLevelUncheckedBlockSet implements BlockSet {

    @Override
    public void set(int x, int y, int z, @NotNull BlockState state, @NotNull ChunkAccess chunk, @NotNull LevelChunkSection section) {
        section.setBlockState(x, y, z, state, false);
    }
}
