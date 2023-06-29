package io.github.milobotdev.milobot.database.model;

public class CommandTracker {
    private String command;
    private int count;

    public CommandTracker(String command, int count) {
        this.command = command;
        this.count = count;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
