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

import eu.agno3.orchestrator.types.net.NetworkSpecification;


/**
 * @author mbechler
 * 
 */
public class NetworkSpecificationStringValidator implements ConstraintValidator<ValidNetworkSpecification, String> {

    private static final Logger log = Logger.getLogger(NetworkSpecificationStringValidator.class);

    private ValidNetworkSpecification annot;


    /**
     * {@inheritDoc}
     * 
     * @see javax.validation.ConstraintValidator#initialize(java.lang.annotation.Annotation)
     */
    @Override
    public void initialize ( ValidNetworkSpecification ann ) {
        this.annot = ann;
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
        return checkNetworkSpecification(val, ctx, this.annot);
    }


    /**
     * @param val
     * @param ctx
     * @param constraint
     * @return whether the given host name or address is valid
     */
    public static boolean checkNetworkSpecification ( String val, ConstraintValidatorContext ctx, ValidNetworkSpecification constraint ) {

        try {
            NetworkSpecification spec = NetworkSpecification.fromString(val, constraint.allowNoPrefix());
            return NetworkSpecificationValidator.checkNetworkSpecification(spec, ctx, constraint);
        }
        catch ( IllegalArgumentException e ) {
            log.debug("Network Specification parsing failed:", e); //$NON-NLS-1$
            ctx.buildConstraintViolationWithTemplate("{network.illegal}").addConstraintViolation(); //$NON-NLS-1$
            return false;
        }

    }

}
