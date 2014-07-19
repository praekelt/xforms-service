/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.praekelt.tools;

import java.util.HashMap;

/**
 *
 * @author victorgeere
 */
public class Extra extends HashMap {
    
    public static Extra getInstance(String key, Object value) {
        Extra e = new Extra(key, value);
        return e;
    }

    public Extra(String key, Object value) {
        this.put(key, value);
    }
    
    public void update(Object obj) {
    
    }
}
