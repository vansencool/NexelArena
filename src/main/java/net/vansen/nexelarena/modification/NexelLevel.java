package net.vansen.nexelarena.modification;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.vansen.nexelarena.NexelArena;
import net.vansen.nexelarena.config.Variables;
import net.vansen.nexelarena.modification.block.BlockSet;
import net.vansen.nexelarena.modification.block.impl.MiddleLevelUncheckedBlockSet;
import net.vansen.nexelarena.modification.block.impl.UnsafeBlockSet;
import net.vansen.nexelarena.modification.update.BlockUpdate;
import net.vansen.nexelarena.modification.update.ChunkUpdates;
import net.vansen.nexelarena.modification.update.SectionUpdate;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * This class is used to batch chunk (blocks) updates in a very fast, async way.
 */
@SuppressWarnings("unused")
public class NexelLevel {

    private List<ChunkUpdates> updates = new ArrayList<>();
    private Consumer<Integer> callback = ignored -> {
        // do nothing
    };
    private Consumer<Integer> blockCallback = ignored -> {
        // do nothing
    };
    private final World world;
    private boolean clearAfterApply = true;
    private final Object lock = new Object();
    private BlockSet set = Variables.SET_BLOCKS_UNSAFE ? new UnsafeBlockSet() : new MiddleLevelUncheckedBlockSet();

    /**
     * Creates a new NexelLevel instance.
     *
     * @param world The world to use.
     */
    public NexelLevel(@NotNull World world) {
        this.world = world;
    }

    /**
     * Creates a new NexelLevel instance.
     *
     * @param world The world to use.
     * @param set   The block set to use.
     */
    public NexelLevel(@NotNull World world, @NotNull BlockSet set) {
        this.world = world;
        this.set = set;
    }

    /**
     * Applies the pending block updates to the world, it is async and will not block the main thread.
     *
     * @return The number of blocks that are going to get updated.
     */
    @SuppressWarnings("all")
    public int applyPendingBlockUpdates() {
        int total = 0;
        for (ChunkUpdates chunkUpdates : updates) {
            total += chunkUpdates.getBlockUpdateCount();
        }
        final int totalUpdates = total;
        if (totalUpdates == 0) {
            return 0;
        }

        CompletableFuture.runAsync(() -> {
            synchronized (lock) {
                for (ChunkUpdates chunkUpdates : updates) {
                    if (Variables.ADD_CHUNKS_TO_FORCE_LOAD)
                        world.addPluginChunkTicket(chunkUpdates.chunkX, chunkUpdates.chunkZ, NexelArena.instance());
                    ChunkAccess chunk = ((CraftWorld) world).getHandle().getChunk(chunkUpdates.chunkX, chunkUpdates.chunkZ);
                    for (SectionUpdate sectionUpdate : chunkUpdates.getSectionUpdates()) {
                        int sectionIndex = sectionUpdate.sectionIndex;
                        if (sectionIndex < 0) {
                            NexelArena.instance()
                                    .getSLF4JLogger()
                                    .warn("Section index is negative: {} - in chunk: {}, {} - if you are think this is a bug, please report it at https://github.com/vansencool/NexelArena", sectionUpdate.sectionIndex, chunkUpdates.chunkX, chunkUpdates.chunkZ);
                            continue;
                        }

                        LevelChunkSection section = chunk.getSection(sectionIndex);

                        for (BlockUpdate update : sectionUpdate.updates) {
                            set.set(update.x & 15, update.y & 15, update.z & 15, update.state, chunk, section);
                        }
                    }
                }

                blockCallback.accept(totalUpdates);
                if (Variables.REFRESH_CHUNKS_ASYNC) afterwards(totalUpdates);
                else
                    CompletableFuture.runAsync(() -> afterwards(totalUpdates), Bukkit.getScheduler().getMainThreadExecutor(NexelArena.instance()));
            }
        });

        return totalUpdates;
    }

    private void afterwards(int totalUpdates) {
        synchronized (lock) {
            for (ChunkUpdates chunkUpdates : updates) {
                world.refreshChunk(chunkUpdates.chunkX, chunkUpdates.chunkZ);
                if (clearAfterApply && Variables.ADD_CHUNKS_TO_FORCE_LOAD)
                    world.removePluginChunkTicket(chunkUpdates.chunkX, chunkUpdates.chunkZ, NexelArena.instance());
            }

            List<ChunkPos> chunkPositions = new ArrayList<>();
            for (ChunkUpdates chunkUpdates : updates) {
                chunkPositions.add(new ChunkPos(chunkUpdates.chunkX, chunkUpdates.chunkZ));
            }
            ((CraftWorld) world).getHandle().getLightEngine().starlight$serverRelightChunks(chunkPositions, null, null);
            if (clearAfterApply) updates.clear();
            if (callback != null) callback.accept(totalUpdates);
        }
    }

    /**
     * Sets the block set to use.
     *
     * @param set The block set to use.
     * @return The current instance of NexelLevel.
     */
    public NexelLevel set(@NotNull BlockSet set) {
        this.set = set;
        return this;
    }

    /**
     * Sets a callback to be called after refreshing chunks.
     * <p>
     * Note, this callback might be called on a different thread, or the main thread, depending on the config.
     *
     * @param callback The callback to be called when the block updates are applied.
     */
    @SuppressWarnings("all")
    public NexelLevel callback(@NotNull Consumer<Integer> callback) {
        this.callback = callback;
        return this;
    }

    /**
     * Calls the callback when all block updates are applied.
     *
     * @param callback The callback to be called for each block update.
     */
    @SuppressWarnings("all")
    public NexelLevel blockCallback(@NotNull Consumer<Integer> callback) {
        this.blockCallback = callback;
        return this;
    }

    /**
     * Sets the block updates to be applied.
     *
     * @param updates The block updates to be applied.
     * @return The current instance of NexelLevel.
     */
    public NexelLevel updates(@NotNull List<ChunkUpdates> updates) {
        this.updates = updates;
        return this;
    }

    /**
     * If true, the chunk updates will be cleared after applying the block updates.
     *
     * @param clearAfterApply If true, the chunk updates will be cleared after applying the block updates.
     * @return The current instance of NexelLevel.
     */
    public NexelLevel clearAfterApply(boolean clearAfterApply) {
        this.clearAfterApply = clearAfterApply;
        return this;
    }
}
