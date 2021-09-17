/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.11.2016 by mbechler
 */
package eu.agno3.runtime.elasticsearch;


import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


/**
 * @author mbechler
 *
 */
public class MappingMigrationStatus {

    private final MappingStatus overallStatus;
    private final Collection<Future<?>> futures;


    /**
     * @param overallStatus
     * @param futures
     */
    public MappingMigrationStatus ( MappingStatus overallStatus, Collection<Future<?>> futures ) {
        this.overallStatus = overallStatus;
        this.futures = futures;
    }


    /**
     * @return the overallStatus
     */
    public MappingStatus getOverallStatus () {
        return this.overallStatus;
    }


    /**
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public void await () throws InterruptedException, ExecutionException {
        for ( Future<?> f : this.futures ) {
            f.get();
        }
    }
}
