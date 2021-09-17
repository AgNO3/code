/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.01.2016 by mbechler
 */
package eu.agno3.orchestrator.types.validation;


import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.runtime.util.sid.SID;


/**
 * @author mbechler
 *
 */
public class SIDStringValidator implements ConstraintValidator<ValidSID, String> {

    private static final Logger log = Logger.getLogger(SIDStringValidator.class);


    /**
     * {@inheritDoc}
     *
     * @see javax.validation.ConstraintValidator#initialize(java.lang.annotation.Annotation)
     */
    @Override
    public void initialize ( ValidSID spec ) {}


    /**
     * {@inheritDoc}
     *
     * @see javax.validation.ConstraintValidator#isValid(java.lang.Object, javax.validation.ConstraintValidatorContext)
     */
    @Override
    public boolean isValid ( String val, ConstraintValidatorContext ctx ) {
        ctx.disableDefaultConstraintViolation();
        return validateSID(val, ctx);
    }


    /**
     * @param val
     * @param ctx
     * @return whether the SID is valid
     */
    static boolean validateSID ( String val, ConstraintValidatorContext ctx ) {
        try {
            if ( StringUtils.isBlank(val) ) {
                return true;
            }

            SID.fromString(val);
            return true;
        }
        catch ( IllegalArgumentException e ) {
            log.debug("Invalid SID", e); //$NON-NLS-1$
            ctx.buildConstraintViolationWithTemplate("{sid.invalid}").addConstraintViolation(); //$NON-NLS-1$
            return false;
        }
    }

}
