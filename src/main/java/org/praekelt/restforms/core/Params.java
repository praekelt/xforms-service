package org.praekelt.restforms.core;


import com.google.gson.Gson;
import java.util.HashMap;
import org.javarosa.core.api.State;

/**
 *
 * @author victorgeere
 */
public class Params implements State {
    public String xform;
    public String navMode;
    public HashMap sessionData;
    public String apiAuth;
    public int stalenessWindow;

    /**
     * 
     * @param xform
     * @param navMode
     * @param sessionData
     * @param apiAuth
     * @param stalenessWindow 
     */
    public Params(String xform, String navMode, HashMap sessionData, String apiAuth, int stalenessWindow) {
        this.xform = xform;
        this.navMode = navMode;
        this.sessionData = sessionData;
        this.apiAuth = apiAuth;
        this.stalenessWindow = stalenessWindow;
    }

    public Params() {
    }
    
    /**
     * Instantiate a Params object from json
     * 
     * @param json
     * @return 
     */
    public static Params getInstance(String json) {
        Gson gson = new Gson();
        Params p = gson.fromJson(json, Params.class);
        return p;
    }
    
    public static void update() {
    
    }
    
    public String toString() {
        return "{xform: \""+this.xform+"\", navMode: \""+
                this.navMode+"\", sessionData:\""+
                this.sessionData+"\", apiAuth:\""+
                this.apiAuth+"\",stalenessWindow: "+
                this.stalenessWindow+"}";
    }

    @Override
    public void start() {
    }

    public void update(String output, String lang, String string, int seqId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
