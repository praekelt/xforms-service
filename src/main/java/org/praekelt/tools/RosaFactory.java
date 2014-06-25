package org.praekelt.tools;

import java.io.Serializable;

/**
 * The object that will contain the xform state
 *
 * @author victorgeere
 */
public class RosaFactory implements Serializable {

    public RosaFactory getInstance() {
        return new RosaFactory();
    }
}
