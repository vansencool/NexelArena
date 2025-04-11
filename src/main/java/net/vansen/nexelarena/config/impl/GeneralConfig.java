package net.vansen.nexelarena.config.impl;

import net.vansen.fursconfig.FursConfig;
import net.vansen.nexelarena.config.Configuration;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class GeneralConfig implements Configuration {

    @Override
    public void config(@NotNull FursConfig node) {
        node.getBoolean("enable_log_filter", true);
    }

    @Override
    public String loadsFrom() {
        return "general";
    }
}
