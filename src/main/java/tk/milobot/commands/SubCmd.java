package tk.milobot.commands;

public interface SubCmd {
    default String getParentCmd() {
        return "";
    }
}
