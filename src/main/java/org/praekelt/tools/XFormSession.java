package org.praekelt.tools;

import java.util.HashMap;

/**
 *
 * @author victorgeere
 */
public class XFormSession {
    public Object uid;
    public String curEvent;
    private Object lang;

    public XFormSession(Object xformXml, Object instanceXml, HashMap kwargs) {
    }

    public String descendRepeat(int ix) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String response(HashMap extra) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Object response(Object object, String ev) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Object response(Object object, boolean ev) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String deleteRepeat(int repIx, int formIx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void newRepetition() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String setLocale(String lang) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Object getLang() {
        return this.lang;
    }

    /**
     * Can't call finalize explicitly
    */
    public void fin() {
        
    }

    public Object output() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
