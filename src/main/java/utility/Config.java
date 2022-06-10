package utility;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class Config {

    public static Config instance;
    public final String botToken ;

    private Config() throws FileNotFoundException {
        InputStream inputStream = new FileInputStream("config.yml");

        Yaml yaml = new Yaml();
        HashMap<String, Object> data = yaml.load(inputStream);

        botToken = (String) data.get("token");
    }

    public static Config getInstance() throws FileNotFoundException {
        if(instance == null) {
            instance = new Config();
        }
        return instance;
    }
}
