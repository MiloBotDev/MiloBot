package io.github.milobotdev.milobot.commands;

public interface SubCmd {
    default String getParentCmd() {
        return "";
    }
}
