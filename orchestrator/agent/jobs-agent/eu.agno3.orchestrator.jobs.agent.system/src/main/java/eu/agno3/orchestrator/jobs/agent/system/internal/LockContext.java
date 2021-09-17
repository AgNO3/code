/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.02.2016 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.system.internal;


import java.io.IOException;
import java.util.concurrent.locks.Lock;


/**
 * @author mbechler
 *
 */
public class LockContext implements AutoCloseable {

    private FileLock fileLock;
    private Lock lock;


    /**
     * @param fileLock
     * @param rl
     */
    public LockContext ( FileLock fileLock, Lock rl ) {
        this.fileLock = fileLock;
        this.lock = rl;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close () throws IOException {
        this.fileLock.release(this);
    }


    Lock getLock () {
        return this.lock;
    }

}
