package utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Holds the configuration data for the bot.
 * This class is a singleton.
 */
public class Config {

    final static Logger logger = LoggerFactory.getLogger(Config.class);
    public static Config instance;

    // bot
    private final String defaultPrefix;
    private final String privateChannelPrefix;
    private final String botToken;
    private final String testGuildId;
    private final String loggingChannelName;
    //github
    private final String personalAccessToken;
    private final String repositoryName;
    // paths
    private final String levelsJsonPath;
    private final String hungerGamesPath;
    private final String wordleWordsPath;
    private final String monstersCsvPath;
    // database
    private final String connectionUrl;
    private final String user;
    private final String password;

    /**
     * Instantiates all the configuration fields.
     *
     * @throws FileNotFoundException thrown when the file can't be found
     */
    private Config() throws FileNotFoundException {
        InputStream inputStream = new FileInputStream("config.yml");

        Yaml yaml = new Yaml();
        HashMap<String, Object> data = yaml.load(inputStream);

        this.botToken = (String) data.get("token");
        this.testGuildId = Long.toString(((Long) data.get("testGuildId")));
        this.loggingChannelName = (String) data.get("loggingChannelName");
        this.levelsJsonPath = (String) data.get("levelsJsonPath");
        this.hungerGamesPath = (String) data.get("hungerGamesPath");

        this.connectionUrl = (String) data.get("connectionUrl");
        this.user = (String) data.get("user");
        this.password = (String) data.get("password");

        this.wordleWordsPath = (String) data.get("wordleWordsPath");
        this.defaultPrefix = (String) data.get("prefix");
        this.privateChannelPrefix = (String) data.get("privateChannelPrefix");
        this.personalAccessToken = (String) data.get("personalAccessToken");
        this.repositoryName = (String) data.get("repositoryName");
        this.monstersCsvPath = (String) data.get("monstersCsvPath");


    }

    /**
     * Returns the only existing instance of Config or creates a new one if no instance exists.
     */
    public static Config getInstance() {
        try {
            if (instance == null) {
                instance = new Config();
                logger.debug("config.yml file loaded in.");
            }
            return instance;
        } catch (FileNotFoundException e) {
            logger.error("config.yml file not found.", e);
            throw new IllegalStateException("config.yml file not found.");
        }
    }

    public String getBotToken() {
        return botToken;
    }

    public String getTestGuildId() {
        return testGuildId;
    }

    public String getLoggingChannelName() {
        return loggingChannelName;
    }

    public String getLevelsJsonPath() {
        return levelsJsonPath;
    }

    public String getConnectionUrl() {
        return connectionUrl;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getWordleWordsPath() {
        return wordleWordsPath;
    }

    public String getHungerGamesPath() {
        return hungerGamesPath;
    }

    public String getDefaultPrefix() {
        return defaultPrefix;
    }

    public String getPrivateChannelPrefix() {
        return privateChannelPrefix;
    }

    public String getPersonalAccessToken() {
        return personalAccessToken;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public String getMonstersCsvPath() {
        return monstersCsvPath;
    }
}
