/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.util;


import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;


/**
 * @author mbechler
 * 
 */
final class MergeStrategy extends PersistStrategy {

    private static final Logger log = Logger.getLogger(MergeStrategy.class);


    @SuppressWarnings ( "unchecked" )
    @Override
    public <T extends ConfigurationObject> AbstractConfigurationObject<T> handlePersistedRef ( @NonNull EntityManager em, @NonNull PersistenceUtil pu,
            AbstractConfigurationObject<?> enclosing, T refObj, T outerRef, boolean collection )
                    throws ModelServiceException, ModelObjectNotFoundException {

        AbstractConfigurationObject<T> obj = pu.fetch(em, refObj);

        if ( log.isDebugEnabled() ) {
            log.debug("Merging persisted referenced object " + refObj); //$NON-NLS-1$
        }
        ObjectReferenceUtil.prepareObjectForMerge(em, pu, obj, refObj);
        if ( !collection ) {
            if ( enclosing.getName() != null ) {
                ( (AbstractConfigurationObject<T>) refObj ).setDisplayName(enclosing.getName());
            }
            else {
                ( (AbstractConfigurationObject<T>) refObj ).setDisplayName(enclosing.getDisplayName());
            }
        }
        em.merge(refObj);

        return obj;
    }
}