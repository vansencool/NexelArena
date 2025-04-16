package net.vansen.nexelarena.config.impl;

import net.vansen.fursconfig.FursConfig;
import net.vansen.nexelarena.config.Configuration;
import net.vansen.nexelarena.config.Variables;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class GeneralConfig implements Configuration {

    @Override
    public void config(@NotNull FursConfig node) {
        Variables.ENABLE_LOG_FILTER = node.getBoolean("enable_log_filter", true);
        Variables.ENABLE_SCHEMATIC_CACHE = node.getBoolean("enable_schematic_cache", true);
    }

    @Override
    public String loadsFrom() {
        return "general";
    }
}
