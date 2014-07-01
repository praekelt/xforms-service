package org.praekelt.xforms;

import org.javarosa.core.model.FormIndex;

/**
 *
 * @author victorgeere
 */
public class Event {
    
    public static final int UNRECOGNISED = 0;
    
    String type;
    FormIndex fi;
    public String caption;
    private String audioText;
    private String imageText;
    private boolean repeatable;
    private boolean exists;
    
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

    
}
