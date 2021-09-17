/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.08.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.util;


import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;


/**
 * @author mbechler
 * 
 */
public final class ReferenceWalker {

    /**
     * 
     */
    private ReferenceWalker () {}

    private static final Set<String> BLACKLIST_PROPERTIES = new HashSet<>();


    static {
        BLACKLIST_PROPERTIES.add("type"); //$NON-NLS-1$
        BLACKLIST_PROPERTIES.add("defaultFor"); //$NON-NLS-1$
        BLACKLIST_PROPERTIES.add("enforcedFor"); //$NON-NLS-1$
        BLACKLIST_PROPERTIES.add("inheritedBy"); //$NON-NLS-1$
        BLACKLIST_PROPERTIES.add("subObjects"); //$NON-NLS-1$
        BLACKLIST_PROPERTIES.add("usedBy"); //$NON-NLS-1$
        BLACKLIST_PROPERTIES.add("uses"); //$NON-NLS-1$
        BLACKLIST_PROPERTIES.add("id"); //$NON-NLS-1$
        BLACKLIST_PROPERTIES.add("anchor"); //$NON-NLS-1$
        BLACKLIST_PROPERTIES.add("displayName"); //$NON-NLS-1$
        BLACKLIST_PROPERTIES.add("outerObject"); //$NON-NLS-1$
        BLACKLIST_PROPERTIES.add("revision"); //$NON-NLS-1$
        BLACKLIST_PROPERTIES.add("version"); //$NON-NLS-1$
        BLACKLIST_PROPERTIES.add("hibernateLazyInitializer"); //$NON-NLS-1$
        BLACKLIST_PROPERTIES.add("empty"); //$NON-NLS-1$
    }


    /**
     * @param ctx
     * @param obj
     * @param handler
     * @throws ModelServiceException
     * @throws ModelObjectException
     */
    public static <T extends ConfigurationObject> void walk ( ReferenceWalkerContext ctx, T obj, ReferenceHandler handler )
            throws ModelServiceException, ModelObjectException {
        handler.begin(ctx, obj);
        walkReferences(ctx, obj, handler);
    }


    /**
     * Walks the objects direct references and calls handleReference for each found
     * 
     * @param ctx
     * @param obj
     * @param handler
     * @throws ModelServiceException
     * @throws ModelObjectException
     */
    public static <T extends ConfigurationObject> void walkReferences ( ReferenceWalkerContext ctx, T obj, ReferenceHandler handler )
            throws ModelServiceException, ModelObjectException {

        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());

            for ( PropertyDescriptor property : beanInfo.getPropertyDescriptors() ) {
                handleReference(ctx, obj, property, handler);
            }
        }
        catch ( IntrospectionException e ) {
            throw new ModelServiceException("Failed to introspect configuration object", e); //$NON-NLS-1$
        }
    }


    /**
     * @param obj
     * @param property
     * @throws ModelServiceException
     * @throws ModelObjectException
     */
    private static <T extends ConfigurationObject> void handleReference ( ReferenceWalkerContext ctx, T obj, PropertyDescriptor property,
            ReferenceHandler handler ) throws ModelServiceException, ModelObjectException {

        Method getter = property.getReadMethod();
        ReferencedObject reference = ReflectionUtil.getReference(obj.getClass(), getter);

        if ( reference == null ) {
            Class<?> declaringClass = property.getReadMethod().getDeclaringClass();
            if ( Object.class.equals(declaringClass) ) {
                return;
            }
            if ( !ConfigurationObject.class.isAssignableFrom(declaringClass) ) {
                return;
            }
            if ( BLACKLIST_PROPERTIES.contains(property.getName()) ) {
                return;
            }
            handler.handleValue(ctx, obj, property);
            return;
        }

        handler.handleReference(ctx, obj, property);
    }

}
