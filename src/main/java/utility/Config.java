package utility;

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

    public static Config instance;

    public final String botToken;
    public final String testGuildId;
    public final String loggingChannelName;

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
    }

    /**
     * Returns the only existing instance of Config or creates a new one if no instance exists.
     * @return the Config instance
     * @throws FileNotFoundException thrown when the file can't be found in the constructor
     */
    public static Config getInstance() throws FileNotFoundException {
        if(instance == null) {
            instance = new Config();
        }
        return instance;
    }
}
