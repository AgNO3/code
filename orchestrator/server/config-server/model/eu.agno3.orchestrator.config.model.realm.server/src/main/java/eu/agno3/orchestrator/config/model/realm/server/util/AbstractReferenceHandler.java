/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.08.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.util;


import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;


/**
 * @author mbechler
 * 
 */
public abstract class AbstractReferenceHandler implements ReferenceHandler {

    private static final Logger log = Logger.getLogger(AbstractReferenceHandler.class);


    protected static Object doGetReference ( ConfigurationObject obj, Method getter ) throws ModelServiceException {
        Object value;
        try {
            value = getter.invoke(obj);
        }
        catch (
            IllegalAccessException |
            IllegalArgumentException |
            InvocationTargetException e ) {
            log.warn("Failed to invoke getter on reference", e); //$NON-NLS-1$
            throw new ModelServiceException("Failed to invoke getter for reference", e); //$NON-NLS-1$
        }
        return value;
    }


    @Override
    public void handleReference ( ReferenceWalkerContext ctx, ConfigurationObject obj, PropertyDescriptor property )
            throws ModelServiceException, ModelObjectException {
        handleReferenceValue(ctx, obj, property, doGetReference(obj, property.getReadMethod()));
    }


    @Override
    public void handleValue ( ReferenceWalkerContext ctx, ConfigurationObject obj, PropertyDescriptor property )
            throws ModelServiceException, ModelObjectException {
        handlePropertyValue(ctx, obj, property, doGetReference(obj, property.getReadMethod()));
    }


    @Override
    public void begin ( ReferenceWalkerContext ctx, ConfigurationObject obj ) throws ModelServiceException, ModelObjectException {
        // empty default impl
    }


    protected abstract void handlePropertyValue ( ReferenceWalkerContext ctx, ConfigurationObject obj, PropertyDescriptor property, Object value )
            throws ModelServiceException, ModelObjectException;


    protected abstract void handleReferenceValue ( ReferenceWalkerContext ctx, ConfigurationObject obj, PropertyDescriptor property, Object value )
            throws ModelServiceException, ModelObjectException;


    protected static void doReplaceReference ( ReferenceWalkerContext ctx, ConfigurationObject obj, Method setter, Object replaceWith )
            throws ModelServiceException {
        try {
            setter.invoke(obj, replaceWith);
        }
        catch (
            IllegalAccessException |
            IllegalArgumentException |
            InvocationTargetException e ) {
            log.warn("Reference replacement failed:", e); //$NON-NLS-1$
            throw new ModelServiceException("Failed to replace reference with persisted instance", e); //$NON-NLS-1$
        }
    }

}