/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.08.2013 by mbechler
 */
package eu.agno3.runtime.db.orm;


import java.util.Set;


/**
 * @author mbechler
 * 
 */
public interface DynamicPersistenceProvider {

    /**
     * Rebuild the EntityManagerFactory for the given PersistenceUnit
     * 
     * @param pu
     * @throws EntityManagerConfigurationFailedException
     */
    void rebuildEntityManagerFactory ( String pu ) throws EntityManagerConfigurationFailedException;


    /**
     * 
     * @return the registered persistence units
     */
    Set<String> getPersistenceUnits ();
}
