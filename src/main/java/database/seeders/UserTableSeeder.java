package database.seeders;

import database.DatabaseManager;

import java.util.Locale;
import java.util.Random;
import java.util.UUID;

class UserTableSeeder {

    /**
     * Fills the user table with randomly generated user data.
     */
    private static void fillUserTable() {
        DatabaseManager manager = DatabaseManager.getInstance();
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            String userId = UUID.randomUUID().toString().replaceAll("-", "").toLowerCase(Locale.ROOT);
            String currency = String.valueOf(random.nextInt(1000));
            String level = String.valueOf(random.nextInt(100));
            String experience = String.valueOf(random.nextInt(10000));
            manager.query(manager.addUser, DatabaseManager.QueryTypes.UPDATE, userId, currency, level, experience);
        }
    }

    public static void main(String[] args) {
        fillUserTable();
    }
}
