/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.09.2015 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.util;


import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;


/**
 * @author mbechler
 *
 */
public class InheritanceProxyContext {

    private static final Logger log = Logger.getLogger(InheritanceProxyContext.class);

    private @NonNull EntityManager entityManager;
    private ClassLoader classLoader;
    private InheritanceProxyBuilder inheritanceProxyBuilder;
    private final EnumSet<ValueTypes> valueTypes;
    private InheritanceProxyCache inheritanceProxyCache;
    private Set<Class<? extends ConfigurationObject>> availableDefaultTypes = new HashSet<>();
    private Set<Class<? extends ConfigurationObject>> availableEnforcedTypes = new HashSet<>();

    private Class<? extends ConfigurationObject> rootObjectType;


    /**
     * @param em
     * @param cl
     * @param inheritanceProxyBuilder
     * @param valueTypes
     */
    InheritanceProxyContext ( @NonNull EntityManager em, ClassLoader cl, InheritanceProxyBuilder inheritanceProxyBuilder,
            EnumSet<ValueTypes> valueTypes, Class<? extends ConfigurationObject> rootObjectType ) {
        this.entityManager = em;
        this.classLoader = cl;
        this.inheritanceProxyBuilder = inheritanceProxyBuilder;
        this.valueTypes = valueTypes;
        this.inheritanceProxyCache = new InheritanceProxyCache();
        this.rootObjectType = rootObjectType;
    }


    /**
     * @param em
     * @param cl
     * @param inheritanceUtil
     */
    InheritanceProxyContext ( @NonNull EntityManager em, ClassLoader cl, InheritanceProxyBuilder inheritanceUtil,
            Class<? extends ConfigurationObject> rootObjectType ) {
        this(em, cl, inheritanceUtil, EnumSet.allOf(ValueTypes.class), rootObjectType);
    }


    /**
     * @return the classloader to use
     */
    public ClassLoader getClassLoader () {
        return this.classLoader;
    }


    /**
     * @return the entity manager to use
     */
    public @NonNull EntityManager getEntityManager () {
        return this.entityManager;
    }


    /**
     * @return the inheritanceProxyBuilder
     */
    public InheritanceProxyBuilder getInheritanceProxyBuilder () {
        return this.inheritanceProxyBuilder;
    }


    /**
     * @return the value types to return
     */
    public EnumSet<ValueTypes> getValueTypes () {
        return this.valueTypes;
    }


    /**
     * @param type
     * @return an adjusted inheritance proxy context
     */
    public @NonNull InheritanceProxyContext withValueType ( ValueTypes type ) {
        EnumSet<ValueTypes> pValueTypes = EnumSet.copyOf(this.valueTypes);
        pValueTypes.add(type);
        InheritanceProxyContext cc = new InheritanceProxyContext(
            getEntityManager(),
            getClassLoader(),
            getInheritanceProxyBuilder(),
            pValueTypes,
            this.rootObjectType);
        cc.availableDefaultTypes = this.availableDefaultTypes;
        cc.availableEnforcedTypes = this.availableEnforcedTypes;
        return cc;
    }


    /**
     * @param valTypes
     * @return an adjusted inheritance proxy context
     */
    public @NonNull InheritanceProxyContext withValueTypes ( EnumSet<ValueTypes> valTypes ) {
        InheritanceProxyContext cc = new InheritanceProxyContext(
            getEntityManager(),
            getClassLoader(),
            getInheritanceProxyBuilder(),
            valTypes,
            this.rootObjectType);
        cc.availableDefaultTypes = this.availableDefaultTypes;
        cc.availableEnforcedTypes = this.availableEnforcedTypes;
        return cc;
    }


    /**
     * @return the proxy cache
     */
    public InheritanceProxyCache getCache () {
        return this.inheritanceProxyCache;
    }


    /**
     * @param availableDefaultTypes
     */
    public void setAvailableDefaultTypes ( Set<Class<? extends ConfigurationObject>> availableDefaultTypes ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Available defaults " + availableDefaultTypes); //$NON-NLS-1$
        }
        this.availableDefaultTypes = availableDefaultTypes;
    }


    /**
     * 
     * @param type
     * @return whether any (non-global) defaults are applied for the type
     */
    public boolean haveDefaultsFor ( Class<? extends ConfigurationObject> type ) {
        return this.availableDefaultTypes.contains(type);
    }


    /**
     * @param availableEnforcedTypes
     */
    public void setAvailableEnforcedTypes ( Set<Class<? extends ConfigurationObject>> availableEnforcedTypes ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Available enforced " + availableEnforcedTypes); //$NON-NLS-1$
        }
        this.availableEnforcedTypes = availableEnforcedTypes;
    }


    /**
     * 
     * @param type
     * @return whether any (non-global) defaults are applied for the type
     */
    public boolean haveEnforcedFor ( Class<? extends ConfigurationObject> type ) {
        return this.availableEnforcedTypes.contains(type);
    }


    /**
     * 
     * @return the root object type
     */
    public Class<? extends ConfigurationObject> getRootObjectType () {
        return this.rootObjectType;
    }

}
