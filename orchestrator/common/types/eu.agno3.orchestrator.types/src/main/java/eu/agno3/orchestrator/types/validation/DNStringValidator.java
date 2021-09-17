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
import org.bouncycastle.asn1.x500.X500Name;


/**
 * @author mbechler
 *
 */
public class DNStringValidator implements ConstraintValidator<ValidDN, String> {

    private static final Logger log = Logger.getLogger(DNStringValidator.class);


    /**
     * {@inheritDoc}
     *
     * @see javax.validation.ConstraintValidator#initialize(java.lang.annotation.Annotation)
     */
    @Override
    public void initialize ( ValidDN spec ) {}


    /**
     * {@inheritDoc}
     *
     * @see javax.validation.ConstraintValidator#isValid(java.lang.Object, javax.validation.ConstraintValidatorContext)
     */
    @Override
    public boolean isValid ( String val, ConstraintValidatorContext ctx ) {
        ctx.disableDefaultConstraintViolation();
        try {
            if ( StringUtils.isBlank(val) ) {
                return true;
            }
            new X500Name(val);
            return true;
        }
        catch ( IllegalArgumentException e ) {
            log.debug("Invalid DN", e); //$NON-NLS-1$
            ctx.buildConstraintViolationWithTemplate("{dn.invalid}").addConstraintViolation(); //$NON-NLS-1$
            return false;
        }
    }

}
