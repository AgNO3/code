/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.04.2014 by mbechler
 */
package eu.agno3.orchestrator.types.net.validation;


import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import eu.agno3.orchestrator.types.net.IPv4Address;
import eu.agno3.orchestrator.types.net.IPv6Address;
import eu.agno3.orchestrator.types.net.NetworkAddress;
import eu.agno3.orchestrator.types.net.NetworkAddressType;
import eu.agno3.runtime.validation.FakeValidationAnnotation;


/**
 * @author mbechler
 * 
 */
public class NetworkAddressValidator implements ConstraintValidator<ValidNetworkAddress, NetworkAddress> {

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
    public boolean isValid ( NetworkAddress addr, ConstraintValidatorContext ctx ) {
        if ( addr == null ) {
            return true;
        }
        ctx.disableDefaultConstraintViolation();
        return checkNetworkAddress(addr, ctx, this.annot);
    }


    /**
     * @param addr
     * @param ctx
     * @param annot
     * @return whether this is a valid network address
     */
    public static boolean checkNetworkAddress ( NetworkAddress addr, ConstraintValidatorContext ctx, ValidNetworkAddress annot ) {

        if ( addr == null ) {
            return true;
        }

        if ( !checkAddressType(addr, ctx, annot) ) {
            return false;
        }

        return checkAddressProperties(addr, ctx, annot);
    }


    private static boolean checkAddressProperties ( NetworkAddress addr, ConstraintValidatorContext ctx, ValidNetworkAddress annot ) {
        boolean valid = true;
        Set<NetworkAddressType> allowedTypes = EnumSet.copyOf(Arrays.asList(annot.allowedTypes()));

        valid &= checkAddrClass(addr, ctx, allowedTypes);

        if ( !allowedTypes.contains(NetworkAddressType.LOOPBACK) && addr.isLoopback() ) {
            ctx.buildConstraintViolationWithTemplate("{ipaddr.illegalLoopback}").addConstraintViolation(); //$NON-NLS-1$
            valid = false;
        }

        if ( !allowedTypes.contains(NetworkAddressType.RESERVED) && addr.isReserved() ) {
            ctx.buildConstraintViolationWithTemplate("{ipaddr.illegalReserved}").addConstraintViolation(); //$NON-NLS-1$
            valid = false;
        }

        return valid;
    }


    private static boolean checkAddrClass ( NetworkAddress addr, ConstraintValidatorContext ctx, Set<NetworkAddressType> allowedTypes ) {
        if ( !allowedTypes.contains(NetworkAddressType.UNICAST) && addr.isUnicast() ) {
            ctx.buildConstraintViolationWithTemplate("{ipaddr.illegalUnicast}").addConstraintViolation(); //$NON-NLS-1$
            return false;
        }

        if ( !allowedTypes.contains(NetworkAddressType.BROADCAST) && addr.isBroadcast() ) {
            ctx.buildConstraintViolationWithTemplate("{ipaddr.illegalBroadcast}").addConstraintViolation(); //$NON-NLS-1$
            return false;
        }

        if ( !allowedTypes.contains(NetworkAddressType.MULTICAST) && addr.isMulticast() ) {
            ctx.buildConstraintViolationWithTemplate("{ipaddr.illegalMulticast}").addConstraintViolation(); //$NON-NLS-1$
            return false;
        }

        if ( !allowedTypes.contains(NetworkAddressType.ANYCAST) && addr.isAnycast() ) {
            ctx.buildConstraintViolationWithTemplate("{ipaddr.illegalAnycast}").addConstraintViolation(); //$NON-NLS-1$
            return false;
        }
        return true;
    }


    private static boolean checkAddressType ( NetworkAddress addr, ConstraintValidatorContext ctx, ValidNetworkAddress annot ) {
        if ( ! ( addr instanceof IPv6Address ) && ! ( addr instanceof IPv4Address ) ) {
            ctx.buildConstraintViolationWithTemplate("{ipaddr.unknownFormat}").addConstraintViolation(); //$NON-NLS-1$
            return false;
        }

        if ( addr instanceof IPv4Address && !annot.v4() ) {
            ctx.buildConstraintViolationWithTemplate("{ipaddr.v4NotAllowed}").addConstraintViolation(); //$NON-NLS-1$
            return false;
        }

        if ( addr instanceof IPv6Address && !annot.v6() ) {
            ctx.buildConstraintViolationWithTemplate("{ipaddr.v6NotAllowed}").addConstraintViolation(); //$NON-NLS-1$
            return false;
        }

        Set<NetworkAddressType> allowedTypes = EnumSet.copyOf(Arrays.asList(annot.allowedTypes()));

        if ( !allowedTypes.contains(NetworkAddressType.UNSPECIFIED) && addr.isUnspecified() ) {
            ctx.buildConstraintViolationWithTemplate("{ipaddr.illegalUnspecified}").addConstraintViolation(); //$NON-NLS-1$
            return false;
        }

        return true;
    }


    /**
     * @param v4
     * @param v6
     * @param allowedTypes
     * @return a ValidNetworkAddress constraint
     */
    public static ValidNetworkAddress makeConstraint ( final boolean v4, final boolean v6, final Set<NetworkAddressType> allowedTypes ) {
        return new ValidNetworkAddressImplementation(allowedTypes, v6, v4);
    }

    /**
     * @author mbechler
     * 
     */
    @SuppressWarnings ( "all" )
    private static final class ValidNetworkAddressImplementation extends FakeValidationAnnotation implements ValidNetworkAddress {

        /**
         * 
         */
        private final Set<NetworkAddressType> allowedTypes;
        /**
         * 
         */
        private final boolean v6;
        /**
         * 
         */
        private final boolean v4;


        /**
         * @param allowedTypes
         * @param v6
         * @param v4
         */
        ValidNetworkAddressImplementation ( Set<NetworkAddressType> allowedTypes, boolean v6, boolean v4 ) {
            super(ValidNetworkAddress.class);
            this.allowedTypes = allowedTypes;
            this.v6 = v6;
            this.v4 = v4;
        }


        @Override
        public boolean v6 () {
            return this.v6;
        }


        @Override
        public boolean v4 () {
            return this.v4;
        }


        @Override
        public boolean parsableOnly () {
            return false;
        }


        @Override
        public NetworkAddressType[] allowedTypes () {
            return this.allowedTypes.toArray(new NetworkAddressType[] {});
        }
    }
}
