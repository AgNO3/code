/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.04.2014 by mbechler
 */
package eu.agno3.orchestrator.types.net.validation;


import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.types.net.MACAddress;


/**
 * @author mbechler
 * 
 */
public class HardwareAddressStringValidator implements ConstraintValidator<ValidHardwareAddress, String> {

    private static final Logger log = Logger.getLogger(HardwareAddressStringValidator.class);
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
    public boolean isValid ( String val, ConstraintValidatorContext ctx ) {
        if ( val == null ) {
            return true;
        }
        ctx.disableDefaultConstraintViolation();
        return checkHardwareAddress(val, ctx, this.annot);
    }


    /**
     * @param val
     * @param ctx
     * @param annot
     * @return whether this is a valid network address
     */
    public static boolean checkHardwareAddress ( String val, ConstraintValidatorContext ctx, ValidHardwareAddress annot ) {

        MACAddress mac = new MACAddress();
        try {
            mac.fromString(val);
        }
        catch ( IllegalArgumentException e ) {
            log.trace("Illegal mac address", e); //$NON-NLS-1$
            ctx.buildConstraintViolationWithTemplate("{hwaddr.invalid}").addConstraintViolation(); //$NON-NLS-1$
            return false;
        }

        return true;
    }

}
