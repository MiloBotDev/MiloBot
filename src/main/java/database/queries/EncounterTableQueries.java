package database.queries;

public interface EncounterTableQueries {
	String creatEncounterTable = "CREATE TABLE IF NOT EXISTS encounter(userId varchar(255), partySize varchar(255), partyLevel varchar(255), difficulty varchar(255), description text, environment varchar(255), id INTEGER CONSTRAINT encounter_pk PRIMARY KEY AUTOINCREMENT);";
	String saveEncounter = "insert into encounter(userId, partySize, partyLevel, difficulty, description, environment) values(?, ?, ?, ?, ?, ?);";
}
