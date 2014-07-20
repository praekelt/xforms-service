package org.praekelt.tools;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author victorgeere
 */
public class Tuple extends ArrayList {
    public Tuple(Object... elements) {
        for (int i = 0; i < elements.length; ++i) {
            this.add(elements[i]);
        }
    }
    
    public static Tuple make(Object... elements) {
        return new Tuple(elements);
    }
}
