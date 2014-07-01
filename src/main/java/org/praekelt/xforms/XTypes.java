
package org.praekelt.xforms;

/**
 * Contains the valid data types and methods to validate them
 * 
 * @author victorgeere
 */
public final class XTypes {

    public static final int INT = 0;
    public static int FLOAT = 1;
    public static int LONG = 2;
    public static int STRING = 3;
    public static int DATE = 4;
    public static int INFO = 5;
    public static int TIME = 6;
    public static int SELECT = 7;
    public static int MULTISELECT = 8;
    public static int GEO = 9;
    
    public static boolean equals(String value, int xtype) {
        //TODO: compare string value with valid xtype
        return false;
    }
    
}
