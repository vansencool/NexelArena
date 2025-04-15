package net.vansen.nexelarena.modification;

import net.minecraft.world.level.block.state.BlockState;
import net.vansen.nexelarena.NexelArena;
import net.vansen.nexelarena.modification.update.BlockUpdate;
import net.vansen.nexelarena.modification.update.ChunkUpdates;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
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

    public static final String HEADER = "ZEAx9104817";
    private static final SchematicCache cache = new SchematicCache();

    private final List<ChunkUpdates> updates = new ArrayList<>();
    private int originX, originY, originZ;
    private World world;
    private NexelLevel level;

    public Schematic() {
    }

    public void saveRegion(@NotNull Player player, @NotNull World world, Location pos1, Location pos2, File file) {
        CompletableFuture.runAsync(() -> {
            this.world = world;
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
                    long chunkKey = Chunk.getChunkKey(chunkX, chunkZ);
                    ChunkUpdates chunkUpdates = new ChunkUpdates(chunkX, chunkZ);

                    for (int x = Math.max(originX, chunkX << 4); x <= Math.min(maxX, (chunkX << 4) + 15); x++) {
                        for (int y = originY; y <= maxY; y++) {
                            for (int z = Math.max(originZ, chunkZ << 4); z <= Math.min(maxZ, (chunkZ << 4) + 15); z++) {
                                BlockState state = chunk.getBlockStateFinal(x, y, z);
                                chunkUpdates.updates.add(new BlockUpdate(x, y, z, state));
                            }
                        }
                    }

                    if (!chunkUpdates.updates.isEmpty()) {
                        updates.add(chunkUpdates);
                        world.addPluginChunkTicket(chunkX, chunkZ, NexelArena.instance());
                    }
                }
            }

            long end = System.nanoTime();
            player.sendRichMessage("<#8336ff>Done getting blocks (took: " + (end - start) / 1000000 + " ms), (found total: " + updates.stream().mapToInt(u -> u.updates.size()).sum() + "), now saving...");
            long start2 = System.nanoTime();
            save(file).thenRun(() -> {
                long end2 = System.nanoTime();
                player.sendRichMessage("<#8336ff>Done saving schematic (took: " + (end2 - start2) / 1000000 + " ms), (found total: " + updates.stream().mapToInt(u -> u.updates.size()).sum() + ")");
            });
            this.level = new NexelLevel(world).clearAfterApply(false).updates(updates);
        });
    }

    /**
     * Pastes the schematic.
     * <p>
     * Note, it is your responsibility to update the applied blocks.
     *
     * @return A CompletableFuture that holds the NexelLevel instance.
     */
    public CompletableFuture<NexelLevel> paste() {
        return CompletableFuture.supplyAsync(() -> {
            level.updates(updates);
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
                dos.writeUTF(HEADER);

                dos.writeUTF(world.getName());
                dos.writeInt(originX);
                dos.writeInt(originY);
                dos.writeInt(originZ);

                Map<BlockState, Integer> indexes = new HashMap<>();
                List<String> states = new ArrayList<>();
                int index = 1;

                for (ChunkUpdates chunkUpdates : updates) {
                    for (BlockUpdate update : chunkUpdates.updates) {
                        if (update.state == null || indexes.containsKey(update.state)) continue;
                        indexes.put(update.state, index++);
                        states.add(update.state.toString());
                    }
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

                for (ChunkUpdates chunkUpdates : updates) {
                    for (BlockUpdate update : chunkUpdates.updates) {
                        minX.set(Math.min(minX.get(), update.x - originX));
                        minY.set(Math.min(minY.get(), update.y - originY));
                        minZ.set(Math.min(minZ.get(), update.z - originZ));
                        maxX.set(Math.max(maxX.get(), update.x - originX));
                        maxY.set(Math.max(maxY.get(), update.y - originY));
                        maxZ.set(Math.max(maxZ.get(), update.z - originZ));
                    }
                }

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

                for (ChunkUpdates chunkUpdates : updates) {
                    for (BlockUpdate update : chunkUpdates.updates) {
                        int flatIndex = ((update.x - originX - minX.get()) * sizeY * sizeZ) +
                                ((update.y - originY - minY.get()) * sizeZ) +
                                (update.z - originZ - minZ.get());
                        flatData[flatIndex] = update.state == null ? 0 : indexes.get(update.state);
                    }
                }

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
            String header;
            try {
                header = dis.readUTF();
            } catch (Exception e) {
                throw new IOException("Invalid schematic file: " + file.getAbsolutePath());
            }
            if (!header.equals(HEADER)) {
                throw new IOException("Invalid schematic file: " + file.getAbsolutePath());
            }

            String worldName = dis.readUTF();
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                throw new IOException("World not found: " + worldName);
            }
            schematic.world = world;
            schematic.originX = dis.readInt();
            schematic.originY = dis.readInt();
            schematic.originZ = dis.readInt();

            int count = dis.readInt();
            List<BlockState> states = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                String serialized = dis.readUTF();
                BlockState state = ((CraftBlockData) Bukkit.createBlockData(serialized)).getState();
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

            Map<Long, ChunkUpdates> chunkMap = new HashMap<>();

            for (int sx = 0; sx < sizeX; sx++) {
                for (int sy = 0; sy < sizeY; sy++) {
                    for (int sz = 0; sz < sizeZ; sz++) {
                        int index = (sx * sizeY * sizeZ) + (sy * sizeZ) + sz;
                        int stateIndex = flatData[index];
                        if (stateIndex == 0) continue;

                        int x = minX + sx + schematic.originX;
                        int y = minY + sy + schematic.originY;
                        int z = minZ + sz + schematic.originZ;
                        int chunkX = x >> 4;
                        int chunkZ = z >> 4;
                        long chunkKey = Chunk.getChunkKey(chunkX, chunkZ);

                        ChunkUpdates chunkUpdates = chunkMap.computeIfAbsent(chunkKey, k -> new ChunkUpdates(chunkX, chunkZ));
                        chunkUpdates.updates.add(new BlockUpdate(x, y, z, states.get(stateIndex - 1)));
                    }
                }
            }

            schematic.updates.addAll(chunkMap.values());
        }

        for (ChunkUpdates chunkUpdates : schematic.updates) {
            schematic.world.addPluginChunkTicket(chunkUpdates.chunkX, chunkUpdates.chunkZ, NexelArena.instance());
        }

        schematic.level = new NexelLevel(schematic.world).clearAfterApply(false).updates(schematic.updates);
        cache.put(file, schematic);
        return schematic;
    }

    /**
     * The nexel level of the schematic, you can directly use apply blocks on it.
     *
     * @return The nexel level of the schematic.
     */
    public NexelLevel asLevel() {
        return level;
    }
}
