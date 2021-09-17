/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.05.2015 by mbechler
 */
package eu.agno3.runtime.eventlog.internal;


import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * @author mbechler
 *
 */
public class CompositeFuture implements Future<Object> {

    private List<Future<?>> futures;


    /**
     * @param futures
     */
    public CompositeFuture ( List<Future<?>> futures ) {
        this.futures = futures;
    }


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
        for ( Future<?> future : this.futures ) {
            if ( future != null && !future.isDone() ) {
                return false;
            }
        }
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.util.concurrent.Future#get()
     */
    @Override
    public Object get () throws InterruptedException, ExecutionException {
        for ( Future<?> future : this.futures ) {
            if ( future != null ) {
                future.get();
            }
        }
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.util.concurrent.Future#get(long, java.util.concurrent.TimeUnit)
     */
    @Override
    public Object get ( long timeout, TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException {
        for ( Future<?> future : this.futures ) {
            if ( future != null ) {
                future.get(timeout, unit);
            }
        }
        return null;
    }

}
