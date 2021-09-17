/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.util;


import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.groups.Default;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectValidationException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.base.exceptions.faults.ModelObjectValidationFault;
import eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectValidationException;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectType;
import eu.agno3.orchestrator.config.model.realm.validation.ObjectValidator;
import eu.agno3.orchestrator.config.model.realm.validation.ValidatorRegistry;
import eu.agno3.orchestrator.config.model.validation.Abstract;
import eu.agno3.orchestrator.config.model.validation.Instance;
import eu.agno3.orchestrator.config.model.validation.Materialized;
import eu.agno3.orchestrator.config.model.validation.ViolationEntry;
import eu.agno3.orchestrator.config.model.validation.ViolationEntryImpl;
import eu.agno3.orchestrator.config.model.validation.ViolationLevel;
import eu.agno3.runtime.validation.ValidConditional;
import eu.agno3.runtime.validation.ValidationELContext;


/**
 * @author mbechler
 * 
 */
@Component ( service = ModelObjectValidationUtil.class )
public class ModelObjectValidationUtil {

    private static final Logger log = Logger.getLogger(ModelObjectValidationUtil.class);

    private ObjectPoolProvider objectPoolProvider;

    private DefaultServerServiceContext sctx;

    private InheritanceProxyBuilder inheritanceProxyBuilder;

    private ValidatorRegistry validatorRegistry;

    private PersistenceUtil persistenceUtil;

    private ExpressionFactory expressionFactory;

    private Validator validator;


    /**
     * 
     */
    public ModelObjectValidationUtil () {
        this.expressionFactory = ExpressionFactory.newInstance();
    }


    @Reference
    protected synchronized void setContext ( DefaultServerServiceContext ctx ) {
        this.sctx = ctx;
        this.validator = this.sctx.getValidatorFactory().getValidator();
    }


    protected synchronized void unsetContext ( DefaultServerServiceContext ctx ) {
        if ( this.sctx == ctx ) {
            this.validator = null;
            this.sctx = null;
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
    protected synchronized void unsetObjectPoolProvider ( ObjectPoolProvider opp ) {
        if ( this.objectPoolProvider == opp ) {
            this.objectPoolProvider = null;
        }
    }


    @Reference
    protected synchronized void setInheritanceProxyBuilder ( InheritanceProxyBuilder ipb ) {
        this.inheritanceProxyBuilder = ipb;
    }


    protected synchronized void unsetInheritanceProxyBuilder ( InheritanceProxyBuilder ipb ) {
        if ( this.inheritanceProxyBuilder == ipb ) {
            this.inheritanceProxyBuilder = null;
        }
    }


    @Reference
    protected synchronized void setValidatorRegistry ( ValidatorRegistry reg ) {
        this.validatorRegistry = reg;
    }


    protected synchronized void unsetValidatorRegistry ( ValidatorRegistry reg ) {
        if ( this.validatorRegistry == reg ) {
            this.validatorRegistry = null;
        }
    }


    @Reference
    protected synchronized void setPersistenceUtil ( PersistenceUtil pu ) {
        this.persistenceUtil = pu;
    }


    protected synchronized void unsetPersistenceUtil ( PersistenceUtil pu ) {
        if ( this.persistenceUtil == pu ) {
            this.persistenceUtil = null;
        }
    }


    /**
     * @param obj
     * @return whether the object is anonymous
     */
    public static boolean isAnonymous ( ConfigurationObject obj ) {
        return obj.getName() == null;
    }


    /**
     * 
     * @param ctx
     * @param obj
     * @throws ModelObjectException
     * @throws ModelServiceException
     */
    public void validateObjectIntegrity ( ReferenceWalkerContext ctx, AbstractConfigurationObject<?> obj )
            throws ModelServiceException, ModelObjectException {
        ReferenceWalker.walk(ctx, obj, new RecursiveReferenceVisitor(new ValidatingReferenceVisitor(this)));
    }


    /**
     * @param ctx
     * @param val
     * @throws ObjectValidationException
     */
    public void validateSingleObject ( ReferenceWalkerContext ctx, AbstractConfigurationObject<?> val ) throws ObjectValidationException {
        if ( log.isDebugEnabled() ) {
            log.debug("Validating object " + val); //$NON-NLS-1$
        }
        validateAnonymity(val);
        validateInheritance(ctx, val);
        validateUsage(ctx, val);
    }


    private static void validateAnonymity ( AbstractConfigurationObject<?> obj ) throws ObjectValidationException {
        StructuralObject anchor = obj.getAnchor();
        if ( isAnonymous(obj) && anchor.getType() != StructuralObjectType.SERVICE && obj.getOuterObject() == null ) {
            throw new ObjectValidationException("Object is anonymous but neither a sub object nor per-service" + obj); //$NON-NLS-1$
        }

        if ( !isAnonymous(obj) && anchor.getType() == StructuralObjectType.SERVICE ) {
            throw new ObjectValidationException("Object is per-service but not anonymous " + obj); //$NON-NLS-1$
        }
    }


    private static void validateUsage ( ReferenceWalkerContext ctx, AbstractConfigurationObject<?> obj ) throws ObjectValidationException {
        validateUsedBy(ctx, obj);
        validateUses(ctx, obj);
    }


    private static void validateUses ( ReferenceWalkerContext ctx, AbstractConfigurationObject<?> obj ) throws ObjectValidationException {

        if ( ctx instanceof PersistentReferenceWalkerContext ) {
            Query query = ( (PersistentReferenceWalkerContext) ctx ).getEnityManager()
                    .createQuery("SELECT uses.id, uses.name, uses.outerObject.id, COUNT(inverse.id) FROM AbstractConfigurationObject config " + //$NON-NLS-1$
                            "INNER JOIN config.uses uses  LEFT JOIN uses.usedBy inverse ON inverse.id = config.id " + //$NON-NLS-1$
                            "WHERE config.id = :id GROUP BY uses.id, uses.name, uses.outerObject.id"); //$NON-NLS-1$

            query.setParameter("id", obj.getId()); //$NON-NLS-1$

            List<Object[]> res = query.getResultList();
            for ( Object[] uses : res ) {
                UUID usesId = (UUID) uses[ 0 ];
                String usesName = (String) uses[ 1 ];
                UUID usesOuter = (UUID) uses[ 2 ];
                long inverseCount = (long) uses[ 3 ];
                if ( inverseCount != 1 ) {
                    logAndThrow(String.format("Object %s declares uses relalation on %s but inverse reference is not set", obj, usesId)); //$NON-NLS-1$
                }

                if ( !ModelObjectValidationUtil.isAnonymous(obj) && StringUtils.isBlank(usesName) && !obj.getId().equals(usesOuter) ) {
                    logAndThrow(String.format(
                        "Named object %s references anonymous object %s that is not a subobject (outer: %s)", //$NON-NLS-1$
                        obj,
                        usesId,
                        usesOuter));
                }
            }
        }
        else {
            for ( ConfigurationObject uses : obj.getUses() ) {
                Set<ConfigurationObject> usedBy = new HashSet<>( ( (AbstractConfigurationObject<?>) uses ).getUsedBy());
                if ( !usedBy.contains(obj) ) {
                    logAndThrow(String.format("Object %s declares uses relalation on %s but inverse reference is not set", obj, uses)); //$NON-NLS-1$
                }

                if ( isIllegalUsesReference(obj, uses) ) {
                    logAndThrow(String.format(
                        "Named object %s references anonymous object %s that is not a subobject (outer: %s)", //$NON-NLS-1$
                        obj,
                        uses,
                        ( (AbstractConfigurationObject<?>) uses ).getOuterObject()));
                }
            }
        }
    }


    private static boolean isIllegalUsesReference ( AbstractConfigurationObject<?> obj, ConfigurationObject uses ) {
        return !ModelObjectValidationUtil.isAnonymous(obj) && ModelObjectValidationUtil.isAnonymous(uses)
                && !obj.equals( ( (AbstractConfigurationObject<?>) uses ).getOuterObject());
    }


    private static void validateUsedBy ( ReferenceWalkerContext ctx, AbstractConfigurationObject<?> obj ) throws ObjectValidationException {
        if ( ctx instanceof PersistentReferenceWalkerContext ) {
            Query query = ( (PersistentReferenceWalkerContext) ctx ).getEnityManager()
                    .createQuery("SELECT usedBy.id, usedBy.name, COUNT(inverse.id) FROM AbstractConfigurationObject config " //$NON-NLS-1$
                            + "INNER JOIN config.usedBy usedBy LEFT JOIN usedBy.uses inverse ON inverse.id = config.id " //$NON-NLS-1$
                            + "WHERE config.id = :id GROUP BY usedBy.id, usedBy.name"); //$NON-NLS-1$

            query.setParameter("id", obj.getId()); //$NON-NLS-1$

            List<Object[]> res = query.getResultList();
            for ( Object[] usedBy : res ) {
                UUID usedById = (UUID) usedBy[ 0 ];
                String usedByName = (String) usedBy[ 1 ];
                long inverseCount = (long) usedBy[ 2 ];
                if ( inverseCount != 1 ) {
                    logAndThrow(String.format("Object %s declares usedBy relalation on %s but inverse reference is not set", obj, usedById)); //$NON-NLS-1$
                }

                if ( !StringUtils.isBlank(usedByName) && ModelObjectValidationUtil.isAnonymous(obj)
                        && !obj.getOuterObject().getId().equals(usedById) ) {
                    logAndThrow(String.format(
                        "Named object %s references anonymous object %s but is not a subobject (outer: %s)", //$NON-NLS-1$
                        usedByName,
                        obj,
                        obj.getOuterObject()));
                }
            }
        }
        else {
            for ( ConfigurationObject usedBy : obj.getUsedBy() ) {
                Set<ConfigurationObject> uses = new HashSet<>( ( (AbstractConfigurationObject<?>) usedBy ).getUses());
                if ( !uses.contains(obj) ) {
                    logAndThrow(String.format("Object %s declares usedBy relalation on %s but inverse reference is not set", obj, uses)); //$NON-NLS-1$
                }

                if ( isIllegalUsedByReference(obj, usedBy) ) {
                    logAndThrow(String.format(
                        "Named object %s references anonymous object %s but is not a subobject (outer: %s)", //$NON-NLS-1$
                        usedBy,
                        obj,
                        obj.getOuterObject()));
                }
            }
        }
    }


    private static boolean isIllegalUsedByReference ( AbstractConfigurationObject<?> obj, ConfigurationObject usedBy ) {
        return !ModelObjectValidationUtil.isAnonymous(usedBy) && ModelObjectValidationUtil.isAnonymous(obj) && !obj.getOuterObject().equals(usedBy);
    }


    private static void logAndThrow ( String err ) throws ObjectValidationException {
        log.warn(err);
        throw new ObjectValidationException(err);
    }


    /**
     * @param ctx
     * @param obj
     * @throws ObjectValidationException
     */
    private void validateInheritance ( ReferenceWalkerContext ctx, AbstractConfigurationObject<?> obj ) throws ObjectValidationException {
        Set<ConfigurationObject> seen = new HashSet<>();
        seen.add(obj);
        validateInheritance(obj, seen, obj.getType());
    }


    /**
     * @param obj
     * @param seen
     * @param objType
     * @throws ObjectValidationException
     */
    private void validateInheritance ( AbstractConfigurationObject<?> obj, Set<ConfigurationObject> seen,
            Class<? extends ConfigurationObject> objType ) throws ObjectValidationException {

        validateObjectType(obj, objType);

        if ( obj.getInherits() == null ) {
            return;
        }

        AbstractConfigurationObject<?> inherits = (AbstractConfigurationObject<?>) obj.getInherits();
        checkInherits(obj, seen, inherits);

        seen.add(inherits);
        validateInheritance(inherits, seen, objType);
    }


    private void checkInherits ( AbstractConfigurationObject<?> obj, Set<ConfigurationObject> seen, AbstractConfigurationObject<?> inherits )
            throws ObjectValidationException {
        if ( isAnonymous(inherits) && inherits.getOuterObject() == null ) {
            throw new ObjectValidationException("Cannot inherit from an anonymous object " + inherits); //$NON-NLS-1$
        }

        if ( !this.objectPoolProvider.isInScope(obj, inherits) ) {
            throw new ObjectValidationException("Inherited object is not in scope of base object:" + inherits); //$NON-NLS-1$
        }

        if ( seen.contains(inherits) ) {
            throw new ObjectValidationException("Cycle in inheritance hierarchy"); //$NON-NLS-1$
        }
    }


    private static void validateObjectType ( AbstractConfigurationObject<?> obj, Class<? extends ConfigurationObject> objType )
            throws ObjectValidationException {
        if ( !objType.isAssignableFrom(obj.getType()) ) {
            throw new ObjectValidationException(
                String.format("Object type inconsistent, %s cannot inherit from %s", objType.getName(), obj.getType().getName())); //$NON-NLS-1$
        }
    }


    /**
     * Validate object integrity and values (bean validation)
     * 
     * @param em
     * 
     * @param object
     * @param contextServices
     * @throws ModelServiceException
     * @throws ModelObjectException
     */
    public <T extends AbstractConfigurationObject<?>> void validateObject ( @NonNull EntityManager em, T object,
            Map<ServiceStructuralObject, ConfigurationInstance> contextServices ) throws ModelServiceException, ModelObjectException {
        PersistenceUtil pu = this.persistenceUtil;
        if ( pu == null ) {
            throw new ModelServiceException();
        }
        validateObjectIntegrity(new PersistentReferenceWalkerContext(em, pu), object);
        List<ViolationEntry> violations = validateObject(em, object, object.getAnchor(), contextServices);

        if ( log.isDebugEnabled() ) {
            log.debug("Have violations " + violations); //$NON-NLS-1$
        }

        boolean haveErrors = false;
        for ( ViolationEntry e : violations ) {
            if ( e.getLevel() == ViolationLevel.ERROR ) {
                haveErrors = true;
            }
        }

        if ( haveErrors ) {
            throw new ModelObjectValidationException(
                "Object validation failed", //$NON-NLS-1$
                new ModelObjectValidationFault(object.getType(), object.getId(), violations));
        }
    }


    /**
     * @param em
     * @param object
     * @param anchor
     * @param contextServices
     * @return violations of this object
     * @throws ModelServiceException
     * @throws ModelObjectException
     * @throws ModelObjectValidationException
     */
    public <T extends ConfigurationObject> List<ViolationEntry> validateObject ( @NonNull EntityManager em, T object,
            AbstractStructuralObjectImpl anchor, Map<ServiceStructuralObject, ConfigurationInstance> contextServices )
                    throws ModelServiceException, ModelObjectException {
        boolean abstr = true;
        if ( anchor instanceof ServiceStructuralObject ) {
            abstr = false;
        }

        ConfigurationObject materialized = makeMaterializedProxy(em, object, anchor);

        return validateEffective(object, abstr, materialized, contextServices);
    }


    /**
     * @param object
     * @param abstr
     * @param materialized
     * @param contextServices
     * @return violations
     * @throws ModelServiceException
     */
    public <T extends ConfigurationObject> List<ViolationEntry> validateEffective ( T object, boolean abstr, ConfigurationObject materialized,
            Map<ServiceStructuralObject, ConfigurationInstance> contextServices ) throws ModelServiceException {
        List<ViolationEntry> entries = new ArrayList<>();
        entries.addAll(validateValues(this.validator, object, abstr, materialized));
        entries.addAll(validateObjectValue(materialized, abstr, contextServices));
        return entries;
    }


    private <T extends ConfigurationObject> List<ViolationEntry> validateValues ( Validator v, T object, boolean abstr,
            ConfigurationObject materialized ) throws ModelServiceException {

        if ( !abstr ) {
            List<ViolationEntry> res = new ArrayList<>();
            log.debug("Validating configuration instance"); //$NON-NLS-1$
            res.addAll(validateBean(object, v, new Class<?>[] {
                Default.class, Instance.class
            }));

            log.debug("Validating materialized instance"); //$NON-NLS-1$
            res.addAll(validateBean(materialized, v, new Class<?>[] {
                Default.class, Materialized.class
            }));

            return res;

        }

        log.debug("Validating template configuration"); //$NON-NLS-1$
        return validateBean(object, v, new Class<?>[] {
            Default.class, Abstract.class
        });
    }


    /**
     * @param em
     * @param obj
     * @param persistentAnchor
     * @return
     * @throws ModelServiceException
     */
    private <T extends ConfigurationObject> ConfigurationObject makeMaterializedProxy ( @NonNull EntityManager em, T obj,
            AbstractStructuralObjectImpl persistentAnchor ) throws ModelServiceException {
        return this.inheritanceProxyBuilder.makeInheritanceProxy(
            new InheritanceProxyContext(
                em,
                obj.getClass().getClassLoader(),
                this.inheritanceProxyBuilder,
                EnumSet.allOf(ValueTypes.class),
                obj.getType()),
            obj.getType(),
            obj,
            persistentAnchor);
    }


    private <T extends ConfigurationObject> List<ViolationEntry> validateBean ( T object, Validator v, Class<?>[] groups )
            throws ModelServiceException {
        if ( log.isDebugEnabled() ) {
            log.debug("Bean validation on " + object.getType().getName()); //$NON-NLS-1$
        }
        Set<ConstraintViolation<T>> violations = v.validate(object, groups);

        if ( log.isDebugEnabled() ) {
            log.debug("Violations are: " + violations); //$NON-NLS-1$
        }

        String objectTypeName = this.sctx.getObjectTypeRegistry().get(object.getType()).getObjectTypeName();

        List<ViolationEntry> res = new ArrayList<>();
        for ( ConstraintViolation<T> violation : violations ) {
            res.add(ViolationEntryImpl.fromConstraintViolation(violation, objectTypeName));
        }
        return res;
    }


    /**
     * @param effectiveProxy
     * @param contextServices
     * @return
     * @throws ModelServiceException
     */
    private List<ViolationEntry> validateObjectValue ( ConfigurationObject effectiveProxy, boolean abst,
            Map<ServiceStructuralObject, ConfigurationInstance> contextServices ) throws ModelServiceException {
        List<ViolationEntry> violations = new ArrayList<>();
        validateRecursive(effectiveProxy, makeRootContext(effectiveProxy, abst, violations, contextServices));
        return violations;
    }


    /**
     * @param effectiveProxy
     * @param violations
     * @param contextServices
     * @return
     */
    private static ObjectValidationContextImpl makeRootContext ( ConfigurationObject effectiveProxy, boolean abst, List<ViolationEntry> violations,
            Map<ServiceStructuralObject, ConfigurationInstance> contextServices ) {
        return new ObjectValidationContextImpl(effectiveProxy, abst, violations, contextServices);
    }


    /**
     * @param obj
     * @param violations
     * @throws ModelServiceException
     */
    private void validateRecursive ( ConfigurationObject obj, ObjectValidationContextImpl ctx ) throws ModelServiceException {
        Class<? extends ConfigurationObject> type = obj.getType();
        this.validateSingleObject(obj, ctx);
        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(type);
        }
        catch ( IntrospectionException e ) {
            throw new ModelServiceException("Failed to introspect object", e); //$NON-NLS-1$
        }

        for ( PropertyDescriptor property : beanInfo.getPropertyDescriptors() ) {

            validateProperty(obj, ctx, type, property);
        }
    }


    /**
     * @param obj
     * @param ctx
     * @param type
     * @param property
     * @throws ModelServiceException
     */
    private void validateProperty ( ConfigurationObject obj, ObjectValidationContextImpl ctx, Class<? extends ConfigurationObject> type,
            PropertyDescriptor property ) throws ModelServiceException {
        Method rm = property.getReadMethod();
        ReferencedObject reference = ReflectionUtil.getReference(type, rm);
        if ( reference == null ) {
            return;
        }

        Valid valid = rm.getAnnotation(Valid.class);
        ValidConditional validCond = rm.getAnnotation(ValidConditional.class);

        if ( valid == null && validCond == null ) {
            log.debug(String.format("Skipping property %s as no nested validation is specified", property.getName())); //$NON-NLS-1$
            return;
        }
        else if ( valid == null && validCond != null ) {
            if ( log.isTraceEnabled() ) {
                log.trace(String.format("Need to evaluate property %s condition %s", property.getName(), validCond.when())); //$NON-NLS-1$
            }

            ValidationELContext vel = new ValidationELContext(obj);

            try {
                ValueExpression ve = this.expressionFactory.createValueExpression(vel, validCond.when(), Object.class);

                if ( !isTrue(ve.getValue(vel)) ) {
                    log.debug(String.format("Skipping property validation as condition %s is false: %s", validCond.when(), property.getName())); //$NON-NLS-1$
                    return;
                }
            }
            catch ( Exception e ) {
                log.warn(String.format("Failed to evaluate condition %s on %s", validCond.when(), obj.getType().getName()), e); //$NON-NLS-1$
            }
        }

        try {
            Object o = rm.invoke(obj);
            if ( o instanceof Collection<?> ) {
                validateCollectionItems(ctx, property, o);
            }
            else if ( o instanceof ConfigurationObject ) {
                ConfigurationObject cfgItm = (ConfigurationObject) o;
                validateRecursive(cfgItm, ctx.makeReferenceContext(property.getName(), cfgItm));
            }
            else if ( o == null ) {
                return;
            }
            else {
                throw new ModelServiceException("Referenced value is not a ConfigurationObject " + o); //$NON-NLS-1$
            }
        }
        catch (
            IllegalAccessException |
            IllegalArgumentException |
            InvocationTargetException e ) {
            log.warn("Failed to retrieve property value", e); //$NON-NLS-1$
            throw new ModelServiceException("Failed to retrieve reference value", e); //$NON-NLS-1$
        }
    }


    /**
     * @param testValue
     * @return
     */
    private static boolean isTrue ( Object testValue ) {
        if ( testValue == null ) {
            return false;
        }

        if ( Boolean.class == testValue.getClass() || boolean.class == testValue.getClass() ) {
            return (boolean) testValue;
        }

        return false;
    }


    /**
     * @param ctx
     * @param property
     * @param o
     * @throws ModelServiceException
     */
    private void validateCollectionItems ( ObjectValidationContextImpl ctx, PropertyDescriptor property, Object o ) throws ModelServiceException {
        for ( Object itm : (Collection<?>) o ) {
            if ( ! ( itm instanceof ConfigurationObject ) ) {
                throw new ModelServiceException("Referenced collection value is not a ConfigurationObject" + itm); //$NON-NLS-1$
            }

            ConfigurationObject cfgItm = (ConfigurationObject) itm;
            validateRecursive(cfgItm, ctx.makeCollectionReferenceContext(property.getName(), cfgItm));
        }
    }


    /**
     * @param obj
     * @param violations
     * @throws ModelServiceException
     */
    @SuppressWarnings ( "unchecked" )
    private <T extends ConfigurationObject> void validateSingleObject ( T obj, ObjectValidationContextImpl ctx ) throws ModelServiceException {
        if ( log.isTraceEnabled() ) {
            log.trace("Validating " + obj.getType().getName()); //$NON-NLS-1$
            log.trace("Property path " + StringUtils.join(ctx.getPath(), '.')); //$NON-NLS-1$
        }
        List<ObjectValidator<? super T>> validators = this.getValidators((Class<T>) obj.getType());
        for ( ObjectValidator<? super T> v : validators ) {
            try {
                if ( log.isDebugEnabled() ) {
                    log.debug("Validating using validator " + v.getClass().getName()); //$NON-NLS-1$
                }
                v.validate(ctx, obj);
            }
            catch ( Exception e ) {
                throw new ModelServiceException("Validator threw exception", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param type
     * @return
     */
    private <T extends ConfigurationObject> List<ObjectValidator<? super T>> getValidators ( Class<T> type ) {
        return this.validatorRegistry.getValidators(type);
    }

}
