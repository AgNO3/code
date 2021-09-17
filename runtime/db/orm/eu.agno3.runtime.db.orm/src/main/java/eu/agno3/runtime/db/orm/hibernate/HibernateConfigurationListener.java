/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.08.2013 by mbechler
 */
package eu.agno3.runtime.db.orm.hibernate;


import org.hibernate.boot.Metadata;

import eu.agno3.runtime.db.orm.PersistenceUnitDescriptor;


/**
 * @author mbechler
 * 
 */
public interface HibernateConfigurationListener {

    /**
     * @param puName
     * @param meta
     */
    void generatedConfiguation ( PersistenceUnitDescriptor puName, Metadata meta );
}
