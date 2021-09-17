/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.util;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;


/**
 * @author mbechler
 * @param <T>
 * 
 */
public class InheritanceInvocationHandler <@Nullable T extends ConfigurationObject> implements InvocationHandler {

    private static final Logger log = Logger.getLogger(InheritanceInvocationHandler.class);

    private static final Set<String> INTERNAL_METHOD_NAMES = new HashSet<>();


    static {
        INTERNAL_METHOD_NAMES.add("getAnchor"); //$NON-NLS-1$
        INTERNAL_METHOD_NAMES.add("getRevision"); //$NON-NLS-1$
        INTERNAL_METHOD_NAMES.add("getId"); //$NON-NLS-1$
    }

    private T enforced;
    private T local;
    private T parent;
    private T defaults;

    private final Class<? extends T> type;
    private StructuralObject at;

    @NonNull
    private final InheritanceProxyContext context;

    private final Map<String, Object> valueCache = new HashMap<>();


    /**
     * @param cc
     * @param at
     * @param enforced
     * @param local
     * @param parent
     * @param defaults
     * @param type
     */
    public InheritanceInvocationHandler ( @NonNull InheritanceProxyContext cc, StructuralObject at, @Nullable T enforced, T local, @Nullable T parent,
            @Nullable T defaults, Class<? extends T> type ) {

        this.context = cc;
        this.at = at;
        this.enforced = enforced;
        this.local = local;
        this.parent = parent;
        this.defaults = defaults;
        this.type = type;
    }


    /**
     * @return the local
     */
    T getLocal () {
        return this.local;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws ModelServiceException
     * 
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    @Override
    public Object invoke ( Object obj, Method m, Object[] args ) throws IllegalAccessException, InvocationTargetException, ModelServiceException {

        // directly delegate toString, hashCode and equals to prevent recursion
        Object internalDelegate = null;
        if ( this.local != null ) {
            // if there is a local object, delegate object methods to it
            internalDelegate = this.local;
        }
        else {
            // if there is no local object, delegate to the invocation handler object
            // this will not be very meaningful, but stable
            internalDelegate = this;
        }

        return invokeInternal(obj, m, args, internalDelegate);
    }


    /**
     * @param obj
     * @param m
     * @param args
     * @param internalDelegate
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws ModelServiceException
     */
    private Object invokeInternal ( Object obj, Method m, Object[] args, Object internalDelegate )
            throws IllegalAccessException, InvocationTargetException, ModelServiceException {
        if ( "toString".equals(m.getName()) ) { //$NON-NLS-1$
            return m.invoke(internalDelegate);
        }
        else if ( "hashCode".equals(m.getName()) ) { //$NON-NLS-1$
            return m.invoke(internalDelegate);
        }
        else if ( "equals".equals(m.getName()) && args.length == 1 ) { //$NON-NLS-1$
            // unwrap proxy so that objects still properly compare
            return m.invoke(internalDelegate, unwrapProxy(args[ 0 ]));
        }
        else if ( "compareTo".equals(m.getName()) && args.length == 1 ) { //$NON-NLS-1$
            // unwrap proxy so that objects still properly compare
            return m.invoke(internalDelegate, unwrapProxy(args[ 0 ]));
        }

        return invokeUserMethod(obj, m);
    }


    private Object invokeUserMethod ( Object obj, Method m ) throws IllegalAccessException, InvocationTargetException, ModelServiceException {
        if ( log.isTraceEnabled() ) {
            log.trace(String.format("Called %s.%s on obj %s", m.getDeclaringClass().getName(), m.getName(), obj)); //$NON-NLS-1$
        }

        if ( "getInherits".equals(m.getName()) ) { //$NON-NLS-1$
            return null;
        }
        else if ( "getType".equals(m.getName()) ) { //$NON-NLS-1$
            return invokeGetType(m);
        }
        else if ( "getVersion".equals(m.getName()) ) { //$NON-NLS-1$
            return invokeGetVersion(m);
        }
        else if ( "getDisplayName".equals(m.getName()) //$NON-NLS-1$
                || "getName".equals(m.getName()) ) { //$NON-NLS-1$
            return getNameValue(m);
        }
        else if ( INTERNAL_METHOD_NAMES.contains(m.getName()) ) {
            return invokeInternalMethod(m);
        }

        Object result = invokeGetter(m);
        if ( log.isTraceEnabled() ) {
            log.trace(String.format(
                "Returning %s from %s.%s on obj %s ( value types: %s)", //$NON-NLS-1$
                result,
                m.getDeclaringClass().getName(),
                m.getName(),
                obj,
                this.context.getValueTypes()));
        }
        return result;
    }


    /**
     * @param m
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private Object invokeInternalMethod ( Method m ) throws IllegalAccessException, InvocationTargetException {
        if ( this.local != null ) {
            return m.invoke(this.local);
        }
        return null;
    }


    /**
     * @param m
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private Object invokeGetVersion ( Method m ) throws IllegalAccessException, InvocationTargetException {
        if ( this.local != null ) {
            return m.invoke(this.local);
        }
        return Long.valueOf(0);
    }


    /**
     * @param m
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private Object invokeGetType ( Method m ) throws IllegalAccessException, InvocationTargetException {
        if ( this.local != null ) {
            return m.invoke(this.local);
        }
        return this.type;
    }


    private Object invokeGetter ( Method m ) throws IllegalAccessException, InvocationTargetException, ModelServiceException {
        if ( !m.getName().startsWith("get") || m.getParameterTypes().length > 0 ) { //$NON-NLS-1$
            throw new UnsupportedOperationException("Calling anything but bean property getters is unsupported"); //$NON-NLS-1$
        }

        Object cached = this.valueCache.get(m.getName());

        if ( cached != null ) {
            return cached;
        }

        ReferencedObject ref = ReflectionUtil.getReference(this.type, m);

        Object val;
        if ( ref != null ) {
            // this is a reference to another configuration object, potentially multi valued
            val = handleReference(m);
        }
        else {
            val = handleValue(m);
        }

        this.valueCache.put(m.getName(), val);
        return val;
    }


    private String getNameValue ( Method m ) throws IllegalAccessException, InvocationTargetException {
        // bit of a hack: also return the proper object name for default only or enforcement only objects
        String name = null;
        if ( this.local != null ) {
            name = (String) m.invoke(this.local);
        }
        else if ( this.defaults != null ) {
            name = (String) m.invoke(this.defaults);
        }
        else if ( this.enforced != null ) {
            name = (String) m.invoke(this.enforced);
        }
        return name;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format(
            "Inheritance invocation handler (local: %s, parent: %s, defaults: %s, types: %s)", //$NON-NLS-1$
            this.local,
            this.parent,
            this.defaults,
            this.context.getValueTypes());
    }


    private Object handleValue ( Method m ) throws IllegalAccessException, InvocationTargetException {
        if ( Collection.class.isAssignableFrom(m.getReturnType()) ) {
            return getValue(m, new CollectionIsSetStrategy());
        }

        return getValue(m, new PrimitiveIsSetStrategy());
    }


    private Object handleReference ( Method m ) throws IllegalAccessException, InvocationTargetException, ModelServiceException {
        if ( Collection.class.isAssignableFrom(m.getReturnType()) ) {
            return handleReferenceCollection(m);
        }

        return handleSingularReference(m);
    }


    @SuppressWarnings ( {
        "unchecked", "null"
    } )
    private Object handleSingularReference ( Method m ) throws IllegalAccessException, InvocationTargetException, ModelServiceException {
        if ( log.isTraceEnabled() ) {
            log.trace(String.format("Handling reference %s in %s", m.getName(), this)); //$NON-NLS-1$
        }

        Set<ValueTypes> types = EnumSet.copyOf(this.context.getValueTypes());
        PrimitiveIsSetStrategy isSet = new PrimitiveIsSetStrategy();
        log.trace("Getting local aspect"); //$NON-NLS-1$
        ConfigurationObject aspect = (ConfigurationObject) getValue(m, isSet, EnumSet.of(ValueTypes.LOCAL, ValueTypes.INHERITED));
        log.trace("Getting default aspect"); //$NON-NLS-1$
        ConfigurationObject defaultAspect = (ConfigurationObject) getValue(m, isSet, EnumSet.of(ValueTypes.DEFAULT));
        log.trace("Getting enforced aspect"); //$NON-NLS-1$
        ConfigurationObject enforcedAspect = (ConfigurationObject) getValue(m, isSet, EnumSet.of(ValueTypes.ENFORCED));
        log.trace("Done getting aspects"); //$NON-NLS-1$

        if ( !isSet.isSet(aspect, m, this.local) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Local value is empty in " + this.local); //$NON-NLS-1$
                if ( this.parent != null ) {
                    log.debug("Parent is " + this.parent); //$NON-NLS-1$
                }
            }

            if ( types.contains(ValueTypes.DEFAULT) && types.contains(ValueTypes.INHERITED) ) {
                types.add(ValueTypes.LOCAL);
            }
        }

        Class<@Nullable ? extends ConfigurationObject> aspectType = null;
        if ( aspect != null ) {
            // the aspect exists and specifies inheritance use standard inheritance
            aspectType = aspect.getType();
        }
        else {
            aspectType = (Class<@Nullable ? extends ConfigurationObject>) m.getReturnType();
        }

        if ( aspectType == null ) {
            throw new ModelServiceException("No type found for " + m); //$NON-NLS-1$
        }

        Object proxy = makeAspectProxy(aspect, aspectType, defaultAspect, enforcedAspect, this.context.getValueTypes());
        if ( log.isTraceEnabled() ) {
            log.trace("Created proxy" + proxy); //$NON-NLS-1$
        }
        return proxy;
    }


    @SuppressWarnings ( "unchecked" )
    private Object handleReferenceCollection ( Method m ) throws IllegalAccessException, InvocationTargetException, ModelServiceException {
        if ( log.isTraceEnabled() ) {
            log.trace(String.format("Handling reference collection %s in %s", m.getName(), this)); //$NON-NLS-1$
        }
        boolean isInherited = false;

        Set<ValueTypes> localTypes = EnumSet.copyOf(this.context.getValueTypes());
        localTypes.retainAll(EnumSet.of(ValueTypes.ENFORCED, ValueTypes.LOCAL));

        Collection<ConfigurationObject> aspects = (Collection<ConfigurationObject>) getValue(m, new CollectionIsSetStrategy(), localTypes);

        if ( aspects == null ) {
            log.debug("Using inherited aspects"); //$NON-NLS-1$
            Set<ValueTypes> inheritedTypes = EnumSet.copyOf(this.context.getValueTypes());
            inheritedTypes.retainAll(EnumSet.of(ValueTypes.INHERITED, ValueTypes.DEFAULT));
            aspects = (Collection<ConfigurationObject>) getValue(m, new CollectionIsSetStrategy(), inheritedTypes);
            isInherited = true;
        }

        Collection<ConfigurationObject> proxies = makeProxyCollection(m.getReturnType());

        if ( aspects == null ) {
            return proxies;
        }

        EnumSet<ValueTypes> valTypes = EnumSet.copyOf(this.context.getValueTypes());
        if ( isInherited && valTypes.contains(ValueTypes.DEFAULT) && valTypes.contains(ValueTypes.INHERITED) ) {
            valTypes.add(ValueTypes.LOCAL);
        }

        for ( ConfigurationObject aspect : aspects ) {
            Class<? extends ConfigurationObject> aspectType = aspect.getType();
            proxies.add(makeAspectProxy(aspect, aspectType, null, null, valTypes));
        }

        return proxies;
    }


    /**
     * @param returnType
     * @return
     * @throws ModelServiceException
     */
    private static Collection<ConfigurationObject> makeProxyCollection ( Class<?> returnType ) throws ModelServiceException {

        if ( List.class.isAssignableFrom(returnType) ) {
            return new LinkedList<>();
        }
        else if ( Set.class.isAssignableFrom(returnType) ) {
            return new HashSet<>();
        }

        throw new ModelServiceException("Unhandled collection type " + returnType.getName()); //$NON-NLS-1$
    }


    private <TAspect extends ConfigurationObject> ConfigurationObject makeAspectProxy ( TAspect aspect,
            @NonNull Class<@Nullable ? extends TAspect> aspectType, TAspect defaultAspect, TAspect enforcedAspect, EnumSet<ValueTypes> valTypes )
                    throws ModelServiceException {

        @Nullable
        TAspect useDefaults = getDefaultForReference(aspectType, defaultAspect);
        @Nullable
        TAspect useEnforced = getEnforcementForReference(aspectType, enforcedAspect);

        if ( log.isDebugEnabled() ) {
            log.debug(String.format(
                "Creating aspect proxy for %s with defaults %s and enforcement %s ( val types: %s)", //$NON-NLS-1$
                aspect,
                useDefaults,
                useEnforced,
                valTypes));
        }

        return this.context.getInheritanceProxyBuilder()
                .makeInheritanceProxy(this.context.withValueTypes(valTypes), aspectType, this.at, aspect, useDefaults, useEnforced);
    }


    private <TAspect extends ConfigurationObject> @Nullable TAspect getEnforcementForReference (
            @NonNull Class<@Nullable ? extends TAspect> aspectType, TAspect enforcedAspect ) throws ModelServiceException {
        TAspect useEnforced = enforcedAspect;
        if ( enforcedAspect == null && this.context.getValueTypes().contains(ValueTypes.ENFORCED) ) {
            log.debug("Making enforcement proxy"); //$NON-NLS-1$
            // if the enforcement does not define the reference, then structurally search for enforced object
            useEnforced = this.context.getInheritanceProxyBuilder()
                    .getEnforcementProxy(this.context, aspectType, (AbstractStructuralObjectImpl) this.at);
        }
        return useEnforced;
    }


    private <TAspect extends ConfigurationObject> @Nullable TAspect getDefaultForReference ( @NonNull Class<@Nullable ? extends TAspect> aspectType,
            TAspect defaultAspect ) throws ModelServiceException {
        TAspect useDefaults = defaultAspect;
        if ( defaultAspect == null && this.context.getValueTypes().contains(ValueTypes.DEFAULT) ) {
            log.debug("Making default proxy"); //$NON-NLS-1$
            // if the default does not define the reference, then structurally search for default object
            useDefaults = this.context.getInheritanceProxyBuilder().getDefaultProxy(this.context, aspectType, this.at);
        }
        return useDefaults;
    }


    private static Object unwrapProxy ( Object obj ) {
        Object cur = obj;
        if ( Proxy.isProxyClass(cur.getClass()) ) {
            InvocationHandler ih = Proxy.getInvocationHandler(cur);

            if ( ih instanceof InheritanceInvocationHandler ) {
                cur = ( (InheritanceInvocationHandler<@Nullable ?>) ih ).local;
            }
        }
        return cur;
    }


    private Object getValue ( Method m, IsSetStrategy isSet ) throws IllegalAccessException, InvocationTargetException {
        return this.getValue(m, isSet, this.context.getValueTypes());
    }


    private Object getValue ( Method m, IsSetStrategy isSet, Set<ValueTypes> t ) throws IllegalAccessException, InvocationTargetException {

        // 1. check structurally inherited enforced value
        if ( this.enforced != null && t.contains(ValueTypes.ENFORCED) ) {
            Object enforcedValue = m.invoke(this.enforced);
            if ( isSet.isSet(enforcedValue, m, this.enforced) ) {
                return doReturnEnforcedValue(enforcedValue);
            }
        }

        // 2. check local value
        if ( this.local != null && t.contains(ValueTypes.LOCAL) ) {
            Object localValue = m.invoke(this.local);

            if ( isSet.isSet(localValue, m, this.local) ) {
                return doReturnLocalValue(localValue);
            }
        }

        return getInheritedValue(m, isSet, t);
    }


    private Object getInheritedValue ( Method m, IsSetStrategy isSet, Set<ValueTypes> t ) throws IllegalAccessException, InvocationTargetException {
        // 3. check value obtained via aspect inheritance
        if ( this.parent != null && t.contains(ValueTypes.INHERITED) ) {
            Object aspectInheritanceValue = m.invoke(this.parent);

            if ( isSet.isSet(aspectInheritanceValue, m, this.parent) ) {
                return doReturnInheritedValue(aspectInheritanceValue);
            }
        }

        // 4. check structurally inherited default value
        if ( this.defaults != null && t.contains(ValueTypes.DEFAULT) ) {
            Object defaultValue = m.invoke(this.defaults);
            if ( isSet.isSet(defaultValue, m, this.defaults) ) {
                return doReturnDefaultValue(defaultValue);
            }
        }

        return null;
    }


    private static Object doReturnLocalValue ( Object localValue ) {
        if ( log.isTraceEnabled() ) {
            log.trace("Using local value " + localValue); //$NON-NLS-1$
        }
        return localValue;
    }


    private static Object doReturnEnforcedValue ( Object enforcedValue ) {
        if ( log.isTraceEnabled() ) {
            log.trace("Returning enforced value " + enforcedValue); //$NON-NLS-1$
        }
        return enforcedValue;
    }


    private static Object doReturnInheritedValue ( Object aspectInheritanceValue ) {
        if ( log.isTraceEnabled() ) {
            log.trace("Returning inherited value " + aspectInheritanceValue); //$NON-NLS-1$
        }
        return aspectInheritanceValue;
    }


    private static Object doReturnDefaultValue ( Object defaultValue ) {
        if ( log.isTraceEnabled() ) {
            log.trace("Returning default value " + defaultValue); //$NON-NLS-1$
        }
        return defaultValue;
    }

}
