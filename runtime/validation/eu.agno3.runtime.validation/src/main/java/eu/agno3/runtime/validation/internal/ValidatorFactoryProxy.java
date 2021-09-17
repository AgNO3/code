/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.02.2014 by mbechler
 */
package eu.agno3.runtime.validation.internal;


import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.validation.Configuration;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.ParameterNameProvider;
import javax.validation.TraversableResolver;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorContext;
import javax.validation.ValidatorFactory;

import org.apache.log4j.Logger;
import org.hibernate.validator.HibernateValidator;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;


/**
 * @author mbechler
 * 
 */
@Component ( service = ValidatorFactory.class )
public class ValidatorFactoryProxy implements ValidatorFactory, BundleListener {

    private static final Logger log = Logger.getLogger(ValidatorFactoryProxy.class);

    private static final ValidatorCacheKey DEFAULT_KEY = new ValidatorCacheKey(null, null, null, null);

    private ConstraintValidatorFactory constraintValidatorFactory;
    private MessageInterpolator messageInterpolator;
    private ValidatorFactory delegate;
    private final Object delegateLock = new Object();
    private boolean needsRefresh = false;

    // this get's cleared on every bundle change therefor should not leak
    private final Map<ValidatorCacheKey, Validator> validators = new ConcurrentHashMap<>();


    @Reference
    protected synchronized void bindConstraintValidatorFactory ( ConstraintValidatorFactory factory ) {
        this.constraintValidatorFactory = factory;
    }


    protected synchronized void unbindConstraintValidatorFactory ( ConstraintValidatorFactory factory ) {
        if ( this.constraintValidatorFactory == factory ) {
            this.constraintValidatorFactory = null;
        }
    }


    @Reference
    protected synchronized void bindMessageInterpolator ( MessageInterpolator interpolator ) {
        this.messageInterpolator = interpolator;
    }


    protected synchronized void unbindMessageInterpolator ( MessageInterpolator interpolator ) {
        if ( this.messageInterpolator == interpolator ) {
            this.messageInterpolator = interpolator;
        }
    }


    @Activate
    protected void activate ( ComponentContext context ) {
        context.getBundleContext().addBundleListener(this);
        synchronized ( this.delegateLock ) {
            this.delegate = makeValidatorFactory();
        }
    }


    @Deactivate
    protected void deactivate ( ComponentContext context ) {
        synchronized ( this.delegateLock ) {
            if ( this.delegate != null ) {
                this.delegate.close();
                this.delegate = null;
            }
        }
        context.getBundleContext().removeBundleListener(this);
    }


    /**
     * @return
     */
    private ValidatorFactory makeValidatorFactory () {
        Configuration<?> config = Validation.byProvider(HibernateValidator.class).providerResolver(new DummyValidationProviderResolver()).configure();
        ValidatorFactory validatorFactory = config.messageInterpolator(this.messageInterpolator)
                .constraintValidatorFactory(this.constraintValidatorFactory).ignoreXmlConfiguration().buildValidatorFactory();
        return validatorFactory;
    }


    synchronized ValidatorFactory getDelegate () {
        synchronized ( this.delegateLock ) {

            if ( this.needsRefresh ) {
                log.debug("Refreshing validator factory"); //$NON-NLS-1$
                this.delegate = makeValidatorFactory();
                this.needsRefresh = false;
            }

            return this.delegate;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.framework.BundleListener#bundleChanged(org.osgi.framework.BundleEvent)
     */
    @Override
    public void bundleChanged ( BundleEvent event ) {
        synchronized ( this.delegateLock ) {
            this.validators.clear();
            this.needsRefresh = true;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.validation.ValidatorFactory#close()
     */
    @Override
    public void close () {
        this.validators.clear();
        getDelegate().close();
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.validation.ValidatorFactory#getConstraintValidatorFactory()
     */
    @Override
    public synchronized ConstraintValidatorFactory getConstraintValidatorFactory () {
        return getDelegate().getConstraintValidatorFactory();
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.validation.ValidatorFactory#getMessageInterpolator()
     */
    @Override
    public synchronized MessageInterpolator getMessageInterpolator () {
        return getDelegate().getMessageInterpolator();
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.validation.ValidatorFactory#getParameterNameProvider()
     */
    @Override
    public ParameterNameProvider getParameterNameProvider () {
        return getDelegate().getParameterNameProvider();
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.validation.ValidatorFactory#getTraversableResolver()
     */
    @Override
    public TraversableResolver getTraversableResolver () {
        return getDelegate().getTraversableResolver();
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.validation.ValidatorFactory#getValidator()
     */
    @Override
    public Validator getValidator () {
        return getValidator(DEFAULT_KEY);
    }


    /**
     * @param key
     * @return
     */
    private Validator getValidator ( ValidatorCacheKey key ) {
        Validator validator = this.validators.get(key);
        if ( validator == null ) {
            if ( key == DEFAULT_KEY ) {
                validator = new ValidatorProxy(getDelegate().getValidator());
            }
            else {
                log.debug("Creating new validator"); //$NON-NLS-1$
                ValidatorContext ctx = getDelegate().usingContext();
                if ( key.getConstaintValidatorFactory() != null ) {
                    ctx = ctx.constraintValidatorFactory(key.getConstaintValidatorFactory());
                }
                if ( key.getMessageInterpolator() != null ) {
                    ctx = ctx.messageInterpolator(key.getMessageInterpolator());
                }
                if ( key.getParameterNameProvider() != null ) {
                    ctx = ctx.parameterNameProvider(key.getParameterNameProvider());
                }
                if ( key.getTraversableResolver() != null ) {
                    ctx = ctx.traversableResolver(key.getTraversableResolver());
                }
                validator = new ValidatorProxy(ctx.getValidator());
            }

            this.validators.put(key, validator);
        }
        else {
            log.trace("Using cached validator"); //$NON-NLS-1$
        }
        return validator;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.validation.ValidatorFactory#unwrap(java.lang.Class)
     */
    @Override
    public <T> T unwrap ( Class<T> arg0 ) {
        return getDelegate().unwrap(arg0);
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.validation.ValidatorFactory#usingContext()
     */
    @Override
    public ValidatorContext usingContext () {
        return new ValidatorContextProxy(this, null, null, null, null);
    }


    /**
     * @param mi
     * @param pnp
     * @param tr
     * @param cvf
     * @return
     */
    Validator getValidator ( MessageInterpolator mi, ParameterNameProvider pnp, TraversableResolver tr, ConstraintValidatorFactory cvf ) {
        return getValidator(
            new ValidatorCacheKey(mi != this.messageInterpolator ? mi : null, pnp, tr, cvf != this.constraintValidatorFactory ? cvf : null));
    }

    private static class ValidatorCacheKey {

        private final MessageInterpolator mi;
        private final ParameterNameProvider pnp;
        private final TraversableResolver tr;
        private final ConstraintValidatorFactory cvf;


        /**
         * @param mi
         * @param pnp
         * @param tr
         * @param cvf
         */
        public ValidatorCacheKey ( MessageInterpolator mi, ParameterNameProvider pnp, TraversableResolver tr, ConstraintValidatorFactory cvf ) {
            this.mi = mi;
            this.pnp = pnp;
            this.tr = tr;
            this.cvf = cvf;
        }


        /**
         * @return the mi
         */
        public final MessageInterpolator getMessageInterpolator () {
            return this.mi;
        }


        /**
         * @return the pnp
         */
        public final ParameterNameProvider getParameterNameProvider () {
            return this.pnp;
        }


        /**
         * @return the tr
         */
        public final TraversableResolver getTraversableResolver () {
            return this.tr;
        }


        /**
         * @return the cvf
         */
        public final ConstraintValidatorFactory getConstaintValidatorFactory () {
            return this.cvf;
        }


        /**
         * {@inheritDoc}
         *
         * @see java.lang.Object#hashCode()
         */
        @Override
        public final int hashCode () {
            return Objects.hash(this.mi, this.pnp, this.tr, this.cvf);
        }


        /**
         * {@inheritDoc}
         *
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public final boolean equals ( Object obj ) {
            if ( ! ( obj instanceof ValidatorCacheKey ) ) {
                return false;
            }
            ValidatorCacheKey o = (ValidatorCacheKey) obj;
            return Objects.equals(this.mi, o.mi) && Objects.equals(this.pnp, o.pnp) && Objects.equals(this.tr, o.tr)
                    && Objects.equals(this.cvf, o.cvf);
        }
    }
}
