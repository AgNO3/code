/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.08.2013 by mbechler
 */
package eu.agno3.runtime.db.orm.internal;


import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.boot.Metadata;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.db.orm.PersistenceUnitDescriptor;
import eu.agno3.runtime.db.orm.hibernate.HibernateConfigurationListener;
import eu.agno3.runtime.db.orm.hibernate.HibernateConfigurationRegistry;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    HibernateConfigurationListener.class, HibernateConfigurationRegistry.class
}, immediate = true )
public class HibernateConfigurationRegistryImpl implements HibernateConfigurationListener, HibernateConfigurationRegistry {

    private static final Logger log = Logger.getLogger(HibernateConfigurationRegistryImpl.class);

    private Map<String, Metadata> metadata = new HashMap<>();


    @Activate
    protected void activate ( ComponentContext context ) {
        log.debug("Starting HibernateConfigurationRegistry"); //$NON-NLS-1$
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.orm.hibernate.HibernateConfigurationRegistry#getMetadata(java.lang.String)
     */
    @Override
    public Metadata getMetadata ( String pu ) {
        if ( !this.metadata.containsKey(pu) ) {
            throw new IllegalArgumentException("Persistence unit metadata not registered: " + pu); //$NON-NLS-1$
        }

        return this.metadata.get(pu);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.orm.hibernate.HibernateConfigurationListener#generatedConfiguation(eu.agno3.runtime.db.orm.PersistenceUnitDescriptor,
     *      org.hibernate.boot.Metadata)
     */
    @Override
    public void generatedConfiguation ( PersistenceUnitDescriptor puDesc, Metadata meta ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Got new persistence unit metadata for " + puDesc.getPersistenceUnitName()); //$NON-NLS-1$
        }
        this.metadata.put(puDesc.getPersistenceUnitName(), meta);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.orm.hibernate.HibernateConfigurationRegistry#hasMetadata(java.lang.String)
     */
    @Override
    public boolean hasMetadata ( String pu ) {
        return this.metadata.containsKey(pu);
    }

}
