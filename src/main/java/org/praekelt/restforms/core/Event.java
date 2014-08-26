package org.praekelt.restforms.core;

import java.util.ArrayList;
import org.javarosa.core.model.FormIndex;
import org.praekelt.xforms.KeyErrorException;

/**
 *
 * @author victorgeere
 */
public class Event {
    
    public static final int UNRECOGNISED = 0;
    public static final int INT = 1;
    public static final int FLOAT = 2;
    public static final int STR = 3;
    public static final int LONGINT = 4;
    public static final int SELECT = 5;
    public static final int MULTISELECT = 6;
    public static int GEO = 7;
    public static int DATE = 8;
    public static int TIME = 9;
    
    String type;
    FormIndex fi;
    public String caption;
    private String audioText;
    private String imageText;
    private boolean repeatable;
    private boolean exists;
    private ArrayList repetitions;
    private ArrayList<Event> children;
    private boolean relevant;
    private FormIndex ix;

    public Event(String subgroup, FormIndex descendIntoRepeat, Object get, boolean b, ArrayList arrayList) {
    }

    public Event() {
    }

    public ArrayList getRepetitions() {
        return repetitions;
    }

    public void setRepetitions(ArrayList repetitions) {
        this.repetitions = repetitions;
    }
    
    private int dataType;

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public FormIndex getFi() {
        return fi;
    }

    public void setFi(FormIndex fi) {
        this.fi = fi;
    }

    public void setCaptionAudio(String audioText) {
        this.audioText = audioText;
    }

    public void setCaptionImage(String imageText) {
        this.imageText = imageText;
    }

    public void setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
    }

    public boolean isRepeatable() {
        return this.repeatable;
    }
    
    public void setExists(boolean exists) {
        this.exists = exists;
    }
    
    public boolean isExists() {
        return this.exists;
    }

    public void setChildren(ArrayList arrayList) {
        this.children = arrayList;
    }

    public void setRelevant(boolean relevant) {
        this.relevant = relevant;
    }

    public FormIndex getIx() {
        return this.ix;
    }

    public ArrayList getChildren() {
        return this.children;
    }

    public void addChild(Event subevt) {
        children.add(subevt);
    }

    public void delete(String key) {
        //TODO: looks like children should be a Hashtable
        children.remove(key);
    }

    public FormIndex getByIx(String ix) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void setByIx(String ix, String obj) {
    
    }

    public void setByIx(String ix, Object obj) throws KeyErrorException {
    
    }

    
}
