package util;

import java.util.Properties;

/**
 *
 * @author Pablo
 */
public class Directory {

    private static Directory instance = null;
    private String connectionProperty = System.getProperty("user.dir") + "/connection.properties";

    public Directory() {
    }

    public static Directory getInstance() {
        if (instance == null) {
            instance = new Directory();
        }
        return instance;
    }

    public Properties loadConnectionDir() {
        Properties property = FileManager.loadPropriety(connectionProperty);
        if (property.isEmpty()) {
            // set default configuration
            property.setProperty("ip", "localhost");
            property.setProperty("dbname", "librol");
            property.setProperty("port", "3306");
            property.setProperty("username", "root");
            property.setProperty("password", "admin");
        }
        return property;
    }

    public void saveConnectionDir(Properties property) {
        FileManager.saveProperty(property, connectionProperty);
    }
}
