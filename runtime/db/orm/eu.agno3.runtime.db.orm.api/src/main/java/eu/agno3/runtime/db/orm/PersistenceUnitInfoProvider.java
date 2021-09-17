/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.07.2015 by mbechler
 */
package eu.agno3.runtime.db.orm;


import javax.persistence.spi.PersistenceUnitInfo;


/**
 * @author mbechler
 *
 */
public interface PersistenceUnitInfoProvider {

    /**
     * @return the persistence unit info
     */
    PersistenceUnitInfo getPersistenceUnitInfo ();

}
