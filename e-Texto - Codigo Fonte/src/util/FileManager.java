package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author Pablo
 */
public class FileManager {

    public static String fileName;

    public FileManager() {
    }

    public static Properties loadPropriety(InputStream resourceAsStream) {
        Properties propertyDB = new Properties();
        try {
            propertyDB.load(resourceAsStream);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return propertyDB;
    }

    public static Properties loadPropriety(String dir) {

        Properties configSystem = new Properties();
        try {
            FileInputStream fis = new FileInputStream(dir);
            configSystem.load(fis);
            fis.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return configSystem;

    }

    public static void saveProperty(Properties property, String dir) {
        File file = new File(dir);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            //save data in file
            property.store(fos, dir);
            fos.close();
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
