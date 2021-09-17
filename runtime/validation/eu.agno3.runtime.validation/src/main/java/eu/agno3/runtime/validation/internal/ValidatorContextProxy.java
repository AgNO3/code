/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.01.2017 by mbechler
 */
package eu.agno3.runtime.validation.internal;


import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.ParameterNameProvider;
import javax.validation.TraversableResolver;
import javax.validation.Validator;
import javax.validation.ValidatorContext;

import org.apache.log4j.Logger;


/**
 * @author mbechler
 *
 */
public class ValidatorContextProxy implements ValidatorContext {

    private static final Logger log = Logger.getLogger(ValidatorContextProxy.class);

    private final MessageInterpolator messageInterpolator;
    private final ParameterNameProvider parameterNameProvider;
    private final TraversableResolver traversableResolver;
    private final ConstraintValidatorFactory constraintValidatorFactory;

    private ValidatorFactoryProxy validatorFactoryProxy;


    /**
     * @param validatorFactoryProxy
     * @param mi
     * @param pnp
     * @param tr
     * @param cvf
     */
    public ValidatorContextProxy ( ValidatorFactoryProxy validatorFactoryProxy, MessageInterpolator mi, ParameterNameProvider pnp,
            TraversableResolver tr, ConstraintValidatorFactory cvf ) {
        this.validatorFactoryProxy = validatorFactoryProxy;
        this.messageInterpolator = mi;
        this.parameterNameProvider = pnp;
        this.traversableResolver = tr;
        this.constraintValidatorFactory = cvf;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.validation.ValidatorContext#constraintValidatorFactory(javax.validation.ConstraintValidatorFactory)
     */
    @Override
    public ValidatorContext constraintValidatorFactory ( ConstraintValidatorFactory cvf ) {
        if ( this.constraintValidatorFactory == cvf ) {
            return this;
        }
        log.debug("Custom validator usage (validatorFactory)"); //$NON-NLS-1$
        return new ValidatorContextProxy(
            this.validatorFactoryProxy,
            this.messageInterpolator,
            this.parameterNameProvider,
            this.traversableResolver,
            cvf);
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.validation.ValidatorContext#getValidator()
     */
    @Override
    public Validator getValidator () {
        return this.validatorFactoryProxy
                .getValidator(this.messageInterpolator, this.parameterNameProvider, this.traversableResolver, this.constraintValidatorFactory);
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.validation.ValidatorContext#messageInterpolator(javax.validation.MessageInterpolator)
     */
    @Override
    public ValidatorContext messageInterpolator ( MessageInterpolator mi ) {
        if ( mi == this.messageInterpolator ) {
            return this;
        }
        log.debug("Custom validator usage (messageInterpolator)"); //$NON-NLS-1$
        return new ValidatorContextProxy(
            this.validatorFactoryProxy,
            mi,
            this.parameterNameProvider,
            this.traversableResolver,
            this.constraintValidatorFactory);
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.validation.ValidatorContext#parameterNameProvider(javax.validation.ParameterNameProvider)
     */
    @Override
    public ValidatorContext parameterNameProvider ( ParameterNameProvider pnp ) {
        if ( pnp == this.parameterNameProvider ) {
            return this;
        }
        log.debug("Custom validator usage (parameterNameProvider)"); //$NON-NLS-1$
        return new ValidatorContextProxy(
            this.validatorFactoryProxy,
            this.messageInterpolator,
            pnp,
            this.traversableResolver,
            this.constraintValidatorFactory);
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.validation.ValidatorContext#traversableResolver(javax.validation.TraversableResolver)
     */
    @Override
    public ValidatorContext traversableResolver ( TraversableResolver tr ) {
        if ( tr == this.traversableResolver ) {
            return this;
        }
        log.debug("Custom validator usage (traversableResolver)"); //$NON-NLS-1$
        return new ValidatorContextProxy(
            this.validatorFactoryProxy,
            this.messageInterpolator,
            this.parameterNameProvider,
            tr,
            this.constraintValidatorFactory);
    }

}
