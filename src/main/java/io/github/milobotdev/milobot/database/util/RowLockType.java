package io.github.milobotdev.milobot.database.util;

/**
 * This enum represents the different types of locking reads that can be used in a SQL query.
 *
 * @see <a href="https://dev.mysql.com/doc/refman/5.7/en/innodb-locking-reads.html">MySQL docs on locking reads</a>
 */
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
