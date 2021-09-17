/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.04.2014 by mbechler
 */
package eu.agno3.orchestrator.types.net.validation;


import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import eu.agno3.orchestrator.types.net.HardwareAddress;
import eu.agno3.runtime.validation.FakeValidationAnnotation;


/**
 * @author mbechler
 * 
 */
public class HardwareAddressValidator implements ConstraintValidator<ValidHardwareAddress, HardwareAddress> {

    private ValidHardwareAddress annot;


    /**
     * {@inheritDoc}
     * 
     * @see javax.validation.ConstraintValidator#initialize(java.lang.annotation.Annotation)
     */
    @Override
    public void initialize ( ValidHardwareAddress info ) {
        this.annot = info;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.validation.ConstraintValidator#isValid(java.lang.Object, javax.validation.ConstraintValidatorContext)
     */
    @Override
    public boolean isValid ( HardwareAddress addr, ConstraintValidatorContext ctx ) {
        if ( addr == null ) {
            return true;
        }
        ctx.disableDefaultConstraintViolation();
        return checkHardwareAddress(addr, ctx, this.annot);
    }


    /**
     * @param addr
     * @param ctx
     * @param annot
     * @return whether this is a valid network address
     */
    public static boolean checkHardwareAddress ( HardwareAddress addr, ConstraintValidatorContext ctx, ValidHardwareAddress annot ) {
        return true;
    }


    /**
     * @return a ValidNetworkAddress constraint
     */
    public static ValidHardwareAddress makeConstraint () {
        return new ValidHardwareAddressImplementation();
    }

    /**
     * @author mbechler
     * 
     */
    @SuppressWarnings ( "all" )
    private static final class ValidHardwareAddressImplementation extends FakeValidationAnnotation implements ValidHardwareAddress {

        /**
         * 
         */
        public ValidHardwareAddressImplementation () {
            super(ValidHardwareAddress.class);
        }

    }
}
