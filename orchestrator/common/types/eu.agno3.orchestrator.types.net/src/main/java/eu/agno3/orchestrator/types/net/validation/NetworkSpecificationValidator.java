/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.04.2014 by mbechler
 */
package eu.agno3.orchestrator.types.net.validation;


import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import eu.agno3.orchestrator.types.net.NetworkSpecification;
import eu.agno3.runtime.validation.FakeValidationAnnotation;


/**
 * @author mbechler
 * 
 */
public class NetworkSpecificationValidator implements ConstraintValidator<ValidNetworkSpecification, NetworkSpecification> {

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
    public boolean isValid ( NetworkSpecification obj, ConstraintValidatorContext ctx ) {
        if ( obj == null ) {
            return true;
        }
        ctx.disableDefaultConstraintViolation();

        return checkNetworkSpecification(obj, ctx, this.annot);
    }


    /**
     * @param obj
     * @param ctx
     * @param info
     * @return whether the host or address is valid
     */
    public static boolean checkNetworkSpecification ( NetworkSpecification obj, ConstraintValidatorContext ctx, ValidNetworkSpecification info ) {

        if ( !NetworkAddressValidator.checkNetworkAddress(obj.getAddress(), ctx, info.restrictAddress()) ) {
            return false;
        }

        if ( obj.getPrefixLength() > obj.getAddress().getBitSize() ) {
            ctx.buildConstraintViolationWithTemplate("{network.illegalPrefixLength}").addConstraintViolation(); //$NON-NLS-1$
            return false;
        }

        if ( info.requireNetworkAddress() ) {
            short[] address = obj.getAddress().getAddress();
            for ( int i = 0; i < address.length; i++ ) {
                if ( !checkAddressPart(obj, ctx, address, i) ) {
                    return false;
                }
            }
        }

        return true;
    }


    /**
     * @param obj
     * @param ctx
     * @param address
     * @param i
     */
    protected static boolean checkAddressPart ( NetworkSpecification obj, ConstraintValidatorContext ctx, short[] address, int i ) {
        int rem = Math.max(0, i * 8 - obj.getPrefixLength());

        if ( rem > 0 ) {
            int mask = 0xFF >> ( 8 - rem );

            if ( ( address[ i ] & mask ) > 0 ) {
                ctx.buildConstraintViolationWithTemplate("{network.notNetworkAddress}").addConstraintViolation(); //$NON-NLS-1$
                return false;
            }
        }

        return true;
    }


    /**
     * @param addrCheck
     * @param requireNetworkAddress
     * @param allowNoPrefixLength
     * @return a ValidHostOrAddress annotation
     */
    public static ValidNetworkSpecification makeConstraint ( final ValidNetworkAddress addrCheck, boolean requireNetworkAddress,
            boolean allowNoPrefixLength ) {
        return new ValidNetworkSpecificationImplementation(addrCheck, requireNetworkAddress, allowNoPrefixLength);
    }

    /**
     * @author mbechler
     * 
     */
    @SuppressWarnings ( "all" )
    private static final class ValidNetworkSpecificationImplementation extends FakeValidationAnnotation implements ValidNetworkSpecification {

        /**
         * 
         */
        private final ValidNetworkAddress addrCheck;

        private boolean requireNetworkAddress;

        private boolean allowNoPrefixLength;


        /**
         * @param addrCheck
         * @param allowNoPrefixLength
         * @param hostCheck
         */
        ValidNetworkSpecificationImplementation ( ValidNetworkAddress addrCheck, boolean requireNetworkAddress, boolean allowNoPrefixLength ) {
            super(ValidNetworkSpecification.class);
            this.addrCheck = addrCheck;
            this.requireNetworkAddress = requireNetworkAddress;
            this.allowNoPrefixLength = allowNoPrefixLength;

        }


        /**
         * {@inheritDoc}
         * 
         * @see eu.agno3.orchestrator.types.net.validation.ValidNetworkSpecification#restrictAddress()
         */
        @Override
        public ValidNetworkAddress restrictAddress () {
            return this.addrCheck;
        }


        /**
         * {@inheritDoc}
         * 
         * @see eu.agno3.orchestrator.types.net.validation.ValidNetworkSpecification#requireNetworkAddress()
         */
        @Override
        public boolean requireNetworkAddress () {
            return this.requireNetworkAddress;
        }


        /**
         * {@inheritDoc}
         *
         * @see eu.agno3.orchestrator.types.net.validation.ValidNetworkSpecification#allowNoPrefix()
         */
        @Override
        public boolean allowNoPrefix () {
            return this.allowNoPrefixLength;
        }

    }
}
