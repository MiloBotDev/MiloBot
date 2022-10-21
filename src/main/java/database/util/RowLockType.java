package database.util;

public enum RowLockType {

    NONE,
    FOR_SHARE,
    FOR_UPDATE;

    public String getQueryWithLock(String query) {
        return switch (this) {
            case FOR_SHARE -> query + " LOCK IN SHARE MODE";
            case FOR_UPDATE -> query + " FOR UPDATE";
            default -> query;
        };
    }
}
