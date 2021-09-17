/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.util;


import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObjectMutable;


/**
 * @author mbechler
 * 
 */
public final class UsageUtil {

    private static final Logger log = Logger.getLogger(UsageUtil.class);


    private UsageUtil () {}


    /**
     * @param obj
     * @return the set of objects that depend on this object
     */
    public static Set<ConfigurationObject> getAffectedClosure ( AbstractConfigurationObject<?> obj ) {
        Set<ConfigurationObject> closure = new HashSet<>();

        // direct usage
        closure.addAll(obj.getUsedBy());

        // inheritance

        // TODO: defaults, enforced

        // recurse on affected objects
        for ( ConfigurationObject dep : obj.getUsedBy() ) {
            closure.addAll(getAffectedClosure((AbstractConfigurationObject<?>) dep));
        }

        return closure;
    }


    /**
     * @param obj
     * @return the set of objects that this object depends on
     */
    public static Set<ConfigurationObject> getDependencies ( AbstractConfigurationObject<?> obj ) {
        Set<ConfigurationObject> closure = new HashSet<>();

        closure.addAll(obj.getUses());

        // TODO: defaults, enforced

        for ( ConfigurationObject dep : obj.getUses() ) {
            closure.addAll(getDependencies((AbstractConfigurationObject<?>) dep));
        }

        return closure;
    }


    /**
     * @param obj
     * @throws ModelServiceException
     */
    public static void updateUsage ( AbstractConfigurationObject<?> obj ) throws ModelServiceException {

        if ( log.isDebugEnabled() ) {
            log.debug("Updating usage pointers of " + obj); //$NON-NLS-1$
        }

        Set<ConfigurationObject> oldUses = obj.getUses();
        Set<ConfigurationObject> uses = getActualUses(obj);

        if ( !oldUses.equals(uses) ) {
            clearAllReferences(obj);
            obj.getUses().clear();

            for ( ConfigurationObject use : uses ) {
                setupReference(obj, obj.getUses(), (AbstractConfigurationObject<?>) use);
            }
        }
    }


    /**
     * 
     * @param obj
     * @return the set of directly used objects for the given obj
     * @throws ModelServiceException
     */
    public static Set<ConfigurationObject> getActualUses ( ConfigurationObjectMutable obj ) throws ModelServiceException {
        Set<ConfigurationObject> uses = new HashSet<>();

        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(obj.getClass());
        }
        catch ( IntrospectionException e ) {
            throw new ModelServiceException("Failed to introspect object class", e); //$NON-NLS-1$
        }

        for ( PropertyDescriptor prop : beanInfo.getPropertyDescriptors() ) {
            if ( log.isTraceEnabled() ) {
                log.trace("Found property " + prop.getName()); //$NON-NLS-1$
            }
            handleProperty(obj, uses, prop);
        }
        return uses;
    }


    private static void handleProperty ( ConfigurationObjectMutable obj, Set<ConfigurationObject> uses, PropertyDescriptor prop )
            throws ModelServiceException {
        ReferencedObject ref = ReflectionUtil.getReference(obj.getClass(), prop.getReadMethod());
        if ( ref == null ) {
            return;
        }

        if ( log.isTraceEnabled() ) {
            log.trace("Found reference property " + prop.getName()); //$NON-NLS-1$
        }

        try {
            handleValue(uses, prop, prop.getReadMethod().invoke(obj));
        }
        catch (
            IllegalAccessException |
            IllegalArgumentException |
            InvocationTargetException e ) {
            throw new ModelServiceException("Failed to get referenced object:", e); //$NON-NLS-1$
        }
    }


    @SuppressWarnings ( "unchecked" )
    private static void handleValue ( Set<ConfigurationObject> uses, PropertyDescriptor m, Object res ) {
        if ( Collection.class.isAssignableFrom(m.getPropertyType()) ) {
            if ( res == null ) {
                return;
            }
            for ( ConfigurationObject refObj : (Collection<? extends ConfigurationObject>) res ) {
                uses.add(refObj);
            }
        }
        else {
            uses.add((ConfigurationObject) res);
        }
    }


    private static void clearAllReferences ( AbstractConfigurationObject<?> obj ) {
        log.debug("Clearing used by"); //$NON-NLS-1$
        Set<ConfigurationObject> oldUses = obj.getUses();

        for ( ConfigurationObject oldUse : oldUses ) {
            Set<@NonNull ConfigurationObject> usedBy = ( (AbstractConfigurationObject<?>) oldUse ).getUsedBy();
            usedBy.remove(obj);
        }
    }


    private static void setupReference ( @NonNull AbstractConfigurationObject<?> referent, Set<ConfigurationObject> uses,
            AbstractConfigurationObject<?> referenced ) {
        if ( referenced == null ) {
            return;
        }
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Setup reference %s -> %s", referent, referenced)); //$NON-NLS-1$
        }
        uses.add(referenced);
        referenced.getUsedBy().add(referent);
    }
}
