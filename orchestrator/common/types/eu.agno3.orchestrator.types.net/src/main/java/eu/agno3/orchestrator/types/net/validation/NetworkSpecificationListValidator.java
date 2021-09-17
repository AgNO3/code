/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.04.2014 by mbechler
 */
package eu.agno3.orchestrator.types.net.validation;


import java.util.Collection;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import eu.agno3.orchestrator.types.net.NetworkSpecification;


/**
 * @author mbechler
 * 
 */
public class NetworkSpecificationListValidator implements ConstraintValidator<ValidNetworkSpecification, Collection<NetworkSpecification>> {

    private ValidNetworkSpecification annot;


    /**
     * {@inheritDoc}
     * 
     * @see javax.validation.ConstraintValidator#initialize(java.lang.annotation.Annotation)
     */
    @Override
    public void initialize ( ValidNetworkSpecification info ) {
        this.annot = info;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.validation.ConstraintValidator#isValid(java.lang.Object, javax.validation.ConstraintValidatorContext)
     */
    @Override
    public boolean isValid ( Collection<NetworkSpecification> list, ConstraintValidatorContext ctx ) {
        if ( list == null ) {
            return true;
        }
        ctx.disableDefaultConstraintViolation();

        for ( NetworkSpecification addr : list ) {
            if ( addr != null && !NetworkSpecificationValidator.checkNetworkSpecification(addr, ctx, this.annot) ) {
                return false;
            }
        }

        return true;
    }

}
