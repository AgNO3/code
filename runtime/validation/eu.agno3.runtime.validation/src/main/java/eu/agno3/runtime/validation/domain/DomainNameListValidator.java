/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.04.2015 by mbechler
 */
package eu.agno3.runtime.validation.domain;


import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


/**
 * @author mbechler
 *
 */
public class DomainNameListValidator implements ConstraintValidator<ValidDomainName, List<String>> {

    private ValidDomainName annot;


    /**
     * {@inheritDoc}
     * 
     * @see javax.validation.ConstraintValidator#initialize(java.lang.annotation.Annotation)
     */
    @Override
    public void initialize ( ValidDomainName info ) {
        this.annot = info;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.validation.ConstraintValidator#isValid(java.lang.Object, javax.validation.ConstraintValidatorContext)
     */
    @Override
    public boolean isValid ( List<String> list, ConstraintValidatorContext ctx ) {
        if ( list == null ) {
            return true;
        }
        ctx.disableDefaultConstraintViolation();
        for ( String addr : list ) {
            if ( addr != null && !DomainNameStringValidator.checkDomainName(addr, ctx, this.annot.allowIdn()) ) {
                return false;
            }
        }

        return true;
    }
}
