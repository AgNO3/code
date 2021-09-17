/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 14, 2017 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.util;


import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.orchestrator.config.model.base.config.OneOff;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;


/**
 * @author mbechler
 *
 */
public class OneOffClearingReferenceVisitor extends AbstractReferenceVisitor {

    private static final Logger log = Logger.getLogger(OneOffClearingReferenceVisitor.class);

    private boolean changed;


    /**
     * @return the changed
     */
    public boolean isChanged () {
        return this.changed;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.util.AbstractReferenceVisitor#begin(eu.agno3.orchestrator.config.model.realm.server.util.ReferenceWalkerContext,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    public void begin ( ReferenceWalkerContext ctx, ConfigurationObject obj ) throws ModelServiceException, ModelObjectException {

    }


    @Override
    public void visitObject ( ReferenceWalkerContext ctx, ConfigurationObject val ) throws ModelServiceException, ModelObjectException {
        ReferenceWalker.walkReferences(ctx, val, this);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.util.RecursiveReferenceVisitor#handlePropertyValue(eu.agno3.orchestrator.config.model.realm.server.util.ReferenceWalkerContext,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObject, java.beans.PropertyDescriptor,
     *      java.lang.Object)
     */
    @Override
    protected void handlePropertyValue ( ReferenceWalkerContext ctx, ConfigurationObject obj, PropertyDescriptor property, Object val )
            throws ModelServiceException, ModelObjectException {

        if ( log.isTraceEnabled() ) {
            log.trace("Object " + obj.getType().getName()); //$NON-NLS-1$
        }

        @Nullable
        OneOff oneOff = ReflectionUtil
                .getInheritedMethodAnnotation(OneOff.class, obj.getClass(), property.getReadMethod(), ConfigurationObject.class);

        if ( oneOff == null ) {
            if ( log.isTraceEnabled() ) {
                log.trace("Is not a oneOff property " + property.getName()); //$NON-NLS-1$
            }
            return;
        }

        Boolean oldVal = (Boolean) val;
        ConfigurationObject local = obj;

        if ( Proxy.isProxyClass(obj.getClass()) ) {
            log.debug("Is a proxy " + obj); //$NON-NLS-1$
            InvocationHandler invocationHandler = Proxy.getInvocationHandler(obj);
            if ( ! ( invocationHandler instanceof InheritanceInvocationHandler ) ) {
                throw new ModelServiceException("Unsupported proxy type " + invocationHandler.getClass().getName()); //$NON-NLS-1$
            }

            @SuppressWarnings ( "unchecked" )
            InheritanceInvocationHandler<@Nullable ConfigurationObject> inh = (InheritanceInvocationHandler<@Nullable ConfigurationObject>) invocationHandler;
            local = inh.getLocal();
            if ( local == null ) {
                throw new ModelServiceException("No local object found"); //$NON-NLS-1$
            }
        }

        Boolean target = oneOff.resetToNull() ? null : oneOff.resetTo();
        if ( !Objects.equals(target, oldVal) ) {
            try {
                if ( log.isDebugEnabled() ) {
                    log.debug(String.format(
                        "Clearing oneOff property %s->%s oldVal: %s target: %s", //$NON-NLS-1$
                        obj.getType().getName(),
                        property.getName(),
                        oldVal,
                        target));
                }

                if ( local != obj ) {
                    Method setter = ReflectionUtil.getCorrespondingSetter((AbstractConfigurationObject<?>) local, property.getReadMethod());
                    setter.invoke(local, target);
                }
                else {
                    property.getWriteMethod().invoke(obj, target);
                }
                this.changed = true;
            }
            catch (
                IllegalAccessException |
                IllegalArgumentException |
                InvocationTargetException e ) {
                log.warn("Failed to set one off property " + property.getName(), e); //$NON-NLS-1$
            }
        }
    }

}
