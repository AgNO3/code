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

import eu.agno3.orchestrator.types.net.NetworkAddress;


/**
 * @author mbechler
 * 
 */
public class NetworkAddressListValidator implements ConstraintValidator<ValidNetworkAddress, List<NetworkAddress>> {

    private ValidNetworkAddress annot;


    /**
     * {@inheritDoc}
     * 
     * @see javax.validation.ConstraintValidator#initialize(java.lang.annotation.Annotation)
     */
    @Override
    public void initialize ( ValidNetworkAddress info ) {
        this.annot = info;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.validation.ConstraintValidator#isValid(java.lang.Object, javax.validation.ConstraintValidatorContext)
     */
    @Override
    public boolean isValid ( List<NetworkAddress> list, ConstraintValidatorContext ctx ) {
        if ( list == null ) {
            return true;
        }
        ctx.disableDefaultConstraintViolation();

        for ( NetworkAddress addr : list ) {
            if ( !NetworkAddressValidator.checkNetworkAddress(addr, ctx, this.annot) ) {
                return false;
            }
        }

        return true;
    }

}
