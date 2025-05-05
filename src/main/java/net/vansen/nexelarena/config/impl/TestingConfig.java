package net.vansen.nexelarena.config.impl;

import net.vansen.fursconfig.FursConfig;
import net.vansen.nexelarena.config.Configuration;
import net.vansen.nexelarena.config.Variables;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class TestingConfig implements Configuration {

    @Override
    public void config(@NotNull FursConfig node) {
        Variables.ENABLE_BENCHMARK_COMMAND = node.getBoolean("enable_benchmark_command", false);
        Variables.ENABLE_BENCHMARK_BLOCKS_COMMAND = node.getBoolean("enable_benchmark_blocks_command", false);
    }

    @Override
    public String loadsFrom() {
        return "testing";
    }
}
