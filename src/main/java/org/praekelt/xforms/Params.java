package org.praekelt.xforms;


import com.google.gson.Gson;

/**
 *
 * @author victorgeere
 */
public class Params {
    public String xform;
    public String navMode;
    public String sessionData;
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
    public Params(String xform, String navMode, String sessionData, String apiAuth, int stalenessWindow) {
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
    
    public String toString() {
        return "{xform: \""+this.xform+"\", navMode: \""+
                this.navMode+"\", sessionData:\""+
                this.sessionData+"\", apiAuth:\""+
                this.apiAuth+"\",stalenessWindow: "+
                this.stalenessWindow+"}";
    }
    
}
