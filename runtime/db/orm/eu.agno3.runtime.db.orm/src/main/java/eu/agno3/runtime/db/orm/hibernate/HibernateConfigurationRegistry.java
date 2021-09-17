/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.08.2013 by mbechler
 */
package eu.agno3.runtime.db.orm.hibernate;


import org.hibernate.boot.Metadata;


/**
 * @author mbechler
 * 
 */
public interface HibernateConfigurationRegistry {

    /**
     * 
     * @param pu
     * @return the configuration for persistence unit pu
     */
    Metadata getMetadata ( String pu );


    /**
     * @param pu
     * @return whether a configuration for the given pu is present
     */
    boolean hasMetadata ( String pu );

}
