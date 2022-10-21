package commands;

public interface SubCmd {
    default String getParentCmd() {
        return "";
    }
}
