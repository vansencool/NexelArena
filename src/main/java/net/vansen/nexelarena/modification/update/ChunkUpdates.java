package net.vansen.nexelarena.modification.update;

import java.util.ArrayList;
import java.util.List;

public class ChunkUpdates {
    public final List<BlockUpdate> updates = new ArrayList<>();
    public final int chunkX;
    public final int chunkZ;

    public ChunkUpdates(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }
}