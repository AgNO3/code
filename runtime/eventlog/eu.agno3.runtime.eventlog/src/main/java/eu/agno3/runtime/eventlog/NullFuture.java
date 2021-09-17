/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.06.2015 by mbechler
 */
package eu.agno3.runtime.eventlog;


import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * @author mbechler
 *
 */
public class NullFuture implements Future<Object> {

    /**
     * {@inheritDoc}
     *
     * @see java.util.concurrent.Future#cancel(boolean)
     */
    @Override
    public boolean cancel ( boolean mayInterruptIfRunning ) {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.util.concurrent.Future#isCancelled()
     */
    @Override
    public boolean isCancelled () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.util.concurrent.Future#isDone()
     */
    @Override
    public boolean isDone () {
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.util.concurrent.Future#get()
     */
    @Override
    public Object get () throws InterruptedException, ExecutionException {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.util.concurrent.Future#get(long, java.util.concurrent.TimeUnit)
     */
    @Override
    public Object get ( long timeout, TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }

}
