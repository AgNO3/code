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

import eu.agno3.runtime.validation.domain.ValidFQDN;


/**
 * @author mbechler
 * 
 */
public class HostOrAddressStringListValidator implements ConstraintValidator<ValidHostOrAddress, List<String>> {

    private ValidNetworkAddress addr;
    private ValidFQDN host;


    /**
     * {@inheritDoc}
     * 
     * @see javax.validation.ConstraintValidator#initialize(java.lang.annotation.Annotation)
     */
    @Override
    public void initialize ( ValidHostOrAddress annot ) {
        this.addr = annot.addr();
        this.host = annot.host();
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

        for ( String val : list ) {
            if ( val != null && !HostOrAddressStringValidator.checkHostOrAddress(val, ctx, this.addr, this.host) ) {
                return false;
            }
        }

        return true;
    }
}
