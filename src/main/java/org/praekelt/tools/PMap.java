package org.praekelt.tools;

import org.javarosa.core.util.Map;

/**
 *
 * @author victorgeere
 */
public class PMap extends Map {
    public PMap() {
    
    }
    
    /**
     * Chainable factory
     * 
     * @param key
     * @param value
     * @return 
     */
    public static PMap make (String key, String value) {
        PMap pm = new PMap();
        pm.add(key, value);
        return pm;
    }
    
    /**
     * Chainable put
     * 
     * @param key
     * @param value
     * @return 
     */
    public PMap add (String key, String value) {
        this.put(key, value);
        return this;
    }
}
