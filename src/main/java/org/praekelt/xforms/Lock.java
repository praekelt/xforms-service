package org.praekelt.xforms;

import java.util.ArrayList;
import java.util.List;

/**
 * This is not a threading lock but a form lock to prevent editing while
 * persistence methods are called.
 *
 * @author victorgeere
 */
public class Lock {

    private static volatile Lock instance = null;

    List<String> locks = new ArrayList<String>();
    
    private Lock() {
    }

    /**
     * Lazy singleton instantiation
     *
     * @return
     */
    public static Lock getInstance() {
        if (instance == null) {
            synchronized (Lock.class) {
                //double check to avoid thread timing error
                if (instance == null) {
                    instance = new Lock();
                }
            }
        }
        return instance;
    }

    /**
     * 
     * @param b
     * @return 
     */
    public boolean acquire(boolean b) {
        return true;
    }

    /**
     * 
     * @param id
     * @return 
     */
    public boolean acquire(String id) {
        return locks.add(id);
    }

    public void release(String id) {
        locks.remove(id);
    }
}
