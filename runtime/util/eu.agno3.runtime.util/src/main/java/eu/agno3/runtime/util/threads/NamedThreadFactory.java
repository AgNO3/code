/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.02.2016 by mbechler
 */
package eu.agno3.runtime.util.threads;


import java.util.concurrent.ThreadFactory;


/**
 * @author mbechler
 *
 */
public class NamedThreadFactory implements ThreadFactory {

    private final String name;
    private final boolean daemon;


    /**
     * @param name
     * 
     */
    public NamedThreadFactory ( String name ) {
        this.name = name;
        this.daemon = false;
    }


    /**
     * @param name
     * @param daemon
     * 
     */
    public NamedThreadFactory ( String name, boolean daemon ) {
        this.name = name;
        this.daemon = daemon;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
     */
    @Override
    public Thread newThread ( Runnable r ) {
        Thread t = new Thread(r, this.name);
        t.setDaemon(this.daemon);
        return t;
    }

}
