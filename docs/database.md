# MiloBot database

MiloBot uses a MySQL/MariaDB database.

We have adopted a nice database model for the bot. This model allows database access to be programmer-friendly with relatively little loss in performance. It also enables us to perform locking reads a nice way.

## Database model

The database model is as follows:
- Each table has its own model and dao (Database Access Object). The model is used for representing a row in the table and the dao is used for accessing the table. When a dao returns a query result, it returns an instance of the corresponding table model. (For some tables that are very simple, the dao returns the values in the table directly without wrapping it in a model, but this is not the case for most tables.)
- A dao can have many methods, but these are the most notable ones. More about the parameters later.
    - add(Connection con, Model model) - Adds a row to the table with the data in the model.
    - update(Connection con, Model model) - Updates a row in the table with the data in the model. The row is identified by the primary key in the model, which is most likely an auto increment 32-bit integer.
    - delete(Connection con, Model model) - Deletes a row in the database. The row is identified by the primary key in the model, which is most likely an auto increment 32-bit integer.
    - getXXXbyYYY(Connection con, YYY yyy, RowLockType lockType) - Retrieves a row from the table identified by some YYYY identifier (often the primary key).
- Most of the time a model is just a class with a series of private fields with getters and setters. However, the model often does more advanced things. For example, the method `addGame(BlackjackResult won/draw/lost, int moneyEarned)` in blackjackGame model takes only two parameters and adds a game played to the user's row in the blackjackGame table, which isn't a simple one-line operation. It has to set all necessary fields: total games played, lost, won, draw, money earned, and streak.

## Connection pooling

We use Apache DBCP for database connection pooling. This means that we have a pool of connections to the database that we can use. This is much more efficient than creating a new connection manually every time we need to access the database, because having a pool means the connection is already open and allows us to acquire the connection almost instantly, while creating a new connection manually requires the program to wait until the connection is made.

This is how to acquire a new connection:

`Connection con = NewDatabaseConnection.getConnection()` [NOTE: subject to change to `DatabaseConnection` after the database refactor.]

## Locking reads

MySQL provides two types of locking reads for `SELECT` statements:

- `SELECT ... LOCK IN SHARE MODE`

  Sets a shared mode lock on any rows that are read. Other sessions can read the rows, but cannot modify them until your transaction commits. If any of these rows were changed by another transaction that has not yet committed, your query waits until that transaction ends and then uses the latest values.

- `SELECT ... FOR UPDATE`

  For index records the search encounters, locks the rows and any associated index entries, the same as if you issued an UPDATE statement for those rows. Other transactions are blocked from updating those rows, from doing SELECT ... FOR SHARE, or from reading the data in certain transaction isolation levels. Consistent reads ignore any locks set on the records that exist in the read view. (Old versions of a record cannot be locked; they are reconstructed by applying undo logs on an in-memory copy of the record.)

Source: [MySQL docs for locking reads](https://dev.mysql.com/doc/refman/8.0/en/innodb-locking-reads.html)

To use this in MiloBot, we have to pass the RowLockType enum to every method in the DAOs that make a `SELECT` query. All DAO methods that make `SELECT` queries have a RowLockType parameter. The RowLockType enum has three values:
- NONE - No locking read is used.
- SHARE - `SELECT ... LOCK IN SHARE MODE` is used.
- UPDATE - `SELECT ... FOR UPDATE` is used.

For locking reads it is important to understand what autocommit mode is. Autocommit is enabled by default and most of the time you'll do queries in this mode. Autocommit means that every query is automatically committed after it is run i.e. it is updated in the database and other connections see the changes immediately after the query completes. With autocommit disabled, changes only take place once you commit or rollback. Commit means you make the changes permanent and update the changes in the database and rollback means you cancel the changes.

Source: [MySQL docs for autocommit](https://dev.mysql.com/doc/refman/8.0/en/commit.html)

 When using locking reads, make sure to disable autocommit. If you don't disable autocommit, the locking reads won't have any effect. You can disable autocommit by calling `con.setAutoCommit(false)`. Then, when you do locking reads the rows will be locked until you commit or rollback. **Always commit or rollback when you disable autocommit**, even if you didn't do any changes. If you don't, the rows could potentially remain locked. Most of the time you'd want to commit, you use usually rollback when an error has been encountered.

## Locking reads in practice

### Accessing fields concurrently

Often, we increment the value of a column of a row of a table. Consider this as an example: A user's money is stored in column `money` and two threads increment the user's money at the same time. Let's say the initial values is 1000 and Thread A wants to increment it by 200 and Thread B wants to increment it by 300. That means we expect the user's money to be 1500 after both thread's operations complete.

Now, consider this case:
- Thread A reads the value of the column `money` and gets 1000.
- Thread B reads the value of the column `money` and gets 1000.
- Thread A increments the value by 200 and gets 1200.
- Thread B increments the value by 300 and gets 1300.
- Thread A commits the changes and writes 1200 as the user's money.
- Thread B commits the changes and writes 1300 as the user's money.

Hence, the end result is 1300, which is wrong. This is a perfect example of how multiple threads operating on the same table can cause problems. Thankfully, locking reads got our back and solve this problem. Let's see how.

The programmer fixes the code of Thread A and Thread B. He adds `SELECT ... FOR UPDATE` locking reads to the queries that read the value of the column `money` and releases the locks (by committing) after they write the updated value. This way, no issue as in the example can occur. Whichever thread reads the value first locks the row and the other thread has to wait to read it until the first threads commits and writes and updated value to the database.

### Adding a row if it does not exist

Another example is adding a row to a table if it does not exist. Let's say that a table holds some kind of entry for each user as its rows. Two threads each want to make sure that a user has a row in the table. They check if a row exists for a user and if not they'll add it. If they do not use locking reads, there is a similar situation as in the previous example. They read the table at the same time and detect that a row does not exist and both add a row for the user. This can either cause an SQL exception if constraints are set up correctly or result in multiple rows for the same user.

Now, it's important not to only implement locking reads for this situation when you know there will be multiple threads. You don't know what features will be added in the future that will introduce multiple threads accessing the same table, so it's better to implement it at the time you're writing the code rather than causing a headache and extra work when adding new features that introduce multiple threads. It's really not hard to add locking reads, so there's no reason not adding it for a little extra protection.

## A word about closing resources

It is very important to close all types of JDBC resources, that means not only Connections, but also PreparedStatements, Statements, ResultSets, and all other JDBC resources. The best way to implement this is try-with-resources. In fact, I think you should always use try-with-resources if you don't have a specific reason against it. It is also important to remember that for try-with-resources it is perfectly fine to have try blocks with no catches.

If you don't know how to use try-with-resources, [here](https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html) is a great resource for it from Oracle Java Docs.