/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.04.2014 by mbechler
 */
package eu.agno3.orchestrator.types.net.validation;


import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import eu.agno3.orchestrator.types.net.name.HostOrAddress;
import eu.agno3.runtime.validation.FakeValidationAnnotation;
import eu.agno3.runtime.validation.domain.FQDNStringValidator;
import eu.agno3.runtime.validation.domain.ValidFQDN;


/**
 * @author mbechler
 * 
 */
public class HostOrAddressValidator implements ConstraintValidator<ValidHostOrAddress, HostOrAddress> {

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
    public boolean isValid ( HostOrAddress obj, ConstraintValidatorContext ctx ) {
        if ( obj == null ) {
            return true;
        }
        ctx.disableDefaultConstraintViolation();

        return checkHostOrAddress(obj, ctx, this.annot);
    }


    /**
     * @param obj
     * @param ctx
     * @param info
     * @return whether the host or address is valid
     */
    public static boolean checkHostOrAddress ( HostOrAddress obj, ConstraintValidatorContext ctx, ValidHostOrAddress info ) {
        if ( obj.isNetworkAddress() ) {
            return NetworkAddressValidator.checkNetworkAddress(obj.getAddress(), ctx, info.addr());
        }

        return FQDNStringValidator.checkFQDN(obj.getHostName(), ctx, info.host().allowIdn());
    }


    /**
     * @param hostCheck
     * @param addrCheck
     * @return a ValidHostOrAddress annotation
     */
    public static ValidHostOrAddress makeConstraint ( final ValidFQDN hostCheck, final ValidNetworkAddress addrCheck ) {
        return new ValidHostOrAddressImplementation(addrCheck, hostCheck);
    }

    /**
     * @author mbechler
     * 
     */
    @SuppressWarnings ( "all" )
    private static final class ValidHostOrAddressImplementation extends FakeValidationAnnotation implements ValidHostOrAddress {

        /**
         * 
         */
        private final ValidNetworkAddress addrCheck;
        /**
         * 
         */
        private final ValidFQDN hostCheck;


        /**
         * @param addrCheck
         * @param hostCheck
         */
        ValidHostOrAddressImplementation ( ValidNetworkAddress addrCheck, ValidFQDN hostCheck ) {
            super(ValidHostOrAddress.class);
            this.addrCheck = addrCheck;
            this.hostCheck = hostCheck;
        }


        @Override
        public ValidFQDN host () {
            return this.hostCheck;
        }


        @Override
        public ValidNetworkAddress addr () {
            return this.addrCheck;
        }

    }
}
