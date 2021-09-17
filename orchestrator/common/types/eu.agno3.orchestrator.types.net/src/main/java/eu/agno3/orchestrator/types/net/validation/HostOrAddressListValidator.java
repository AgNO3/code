/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.04.2014 by mbechler
 */
package eu.agno3.orchestrator.types.net.validation;


import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import eu.agno3.orchestrator.types.net.name.HostOrAddress;


/**
 * @author mbechler
 * 
 */
public class HostOrAddressListValidator implements ConstraintValidator<ValidHostOrAddress, List<HostOrAddress>> {

    private ValidHostOrAddress annot;


    /**
     * {@inheritDoc}
     * 
     * @see javax.validation.ConstraintValidator#initialize(java.lang.annotation.Annotation)
     */
    @Override
    public void initialize ( ValidHostOrAddress info ) {
        this.annot = info;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.validation.ConstraintValidator#isValid(java.lang.Object, javax.validation.ConstraintValidatorContext)
     */
    @Override
    public boolean isValid ( List<HostOrAddress> list, ConstraintValidatorContext ctx ) {
        if ( list == null ) {
            return true;
        }
        ctx.disableDefaultConstraintViolation();

        for ( HostOrAddress addr : list ) {
            if ( addr != null && !HostOrAddressValidator.checkHostOrAddress(addr, ctx, this.annot) ) {
                return false;
            }
        }

        return true;
    }

}
