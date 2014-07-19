package org.praekelt.tools;

import java.util.HashMap;

/**
 *
 * @author victorgeere
 */
public class XFormSession {
    Object uid;
    String curEvent;
    private Object lang;

    XFormSession(Object xformXml, Object instanceXml, HashMap kwargs) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    String descendRepeat(int ix) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    String response(HashMap extra) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Object response(Object object, String ev) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Object response(Object object, boolean ev) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    String deleteRepeat(int repIx, int formIx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    void newRepetition() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    String setLocale(String lang) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Object getLang() {
        return this.lang;
    }

    /**
     * Can't call finalize explicitly
    */
    void fin() {
        
    }

    Object output() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
