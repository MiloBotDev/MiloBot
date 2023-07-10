# Database Access

This guide provides instructions on how to store data in the MySQL database used by MiloBot.

## Daos and Models

In MiloBot, Daos (Database Access Objects) represent tables in the database, while Models represent rows in those tables. It is generally recommended to have a model for each dao, although for simpler tables, it is not necessary.

It is also strongly recommended to include an auto-incremented `id` field in each table. This allows the `update` and `delete` methods in the dao to easily identify the row associated with the model.

## Creating a Dao

Daos are implemented as singleton classes, meaning that the constructor is private, and an instance of the dao is obtained using the static `getInstance()` method.

A typical dao includes the following methods:

- `private createTableIfNotExists()` - Creates the table in the database if it doesn't already exist.
- `add(Connection con, T model)` - Inserts a row into the table.
- `update(Connection con, T model)` - Updates a row in the table.
- `delete(Connection con, T model)` - Deletes a row from the table.
- `getByFieldXXX(Connection con, T model)` - Retrieves a row from the table based on a field in the model.

In the above methods, `con` represents a connection to the database, which can be obtained using the `getConnection()` method from the `DatabaseConnection` class.

The parameter `model` represents the model associated with the dao.

It is also recommended for each dao to have a logger to record errors. You can acquire the logger using the `getLogger()` method from slf4j's `LoggerFactory` class.

## Creating a Model

A model typically consists of a class that holds data along with corresponding getters and setters. Feel free to include additional constructors and utility methods as needed.