/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.01.2016 by mbechler
 */
package eu.agno3.orchestrator.types.validation;


import java.util.Collection;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


/**
 * @author mbechler
 *
 */
public class SIDCollectionValidator implements ConstraintValidator<ValidSID, Collection<String>> {

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
    public boolean isValid ( Collection<String> val, ConstraintValidatorContext ctx ) {
        ctx.disableDefaultConstraintViolation();
        if ( val == null || val.isEmpty() ) {
            return true;
        }

        boolean anyInvalid = false;
        for ( String sid : val ) {
            anyInvalid |= !SIDStringValidator.validateSID(sid, ctx);
        }
        return !anyInvalid;
    }

}
