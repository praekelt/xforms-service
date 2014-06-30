package org.praekelt.xforms;

/**
 * Threading not supposed to be done manually in a servlet, this is just to
 * simplify the translation from touchforms.
 *
 * @author victorgeere
 */
public class Lock {

    public boolean acquire(boolean b) {
        return true;
    }

    public boolean acquire() {
        return true;
    }
}
