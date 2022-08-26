package database.queries;

public interface WordleTableQueries {
    String addUserWordle = "INSERT INTO wordle(userId, fastestTime, wonLastGame, streak, totalGames, highestStreak) VALUES(?, ?, ?, ?, ?, ?)";
    String updateUserWordle = "UPDATE wordle SET fastestTime = ?, wonLastGame = ?, streak = ?, totalGames = ?, highestStreak = ? WHERE userId = ?";
    String selectUserWordle = "SELECT * FROM wordle WHERE userId = ?";
    String wordleGetTopTotalGamesPlayed = "SELECT users.name, wordle.totalGames FROM users JOIN wordle ON users.userId = wordle.userId ORDER BY CAST(wordle.totalGames AS int) DESC LIMIT 100;";
    String wordleGetTopHighestStreak = "SELECT users.name, wordle.highestStreak FROM users JOIN wordle ON users.userId = wordle.userId ORDER BY CAST(wordle.highestStreak AS int) DESC LIMIT 100;";
    String wordleGetTopCurrentStreak = "SELECT users.name, wordle.streak FROM users JOIN wordle ON users.userId = wordle.userId ORDER BY CAST(wordle.streak AS int) DESC LIMIT 100;";
}
