/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.09.2016 by mbechler
 */
package eu.agno3.runtime.jsf.config.cdi;


import java.util.concurrent.locks.ReentrantLock;


/**
 * @author mbechler
 *
 */
public class DebugableReentrantLock extends ReentrantLock {

    /**
     * 
     */
    private static final long serialVersionUID = 8907514788452102096L;


    @Override
    public Thread getOwner () {
        return super.getOwner();
    }
}
