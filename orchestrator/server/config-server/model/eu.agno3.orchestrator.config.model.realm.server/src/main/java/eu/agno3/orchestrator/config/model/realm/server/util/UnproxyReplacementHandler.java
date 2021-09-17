/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.08.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.util;


import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.collection.spi.PersistentCollection;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;


/**
 * @author mbechler
 * 
 */
public class UnproxyReplacementHandler extends AbstractReferenceReplacementHandler {

    private static final Logger log = Logger.getLogger(UnproxyReplacementHandler.class);


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.server.util.AbstractReferenceReplacementHandler#handleReferenceCollection(eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject,
     *      java.util.Collection)
     */
    @Override
    protected List<ConfigurationObject> handleReferenceCollection ( ReferenceWalkerContext ctx, ConfigurationObject obj,
            Collection<ConfigurationObject> value ) throws ModelObjectException, ModelServiceException {
        List<ConfigurationObject> unproxied = new LinkedList<>();
        for ( ConfigurationObject elem : value ) {
            if ( elem == null ) {
                continue;
            }
            unproxied.add(unproxy(elem));
        }
        return unproxied;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.server.util.AbstractReferenceReplacementHandler#handlePrimitiveReference(eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObject, java.beans.PropertyDescriptor)
     */
    @Override
    protected Object handlePrimitiveReference ( ReferenceWalkerContext ctx, ConfigurationObject obj, ConfigurationObject value,
            PropertyDescriptor property ) throws ModelServiceException, ModelObjectException {
        if ( value == null ) {
            return null;
        }
        return unproxy(value);
    }


    private static @NonNull ConfigurationObject unproxy ( @NonNull ConfigurationObject value ) {
        if ( log.isTraceEnabled() ) {
            log.trace("Unproxy " + value); //$NON-NLS-1$
        }

        return PersistenceUtil.unproxy(value);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.util.AbstractReferenceReplacementHandler#handlePrimitiveProperty(eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject,
     *      java.lang.Object, java.beans.PropertyDescriptor)
     */
    @Override
    protected Object handlePrimitiveProperty ( ReferenceWalkerContext ctx, ConfigurationObject obj, Object value, PropertyDescriptor property )
            throws ModelServiceException, ModelObjectException {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.util.AbstractReferenceReplacementHandler#handlePropertyCollection(eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject,
     *      java.util.Collection)
     */
    @Override
    protected Collection<Object> handlePropertyCollection ( ReferenceWalkerContext ctx, ConfigurationObject obj, Collection<Object> value )
            throws ModelObjectException, ModelServiceException {
        if ( value instanceof PersistentCollection ) {
            PersistentCollection col = (PersistentCollection) value;
            col.forceInitialization();
        }
        return null;
    }
}
