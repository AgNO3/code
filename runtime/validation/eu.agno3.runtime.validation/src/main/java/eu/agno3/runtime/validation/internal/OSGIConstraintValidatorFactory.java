/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.10.2013 by mbechler
 */
package eu.agno3.runtime.validation.internal;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;

import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.runtime.validation.ValidatorFactory;


/**
 * @author mbechler
 * 
 */
@Component ( service = ConstraintValidatorFactory.class )
public class OSGIConstraintValidatorFactory implements ConstraintValidatorFactory {

    private static final Logger log = Logger.getLogger(OSGIConstraintValidatorFactory.class);

    private final Map<Class<?>, eu.agno3.runtime.validation.ConstraintValidatorFactory<?>> validatorMap = new ConcurrentHashMap<>();


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindConstraintValidatorFactory ( eu.agno3.runtime.validation.ConstraintValidatorFactory<?> factory ) {
        ValidatorFactory annot = getValidatorFactoryAnnotation(factory);

        if ( annot == null ) {
            return;
        }

        if ( this.validatorMap.containsKey(annot.value()) ) {
            log.warn("An ConstraintValidatorFactory is already registered for the ConstraintValidator " + annot.value().getName()); //$NON-NLS-1$
        }

        this.validatorMap.put(annot.value(), factory);

    }


    protected synchronized void unbindConstraintValidatorFactory ( eu.agno3.runtime.validation.ConstraintValidatorFactory<?> factory ) {
        ValidatorFactory annot = getValidatorFactoryAnnotation(factory);

        if ( annot == null ) {
            return;
        }

        if ( factory == this.validatorMap.get(annot.value()) ) {
            this.validatorMap.remove(annot.value());
        }
    }


    /**
     * @param factory
     * @return
     */
    private static ValidatorFactory getValidatorFactoryAnnotation ( eu.agno3.runtime.validation.ConstraintValidatorFactory<?> factory ) {
        if ( !factory.getClass().isAnnotationPresent(ValidatorFactory.class) ) {
            log.warn("ConstraintValidatorFactory is missing @ValidatorFactory annotation"); //$NON-NLS-1$
            return null;
        }

        return factory.getClass().getAnnotation(ValidatorFactory.class);
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.validation.ConstraintValidatorFactory#getInstance(java.lang.Class)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public <T extends ConstraintValidator<?, ?>> T getInstance ( Class<T> clazz ) {

        if ( this.validatorMap.containsKey(clazz) ) {
            return (T) createFromFactory(clazz);
        }

        try {
            return createInstance(clazz);
        }
        catch (
            InstantiationException |
            IllegalAccessException e ) {
            log.error("Failed to instantiate ConstraintValidator " + clazz.getName(), e); //$NON-NLS-1$
            throw new RuntimeException(e);
        }
    }


    /**
     * @param clazz
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    protected <T extends ConstraintValidator<?, ?>> T createInstance ( Class<T> clazz ) throws InstantiationException, IllegalAccessException {
        if ( log.isDebugEnabled() ) {
            log.debug("Create simple ConstraintValidator " + clazz.getName()); //$NON-NLS-1$
        }
        return clazz.newInstance();
    }


    /**
     * @param clazz
     * @return
     */
    protected <T extends ConstraintValidator<?, ?>> ConstraintValidator<?, ?> createFromFactory ( Class<T> clazz ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Create ConstraintValidator %s using ConstraintValidatorFactory", clazz.getName())); //$NON-NLS-1$
        }
        ConstraintValidator<?, ?> validator = this.validatorMap.get(clazz).createValidator();

        if ( !clazz.isAssignableFrom(validator.getClass()) ) {
            throw new ClassCastException("Incompatible validator returned from ConstraintValidatorFactory"); //$NON-NLS-1$
        }

        return validator;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.validation.ConstraintValidatorFactory#releaseInstance(javax.validation.ConstraintValidator)
     */
    @Override
    public void releaseInstance ( ConstraintValidator<?, ?> validator ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Release ConstraintValidator " + validator.getClass().getName()); //$NON-NLS-1$
        }
    }

}
