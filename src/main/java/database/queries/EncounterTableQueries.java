package database.queries;

public interface EncounterTableQueries {
    String saveEncounter = "insert into encounter(userId, partySize, partyLevel, difficulty, description, environment) values(?, ?, ?, ?, ?, ?);";
}
