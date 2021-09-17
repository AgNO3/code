/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.08.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.util;


import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;


/**
 * @author mbechler
 * 
 */
public abstract class AbstractReferenceReplacementHandler extends AbstractReferenceHandler {

    private static final Logger log = Logger.getLogger(AbstractReferenceReplacementHandler.class);


    protected abstract List<ConfigurationObject> handleReferenceCollection ( ReferenceWalkerContext ctx, ConfigurationObject obj,
            Collection<ConfigurationObject> value ) throws ModelObjectException, ModelServiceException;


    protected abstract Object handlePrimitiveReference ( ReferenceWalkerContext ctx, ConfigurationObject obj, ConfigurationObject value,
            PropertyDescriptor property ) throws ModelServiceException, ModelObjectException;


    protected abstract Collection<Object> handlePropertyCollection ( ReferenceWalkerContext ctx, ConfigurationObject obj, Collection<Object> value )
            throws ModelObjectException, ModelServiceException;


    protected abstract Object handlePrimitiveProperty ( ReferenceWalkerContext ctx, ConfigurationObject obj, Object value,
            PropertyDescriptor property ) throws ModelServiceException, ModelObjectException;


    @Override
    protected void handleReferenceValue ( ReferenceWalkerContext ctx, ConfigurationObject obj, PropertyDescriptor property, Object value )
            throws ModelServiceException, ModelObjectException {
        Object replaceWith = getReplacementValue(ctx, obj, value, property);

        if ( property.getWriteMethod() == null ) {
            throw new ModelServiceException(String.format(
                "Failed to get setter for property %s (%s) in %s", //$NON-NLS-1$
                property.getName(),
                property.getPropertyType().getName(),
                obj.getClass()));
        }

        if ( replaceWith != null ) {
            doReplaceReference(ctx, obj, property.getWriteMethod(), replaceWith);
        }
    }


    @Override
    protected void handlePropertyValue ( ReferenceWalkerContext ctx, ConfigurationObject obj, PropertyDescriptor property, Object value )
            throws ModelServiceException, ModelObjectException {

        Object replaceWith = getReplacementPropertyValue(ctx, obj, value, property);

        if ( property.getWriteMethod() == null ) {
            throw new ModelServiceException(String.format(
                "Failed to get setter for property %s (%s) in %s", //$NON-NLS-1$
                property.getName(),
                property.getPropertyType().getName(),
                obj.getClass()));
        }

        if ( replaceWith != null ) {
            doReplaceReference(ctx, obj, property.getWriteMethod(), replaceWith);
        }
    }


    /**
     * @param ctx
     * @param obj
     * @param value
     * @param property
     * @return
     * @throws ModelObjectException
     * @throws ModelServiceException
     */
    @SuppressWarnings ( "unchecked" )
    private Object getReplacementPropertyValue ( ReferenceWalkerContext ctx, ConfigurationObject obj, Object value, PropertyDescriptor property )
            throws ModelServiceException, ModelObjectException {
        if ( value instanceof Collection ) {
            return handlePropertyCollectionInternal(ctx, obj, (Collection<Object>) value, property);
        }
        else if ( value instanceof Map ) {
            return handlePropertyMapInternal(ctx, obj, (Map<?, ?>) value, property);
        }
        else if ( value != null ) {
            return handlePrimitivePropertyInternal(ctx, obj, value, property);
        }
        else if ( !property.getPropertyType().isAssignableFrom(Collections.class) && !property.getPropertyType().isAssignableFrom(Map.class) ) {
            return handleEmptyProperty(ctx, obj, property);
        }

        return handleEmptyPropertyCollection(ctx, obj, property);
    }


    /**
     * @param ctx
     * @param obj
     * @param property
     * @return
     * @throws ModelServiceException
     * @throws ModelObjectException
     */
    protected Object handleEmptyPropertyCollection ( ReferenceWalkerContext ctx, ConfigurationObject obj, PropertyDescriptor property )
            throws ModelServiceException, ModelObjectException {
        return null;
    }


    /**
     * @param ctx
     * @param obj
     * @param property
     * @return
     * @throws ModelServiceException
     * @throws ModelObjectException
     */
    protected Object handleEmptyProperty ( ReferenceWalkerContext ctx, ConfigurationObject obj, PropertyDescriptor property )
            throws ModelServiceException, ModelObjectException {
        return null;
    }


    @SuppressWarnings ( "unchecked" )
    protected Object getReplacementValue ( ReferenceWalkerContext ctx, ConfigurationObject obj, Object value, PropertyDescriptor property )
            throws ModelServiceException, ModelObjectException {
        if ( value instanceof Collection ) {
            return handleReferenceCollectionInternal(ctx, obj, (Collection<ConfigurationObject>) value, property);
        }
        else if ( value != null ) {
            return handlePrimitiveReferenceInternal(ctx, obj, (ConfigurationObject) value, property);
        }
        else if ( !property.getPropertyType().isAssignableFrom(Collections.class) && !property.getPropertyType().isAssignableFrom(Map.class) ) {
            return handleEmptyReference(ctx, obj, property);
        }
        return handleEmptyReferenceCollection(ctx, obj, property);
    }


    /**
     * @param ctx
     * @param obj
     * @param property
     * @return
     * @throws ModelServiceException
     * @throws ModelObjectException
     */
    protected Object handleEmptyReferenceCollection ( ReferenceWalkerContext ctx, ConfigurationObject obj, PropertyDescriptor property )
            throws ModelServiceException, ModelObjectException {
        return null;
    }


    /**
     * @param obj
     * @param property
     * @return
     * @throws ModelServiceException
     * @throws ModelObjectException
     */
    protected Object handleEmptyReference ( ReferenceWalkerContext ctx, ConfigurationObject obj, PropertyDescriptor property )
            throws ModelServiceException, ModelObjectException {
        return null;
    }


    private Object handlePrimitiveReferenceInternal ( ReferenceWalkerContext ctx, ConfigurationObject obj, ConfigurationObject value,
            PropertyDescriptor property ) throws ModelServiceException, ModelObjectException {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Found reference object %s in property %s of %s", value, property.getName(), obj)); //$NON-NLS-1$
        }

        return handlePrimitiveReference(ctx, obj, value, property);
    }


    private Object handleReferenceCollectionInternal ( ReferenceWalkerContext ctx, ConfigurationObject obj, Collection<ConfigurationObject> value,
            PropertyDescriptor property ) throws ModelServiceException, ModelObjectException {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Found reference collection of size %d in property %s of %s", value.size(), property.getName(), obj)); //$NON-NLS-1$
        }
        Collection<ConfigurationObject> replaceWithCollection = handleReferenceCollection(ctx, obj, value);

        // Hibernate does not like when the collection identity changes
        if ( replaceWithCollection != null /* && !replaceWithCollection.equals(value) */ ) {
            // cannot just check for equals, otherwise we might end up with detached entities on persist
            // for merge this should be fine.
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Replacing reference collection in property %s of %s", property.getName(), obj)); //$NON-NLS-1$
            }

            value.clear();
            if ( !replaceWithCollection.isEmpty() ) {
                value.addAll(replaceWithCollection);
            }
        }
        return value;
    }


    private Object handlePrimitivePropertyInternal ( ReferenceWalkerContext ctx, ConfigurationObject obj, Object value, PropertyDescriptor property )
            throws ModelServiceException, ModelObjectException {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Found property object %s in property %s of %s", value, property.getName(), obj)); //$NON-NLS-1$
        }

        return handlePrimitiveProperty(ctx, obj, value, property);
    }


    private Object handlePropertyCollectionInternal ( ReferenceWalkerContext ctx, ConfigurationObject obj, Collection<Object> value,
            PropertyDescriptor property ) throws ModelServiceException, ModelObjectException {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Found property collection of size %d in property %s of %s", value.size(), property.getName(), obj)); //$NON-NLS-1$
        }

        // Hibernate does not like when the collection identity changes
        Collection<Object> replaceWithCollection = handlePropertyCollection(ctx, obj, value);
        if ( replaceWithCollection != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Replacing property collection in property %s of %s", property.getName(), obj)); //$NON-NLS-1$
            }
            value.clear();
            if ( !replaceWithCollection.isEmpty() ) {
                value.addAll(replaceWithCollection);
            }
        }
        return value;
    }


    /**
     * @param ctx
     * @param obj
     * @param value
     * @param property
     * @return
     */
    private static Object handlePropertyMapInternal ( ReferenceWalkerContext ctx, ConfigurationObject obj, Map<?, ?> value,
            PropertyDescriptor property ) {
        // TODO Auto-generated method stub
        return value;
    }

}