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
import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;


/**
 * Replaces all references with their persisted versions and persists unknown objects
 * 
 * @author mbechler
 * 
 */
public class PersistStrategy implements ReplacementStrategy {

    private static final Logger log = Logger.getLogger(PersistStrategy.class);


    @Override
    public <T extends ConfigurationObject> AbstractConfigurationObject<T> handleUnpersistedRef ( @NonNull EntityManager em,
            @NonNull PersistenceUtil pu, AbstractConfigurationObject<?> enclosing, T refObj, T outerRef, boolean collection )
                    throws ModelServiceException, ModelObjectException {

        @SuppressWarnings ( "unchecked" )
        AbstractConfigurationObject<T> ref = (AbstractConfigurationObject<T>) refObj;

        ref.setId(null);
        if ( log.isDebugEnabled() ) {
            log.debug("Automatically persisting sub object " + refObj); //$NON-NLS-1$
        }

        if ( !collection ) {
            if ( enclosing.getName() != null ) {
                ref.setDisplayName(enclosing.getName());
            }
            else {
                ref.setDisplayName(enclosing.getDisplayName());
            }
        }
        if ( log.isDebugEnabled() ) {
            log.debug("Setting outer object " + enclosing); //$NON-NLS-1$
        }
        ref.setOuterObject(enclosing);
        enclosing.getSubObjects().add(ref);
        ref.setAnchor(enclosing.getAnchor());

        ObjectReferenceUtil.replaceReferences(em, pu, this, ref);

        em.persist(ref);
        UsageUtil.updateUsage(ref);
        em.persist(ref);

        return ref;
    }


    @Override
    public <T extends ConfigurationObject> @Nullable AbstractConfigurationObject<T> handlePersistedRef ( @NonNull EntityManager em,
            @NonNull PersistenceUtil pu, AbstractConfigurationObject<?> enclosing, T refObj, T outerRef, boolean collection )
                    throws ModelObjectNotFoundException, ModelServiceException {

        if ( refObj == null ) {
            return null;
        }

        return pu.fetch(em, refObj, "Failed to fetch referenced object"); //$NON-NLS-1$
    }

}