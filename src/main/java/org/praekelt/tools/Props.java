package org.praekelt.tools;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Victor
 */
public class Props {

    /**
     * Return the value from a property file
     *
     * @param key
     * @return
     */
    public String get(String key) {
        Properties prop = new Properties();
        String value = "";
        try {
            prop.load(getClass().getResourceAsStream("/config.properties"));
            value = prop.getProperty(key);
        } catch (IOException ex) {
            Logger.getLogger(Props.class.getName()).log(Level.SEVERE, null, ex);
        }
        return value;
    }

    /**
     * Return the value of a property as an integer
     *
     * @param key
     * @return
     */
    public static Integer getInt(String key) {
        Props p = new Props();
        return Integer.parseInt(p.get(key));
    }
}
