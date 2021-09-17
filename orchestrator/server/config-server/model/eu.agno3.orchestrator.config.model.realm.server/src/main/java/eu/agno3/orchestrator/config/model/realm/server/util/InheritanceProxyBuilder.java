/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.util;


import java.lang.reflect.Proxy;
import java.util.EnumSet;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeRegistry;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObjectReference;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;


/**
 * @author mbechler
 * 
 */
@Component ( service = InheritanceProxyBuilder.class )
public class InheritanceProxyBuilder {

    private static final Logger log = Logger.getLogger(InheritanceProxyBuilder.class);

    private ObjectTypeRegistry objectTypeRegistry;
    private ObjectPoolProvider objectPoolProvider;
    private PersistenceUtil persistenceUtil;


    /**
     * Test only
     * 
     * @param reg
     */
    @Reference
    public synchronized void setObjectTypeRegistry ( ObjectTypeRegistry reg ) {
        this.objectTypeRegistry = reg;
    }


    /**
     * Test only
     * 
     * @param reg
     */
    public synchronized void unsetObjectTypeRegistry ( ObjectTypeRegistry reg ) {
        if ( this.objectTypeRegistry == reg ) {
            this.objectTypeRegistry = reg;
        }
    }


    /**
     * Test only
     * 
     * @param opp
     */
    @Reference
    public synchronized void setObjectPoolProvider ( ObjectPoolProvider opp ) {
        this.objectPoolProvider = opp;
    }


    /**
     * Test only
     * 
     * @param opp
     */
    public synchronized void unsetObjectPoolProvider ( ObjectPoolProvider opp ) {
        if ( this.objectPoolProvider == opp ) {
            this.objectPoolProvider = null;
        }
    }


    /**
     * Test only
     * 
     * @param pu
     */
    @Reference
    public synchronized void setPersistenceUtil ( PersistenceUtil pu ) {
        this.persistenceUtil = pu;
    }


    /**
     * Test only
     * 
     * @param pu
     */
    public synchronized void unsetPersistenceUtil ( PersistenceUtil pu ) {
        if ( this.persistenceUtil == pu ) {
            this.persistenceUtil = null;
        }
    }


    /**
     * @param em
     * @param type
     * @param rootAnchor
     * @param rootType
     *            if null set to type
     * @return a new proxy context
     */
    public @NonNull InheritanceProxyContext makeProxyContext ( @NonNull EntityManager em, Class<? extends @Nullable ConfigurationObject> type,
            StructuralObject rootAnchor, Class<? extends @Nullable ConfigurationObject> rootType ) {
        InheritanceProxyContext ctx = new InheritanceProxyContext(em, type.getClassLoader(), this, rootType != null ? rootType : type);
        ctx.setAvailableDefaultTypes(this.objectPoolProvider.getAvailableDefaultTypes(em, rootAnchor));
        ctx.setAvailableEnforcedTypes(this.objectPoolProvider.getAvailableEnforcementTypes(em, rootAnchor));
        return ctx;
    }


    /**
     * 
     * @param cc
     * @param aspect
     * @return an inheritance proxy using the structurally applied defaults and enforcements
     * @throws ModelServiceException
     */
    @SuppressWarnings ( {
        "unchecked", "null"
    } )
    public <T extends ConfigurationObject> @NonNull T makeInheritanceProxy ( @NonNull InheritanceProxyContext cc, @NonNull T aspect )
            throws ModelServiceException {
        return (@NonNull T) makeInheritanceProxy(cc, aspect.getType(), aspect, getAnchor(aspect));
    }


    /**
     * @param cc
     * @param type
     * @param aspect
     * @param anchor
     * @return an inheritance proxy using the structurally applied defaults and enforcements
     * @throws ModelServiceException
     */
    public <T extends ConfigurationObject> @NonNull T makeInheritanceProxy ( InheritanceProxyContext cc, @NonNull Class<? extends T> type, T aspect,
            AbstractStructuralObjectImpl anchor ) throws ModelServiceException {
        return makeInheritanceProxy(
            cc,
            type,
            anchor,
            aspect,
            cc.getValueTypes().contains(ValueTypes.DEFAULT) ? getDefaultProxy(cc, type, anchor) : null,
            cc.getValueTypes().contains(ValueTypes.ENFORCED) ? getEnforced(cc, type, aspect) : null);
    }


    /**
     * @param em
     * @param cl
     * @param type
     * @param at
     * @param aspect
     * @param outer
     * @param defaults
     * @param enforced
     * @param valueTypes
     * @return a inheritance proxy
     * @throws ModelServiceException
     */
    @SuppressWarnings ( "unchecked" )
    protected <T extends ConfigurationObject> @NonNull T makeInheritanceProxy ( @NonNull InheritanceProxyContext cc, @NonNull Class<? extends T> type,
            StructuralObject at, T aspect, @Nullable T defaults, @Nullable T enforced ) throws ModelServiceException {

        if ( aspect != null && !type.isAssignableFrom(aspect.getType()) ) {
            throw new IllegalArgumentException("Wrong type"); //$NON-NLS-1$
        }

        if ( log.isTraceEnabled() ) {
            log.trace("Creating inheritance proxy for " + aspect); //$NON-NLS-1$
            log.trace("With value types " + cc.getValueTypes()); //$NON-NLS-1$
            log.trace("With defaults " + defaults); //$NON-NLS-1$
            log.trace("With enforcement " + enforced); //$NON-NLS-1$
        }

        @Nullable
        T parent = makeParent(cc, type, aspect);
        InheritanceInvocationHandler<@Nullable T> invocationHandler = new InheritanceInvocationHandler<>(
            cc,
            at,
            enforced,
            aspect,
            parent,
            defaults,
            type);

        T proxy = (T) Proxy.newProxyInstance(cc.getClassLoader(), new Class[] {
            type
        }, invocationHandler);

        if ( proxy == null ) {
            throw new ModelServiceException();
        }

        return proxy;
    }


    @SuppressWarnings ( {
        "unchecked"
    } )
    private <@Nullable T extends ConfigurationObject> T makeParent ( @NonNull InheritanceProxyContext cc, @NonNull Class<? extends T> type, T aspect )
            throws ModelServiceException {
        if ( aspect != null && aspect.getInherits() != null ) {
            log.trace("Making parent"); //$NON-NLS-1$
            ConfigurationObject inherits = aspect.getInherits();

            if ( type.isAssignableFrom(inherits.getType()) ) {
                inherits = PersistenceUtil.unproxyDeep((AbstractConfigurationObject<?>) inherits);
            }
            else if ( aspect.getInherits() instanceof ConfigurationObjectReference ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Fetching inherits reference" + aspect.getInherits()); //$NON-NLS-1$
                }

                AbstractConfigurationObject<ConfigurationObject> realInherits;
                try {
                    realInherits = this.persistenceUtil.fetch(cc.getEntityManager(), inherits);
                }
                catch ( ModelObjectNotFoundException e ) {
                    throw new ModelServiceException("Failed to fetch inherits reference", e); //$NON-NLS-1$
                }

                inherits = PersistenceUtil.unproxyDeep(realInherits);
            }
            else {
                return null;
            }

            if ( log.isTraceEnabled() ) {
                log.trace("Creating inheritance proxy for inherited aspect " + inherits); //$NON-NLS-1$
            }
            log.trace("Parent created"); //$NON-NLS-1$
            return (T) makeInheritanceProxy(cc.withValueType(ValueTypes.LOCAL), inherits.getType(), inherits, getAnchor(inherits));
        }
        if ( log.isDebugEnabled() ) {
            log.debug("Does not have parent " + aspect); //$NON-NLS-1$
        }
        return null;
    }


    /**
     * @param cc
     * @param type
     * @param obj
     * @return the enforced values for the given object
     * @throws ModelServiceException
     */

    public <@Nullable T extends ConfigurationObject> @Nullable T getEnforced ( @NonNull InheritanceProxyContext cc, @NonNull Class<? extends T> type,
            T obj ) throws ModelServiceException {
        if ( obj != null ) {
            AbstractStructuralObjectImpl anchor = getAnchor(obj);
            if ( anchor != null ) {
                return getEnforcementProxy(cc, type, anchor);
            }
        }

        return null;
    }


    /**
     * @param cc
     * @param type
     * @param at
     * @return an inheritance proxy resolving the enforcments for type at the given structural anchor
     * @throws ModelServiceException
     */

    public <T extends ConfigurationObject> @NonNull T getEnforcementProxy ( @NonNull InheritanceProxyContext cc, @NonNull Class<? extends T> type,
            AbstractStructuralObjectImpl at ) throws ModelServiceException {

        try {
            if ( !cc.haveEnforcedFor(type) ) {
                log.debug("Not having any enforcements for type " + type); //$NON-NLS-1$
                return this.objectTypeRegistry.getConcrete(type).newInstance();
            }

            CacheKey k = new CacheKey(type, at.getId());
            @SuppressWarnings ( "unchecked" )
            T cached = (T) cc.getCache().getEnforcementProxy(k);
            if ( cached != null ) {
                return cached;
            }

            T enforced = fetchEnforced(cc, type, at);
            T proxy = makeInheritanceProxy(cc.withValueTypes(EnumSet.complementOf(EnumSet.of(ValueTypes.DEFAULT))), type, at, enforced, null, null);
            cc.getCache().putEnforcementProxy(k, proxy);
            return proxy;

        }
        catch (
            InstantiationException |
            IllegalAccessException e ) {
            throw new ModelServiceException("Failed to create empty object", e); //$NON-NLS-1$
        }

    }


    /**
     * @param cc
     * @param type
     * @param at
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ModelServiceException
     */
    @SuppressWarnings ( "unchecked" )
    private <T extends ConfigurationObject> T fetchEnforced ( InheritanceProxyContext cc, Class<? extends T> type, AbstractStructuralObjectImpl at )
            throws InstantiationException, IllegalAccessException, ModelServiceException {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Getting enforcement for type %s at %s", type.getName(), at)); //$NON-NLS-1$
        }

        AbstractConfigurationObject<T> empty = (AbstractConfigurationObject<T>) this.objectTypeRegistry.getConcrete(type).newInstance();

        AbstractConfigurationObject<T> inheritedEnforcement = this.objectPoolProvider
                .fetchInheritedEnforcement(cc.getEntityManager(), type, at, empty);
        return (T) PersistenceUtil.unproxyDeep(inheritedEnforcement);

    }


    /**
     * @param cc
     * @param type
     * @param at
     * @return the defaults applied for the type
     * @throws ModelServiceException
     */

    public <@Nullable T extends ConfigurationObject> @NonNull T getDefaultProxy ( @NonNull InheritanceProxyContext cc,
            @NonNull Class<? extends T> type, StructuralObject at ) throws ModelServiceException {

        CacheKey k = new CacheKey(type, at.getId());
        @SuppressWarnings ( "unchecked" )
        T cached = (T) cc.getCache().getDefaultsProxy(k);
        if ( cached != null ) {
            return cached;
        }

        T globalDefaults = null;

        if ( this.objectTypeRegistry.getConcrete(cc.getRootObjectType()).hasOverrideDefaultsFor(type) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Using overridden global defaults for " + type); //$NON-NLS-1$
            }
            globalDefaults = this.objectTypeRegistry.getConcrete(cc.getRootObjectType()).getOverrideDefaults(type);
        }
        else {
            globalDefaults = this.objectTypeRegistry.getConcrete(type).getGlobalDefaults();
        }
        if ( !cc.haveDefaultsFor(type) ) {
            log.debug("Not having any defaults for type " + type); //$NON-NLS-1$
            T proxy = makeInheritanceProxy(
                cc.withValueTypes(EnumSet.complementOf(EnumSet.of(ValueTypes.ENFORCED))),
                type,
                at,
                globalDefaults,
                globalDefaults,
                null);

            cc.getCache().putDefaultsProxy(k, proxy);
            return proxy;
        }

        T defaults = fetchDefaults(cc, k, type, at, globalDefaults);
        if ( log.isDebugEnabled() ) {
            log.debug("Creating default proxy with defaults " + defaults); //$NON-NLS-1$
        }
        T proxy = makeInheritanceProxy(
            cc.withValueTypes(EnumSet.complementOf(EnumSet.of(ValueTypes.ENFORCED))),
            type,
            at,
            defaults,
            globalDefaults,
            null);

        cc.getCache().putDefaultsProxy(k, proxy);
        log.debug("Default proxy created"); //$NON-NLS-1$
        return proxy;
    }


    /**
     * @param cc
     * @param type
     * @param at
     * @param globalDefaults
     * @return
     * @throws ModelServiceException
     */
    @SuppressWarnings ( "unchecked" )
    private <@Nullable T extends ConfigurationObject> @NonNull T fetchDefaults ( @NonNull InheritanceProxyContext cc, CacheKey k,
            @NonNull Class<? extends T> type, StructuralObject at, T globalDefaults ) throws ModelServiceException {

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Getting defaults for type %s at %s", type.getName(), at)); //$NON-NLS-1$
        }

        AbstractConfigurationObject<@NonNull T> fetchInheritedDefault = this.objectPoolProvider
                .fetchInheritedDefault(cc.getEntityManager(), type, at, (AbstractConfigurationObject<@NonNull T>) globalDefaults);

        return (T) PersistenceUtil.unproxyDeep(fetchInheritedDefault);

    }


    private static <T extends ConfigurationObject> AbstractStructuralObjectImpl getAnchor ( T obj ) {
        return ( (AbstractConfigurationObject<?>) obj ).getAnchor();
    }

}
