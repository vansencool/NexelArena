package net.vansen.nexelarena.config.impl;

import net.vansen.fursconfig.FursConfig;
import net.vansen.nexelarena.config.Configuration;
import net.vansen.nexelarena.config.Variables;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class PerformanceConfig implements Configuration {

    @Override
    public void config(@NotNull FursConfig node) {
        Variables.ADD_CHUNKS_TO_FORCE_LOAD = node.getBoolean("add_chunks_to_force_load", true);
        Variables.REFRESH_CHUNKS_ASYNC = node.getBoolean("refresh_chunks_async", false);
    }

    @Override
    public String loadsFrom() {
        return "performance";
    }
}
