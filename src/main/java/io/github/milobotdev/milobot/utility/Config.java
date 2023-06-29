package io.github.milobotdev.milobot.utility;

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
    private final String botSecret;
    private final String botClientId;
    private final String testGuildId;
    private final String loggingChannelName;
    //github
    private final String personalAccessToken;
    private final String repositoryName;
    // database
    private final String connectionUrl;
    private final String user;
    private final String password;
    // imgur api
    private final String imgurClientId;
    private final String imgurClientSecret;

    private final String apiRedirectUri;

    private final boolean botEnabled;
    private final boolean apiEnabled;

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
        this.botSecret = (String) data.get("secret");
        this.botClientId = (String) data.get("clientId");
        this.testGuildId = Long.toString(((Long) data.get("testGuildId")));
        this.loggingChannelName = (String) data.get("loggingChannelName");

        this.connectionUrl = (String) data.get("connectionUrl");
        this.user = (String) data.get("user");
        this.password = (String) data.get("password");

        this.defaultPrefix = (String) data.get("prefix");
        this.privateChannelPrefix = (String) data.get("privateChannelPrefix");
        this.personalAccessToken = (String) data.get("personalAccessToken");
        this.repositoryName = (String) data.get("repositoryName");

        this.imgurClientId = (String) data.get("imgurClientId");
        this.imgurClientSecret = (String) data.get("imgurClientSecret");

        this.apiRedirectUri = (String) data.get("apiRedirectUri");

        this.botEnabled = (boolean) data.get("botEnabled");
        this.apiEnabled = (boolean) data.get("apiEnabled");
    }

    /**
     * Returns the only existing instance of Config or creates a new one if no instance exists.
     */
    public static synchronized Config getInstance() {
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

    public String getBotSecret() {
        return botSecret;
    }

    public String getBotClientId() {
        return botClientId;
    }

    public String getTestGuildId() {
        return testGuildId;
    }

    public String getLoggingChannelName() {
        return loggingChannelName;
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

    public String getImgurClientId() {
        return imgurClientId;
    }

    public String getImgurClientSecret() {
        return imgurClientSecret;
    }

    public String apiRedirectUri() {
        return apiRedirectUri;
    }

    public boolean isBotEnabled() {
        return botEnabled;
    }

    public boolean isApiEnabled() {
        return apiEnabled;
    }
}
