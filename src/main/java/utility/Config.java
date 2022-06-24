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
 * @author Ruben Eekhof - rubeneekhof@gmail.com
 */
public class Config {

    final static Logger logger = LoggerFactory.getLogger(Config.class);
    public static Config instance;

    public final String botToken;
    public final String testGuildId;
    public final String loggingChannelName;
    public final String levelsJsonPath;
    public final String connectionUrl;
    public final String wordleWordsPath;
    public final String defaultPrefix;

    /**
     * Instantiates all the
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
        this.connectionUrl = (String) data.get("connectionUrl");
        this.wordleWordsPath = (String) data.get("wordleWordsPath");
        this.defaultPrefix = (String) data.get("prefix");
    }

    /**
     * Returns the only existing instance of Config or creates a new one if no instance exists.
     * @return Config instance
     */
    public static Config getInstance() {
        try{
            if(instance == null) {
                instance = new Config();
                logger.info("Config file loaded in.");
            }
            return instance;
        } catch (FileNotFoundException e) {
            logger.info("Config file not found.");
            logger.error(e.getMessage());
            throw new IllegalStateException("Config file not found.");
        }
    }
}
