/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.02.2016 by mbechler
 */
package eu.agno3.runtime.db.jmx;


import eu.agno3.runtime.db.DatabaseException;


/**
 * @author mbechler
 *
 */
public interface DatabaseManagementMXBean {

    /**
     * @param ds
     * @param timeout
     * @param tu
     * @return whether the database was locked
     * @throws DatabaseException
     */
    boolean lockDataSource ( String ds, long timeout ) throws DatabaseException;


    /**
     * @param ds
     * @throws DatabaseException
     */
    void unlockDataSource ( String ds ) throws DatabaseException;

}
