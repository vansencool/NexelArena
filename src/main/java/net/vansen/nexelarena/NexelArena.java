package net.vansen.nexelarena;

import dev.vansen.commandutils.api.CommandAPI;
import net.vansen.fursconfig.FursConfig;
import net.vansen.nexelarena.commands.ArenaCommand;
import net.vansen.nexelarena.commands.benchmark.BenchmarkBlocksCommand;
import net.vansen.nexelarena.config.Variables;
import net.vansen.nexelarena.config.process.ConfigurationProcessor;
import net.vansen.nexelarena.logging.LogFilter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.file.Files;

/**
 * This is the main class of the NexelArena plugin.
 * <p>
 * Doesn't nessesarily do anything except provide the instance of the plugin.
 */
@SuppressWarnings("all")
public final class NexelArena extends JavaPlugin {
    private static NexelArena instance;

    @Override
    public void onEnable() {
        CommandAPI.set(this);
        instance = this;
        if (Files.notExists(new File(getDataFolder(), "config.conf").toPath())) {
            saveResource("config.conf", true);
        }
        ConfigurationProcessor.process(FursConfig.createAndParseFile(getDataFolder().toPath().resolve("config.conf")));
        ArenaCommand.register();
        if (Variables.ENABLE_BENCHMARK_BLOCKS_COMMAND) BenchmarkBlocksCommand.register();
        LogFilter.register();
    }

    @Override
    public void onDisable() {
        Bukkit.getWorlds()
                .forEach(world -> {
                    world.removePluginChunkTickets(this);
                });
    }

    /**
     * Returns the instance of the plugin.
     *
     * @return the instance of the plugin
     */
    public static NexelArena instance() {
        return instance;
    }
}
