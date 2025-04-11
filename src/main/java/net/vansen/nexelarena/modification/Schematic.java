package net.vansen.nexelarena.modification;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.vansen.nexelarena.collections.BlockMap;
import net.vansen.nexelarena.collections.impl.FastBlockMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class is used to save and load schematics, basically "regeneration" of blocks.
 */
@SuppressWarnings("unused")
public class Schematic {

    private static final net.vansen.nexelarena.modification.SchematicCache cache = new net.vansen.nexelarena.modification.SchematicCache();

    private final BlockMap blockMap;
    private int originX, originY, originZ;

    public Schematic() {
        this.blockMap = new FastBlockMap();
    }

    public Schematic(@NotNull BlockMap blockMap) {
        this.blockMap = blockMap;
    }

    public void saveRegion(@NotNull Player player, @NotNull World world, Location pos1, Location pos2, File file) {
        CompletableFuture.runAsync(() -> {
            player.sendRichMessage("<#8336ff>Getting blocks...");
            long start = System.nanoTime();
            originX = Math.min(pos1.getBlockX(), pos2.getBlockX());
            originY = Math.min(pos1.getBlockY(), pos2.getBlockY());
            originZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
            int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
            int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
            int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

            int chunkMinX = originX >> 4;
            int chunkMaxX = maxX >> 4;
            int chunkMinZ = originZ >> 4;
            int chunkMaxZ = maxZ >> 4;

            for (int chunkX = chunkMinX; chunkX <= chunkMaxX; chunkX++) {
                for (int chunkZ = chunkMinZ; chunkZ <= chunkMaxZ; chunkZ++) {
                    var chunk = ((CraftWorld) world).getHandle().getChunk(chunkX, chunkZ);
                    for (int x = Math.max(originX, chunkX << 4); x <= Math.min(maxX, (chunkX << 4) + 15); x++) {
                        for (int y = originY; y <= maxY; y++) {
                            for (int z = Math.max(originZ, chunkZ << 4); z <= Math.min(maxZ, (chunkZ << 4) + 15); z++) {
                                BlockState state = chunk.getBlockStateFinal(x, y, z);
                                blockMap.put(x - originX, y - originY, z - originZ, state);
                            }
                        }
                    }
                }
            }
            long end = System.nanoTime();
            player.sendRichMessage("<#8336ff>Done getting blocks (took: " + (end - start) / 1000000 + " ms), (found total: " + blockMap.size() + "), now saving...");
            long start2 = System.nanoTime();
            save(file).thenRun(() -> {
                long end2 = System.nanoTime();
                player.sendRichMessage("<#8336ff>Done saving schematic (took: " + (end2 - start2) / 1000000 + " ms), (found total: " + blockMap.size() + ")");
            });
        });
    }

    /**
     * Pastes the schematic to the given world.
     * <p>
     * Note, it is your responsibility to update the applied blocks.
     *
     * @param world The world to paste the schematic to.
     * @return A CompletableFuture that holds the NexelLevel instance.
     */
    public CompletableFuture<NexelLevel> paste(@NotNull World world) {
        NexelLevel level = new NexelLevel(world);
        return CompletableFuture.supplyAsync(() -> {
            blockMap.forEach((x, y, z, state) -> level.block(new BlockPos(originX + x, originY + y, originZ + z), state));
            return level;
        });
    }

    /**
     * Saves the schematic to the given file.
     *
     * @param file The file to save the schematic to.
     * @return A CompletableFuture.
     */
    public CompletableFuture<Void> save(@NotNull File file) {
        return CompletableFuture.runAsync(() -> {
            try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
                dos.writeInt(originX);
                dos.writeInt(originY);
                dos.writeInt(originZ);

                Map<BlockState, Integer> indexes = new HashMap<>();
                List<String> states = new ArrayList<>();
                int index = 1;
                for (BlockState state : blockMap.valuesNonCopy()) {
                    if (state == null || indexes.containsKey(state)) continue;
                    indexes.put(state, index++);
                    states.add(state.toString());
                }

                dos.writeInt(states.size());
                for (String s : states) {
                    dos.writeUTF(s);
                }

                AtomicInteger minX = new AtomicInteger(Integer.MAX_VALUE);
                AtomicInteger minY = new AtomicInteger(Integer.MAX_VALUE);
                AtomicInteger minZ = new AtomicInteger(Integer.MAX_VALUE);
                AtomicInteger maxX = new AtomicInteger(Integer.MIN_VALUE);
                AtomicInteger maxY = new AtomicInteger(Integer.MIN_VALUE);
                AtomicInteger maxZ = new AtomicInteger(Integer.MIN_VALUE);

                blockMap.forEach((x, y, z, state) -> {
                    minX.set(Math.min(minX.get(), x));
                    minY.set(Math.min(minY.get(), y));
                    minZ.set(Math.min(minZ.get(), z));
                    maxX.set(Math.max(maxX.get(), x));
                    maxY.set(Math.max(maxY.get(), y));
                    maxZ.set(Math.max(maxZ.get(), z));
                });

                dos.writeInt(minX.get());
                dos.writeInt(minY.get());
                dos.writeInt(minZ.get());
                dos.writeInt(maxX.get());
                dos.writeInt(maxY.get());
                dos.writeInt(maxZ.get());

                int sizeX = maxX.get() - minX.get() + 1;
                int sizeY = maxY.get() - minY.get() + 1;
                int sizeZ = maxZ.get() - minZ.get() + 1;
                int totalSize = sizeX * sizeY * sizeZ;

                int[] flatData = new int[totalSize];
                Arrays.fill(flatData, 0);

                blockMap.forEach((x, y, z, state) -> flatData[((x - minX.get()) * sizeY * sizeZ) + ((y - minY.get()) * sizeZ) + (z - minZ.get())] = state == null ? 0 : indexes.get(state));

                // ZRLE-like
                int i = 0;
                while (i < totalSize) {
                    int value = flatData[i];
                    int runLength = 1;

                    while (i + runLength < totalSize && flatData[i + runLength] == value && runLength < 0xFFFF) {
                        runLength++;
                    }

                    dos.writeInt(value);
                    dos.writeShort(runLength);
                    i += runLength;
                }

            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    /**
     * Loads a schematic from the given file.
     *
     * @param file The file to load the schematic from.
     * @return The loaded schematic.
     * @throws IOException If an I/O error occurs.
     */
    public static Schematic load(@NotNull File file) throws IOException {
        Schematic cached = cache.get(file);
        if (cached != null) return cached;

        Schematic schematic = new Schematic();
        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            schematic.originX = dis.readInt();
            schematic.originY = dis.readInt();
            schematic.originZ = dis.readInt();

            int count = dis.readInt();
            List<BlockState> states = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                String serialized = dis.readUTF();
                BlockState state = ((CraftBlockData) org.bukkit.Bukkit.createBlockData(serialized)).getState();
                states.add(state);
            }

            int minX = dis.readInt(), minY = dis.readInt(), minZ = dis.readInt();
            int maxX = dis.readInt(), maxY = dis.readInt(), maxZ = dis.readInt();

            int sizeX = maxX - minX + 1;
            int sizeY = maxY - minY + 1;
            int sizeZ = maxZ - minZ + 1;
            int totalSize = sizeX * sizeY * sizeZ;

            int[] flatData = new int[totalSize];
            int i = 0;
            while (i < totalSize) {
                int value = dis.readInt();
                int runLength = dis.readUnsignedShort();
                Arrays.fill(flatData, i, i + runLength, value);
                i += runLength;
            }

            schematic.blockMap.clear();

            for (int sx = 0; sx < sizeX; sx++) {
                for (int sy = 0; sy < sizeY; sy++) {
                    for (int sz = 0; sz < sizeZ; sz++) {
                        int index = (sx * sizeY * sizeZ) + (sy * sizeZ) + sz;
                        int stateIndex = flatData[index];
                        if (stateIndex == 0) continue;

                        BlockState state = states.get(stateIndex - 1);
                        schematic.blockMap.put(minX + sx, minY + sy, minZ + sz, state);
                    }
                }
            }
        }

        cache.put(file, schematic);
        return schematic;
    }
}
