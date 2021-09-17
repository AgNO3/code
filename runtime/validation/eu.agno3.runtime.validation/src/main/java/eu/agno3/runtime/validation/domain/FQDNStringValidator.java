/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.04.2014 by mbechler
 */
package eu.agno3.runtime.validation.domain;


import java.lang.annotation.Annotation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;


/**
 * @author mbechler
 * 
 */
public class FQDNStringValidator implements ConstraintValidator<ValidFQDN, String> {

    private ValidFQDN annot;


    /**
     * {@inheritDoc}
     * 
     * @see javax.validation.ConstraintValidator#initialize(java.lang.annotation.Annotation)
     */
    @Override
    public void initialize ( ValidFQDN info ) {
        this.annot = info;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.validation.ConstraintValidator#isValid(java.lang.Object, javax.validation.ConstraintValidatorContext)
     */
    @Override
    public boolean isValid ( String val, ConstraintValidatorContext ctx ) {
        if ( val == null ) {
            return true;
        }
        ctx.disableDefaultConstraintViolation();

        return checkFQDN(val, ctx, this.annot.allowIdn());
    }


    /**
     * @param val
     * @param ctx
     * @param allowIdn
     * @return whether this is a valid fully qualified domain name
     */
    public static boolean checkFQDN ( String val, ConstraintValidatorContext ctx, boolean allowIdn ) {
        return DomainNameStringValidator.checkDomainName(val, ctx, allowIdn);
    }


    /**
     * @param allowIdn
     * @return a ValidFQDN constraint
     */
    public static ValidFQDN makeConstraint ( final boolean allowIdn ) {
        return new ValidFQDNImplementation(allowIdn);
    }

    /**
     * @author mbechler
     * 
     */
    @SuppressWarnings ( "all" )
    private static final class ValidFQDNImplementation implements ValidFQDN {

        /**
         * 
         */
        private final boolean allowIdn;


        /**
         * @param allowIdn
         */
        ValidFQDNImplementation ( boolean allowIdn ) {
            this.allowIdn = allowIdn;
        }


        @Override
        public Class<? extends Annotation> annotationType () {
            return ValidFQDN.class;
        }


        @SuppressWarnings ( "unchecked" )
        @Override
        public Class<? extends Payload>[] payload () {
            return new Class[] {};
        }


        @Override
        public String message () {
            return null;
        }


        @Override
        public Class<?>[] groups () {
            return new Class[] {};
        }


        @Override
        public boolean allowIdn () {
            return this.allowIdn;
        }
    }
}
