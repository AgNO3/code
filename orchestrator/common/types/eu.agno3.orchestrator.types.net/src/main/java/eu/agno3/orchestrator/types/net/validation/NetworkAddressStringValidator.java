/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.04.2014 by mbechler
 */
package eu.agno3.orchestrator.types.net.validation;


import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.orchestrator.types.net.AbstractIPAddress;
import eu.agno3.orchestrator.types.net.IPv4Address;
import eu.agno3.orchestrator.types.net.IPv6Address;
import eu.agno3.runtime.util.ip.IpUtil;


/**
 * @author mbechler
 * 
 */
public class NetworkAddressStringValidator implements ConstraintValidator<ValidNetworkAddress, String> {

    private static final Logger log = Logger.getLogger(NetworkAddressStringValidator.class);
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
    public boolean isValid ( String val, ConstraintValidatorContext ctx ) {
        if ( StringUtils.isEmpty(val) ) {
            return true;
        }
        ctx.disableDefaultConstraintViolation();
        return checkNetworkAddress(val, ctx, this.annot);
    }


    /**
     * @param val
     * @param ctx
     * @param annot
     * @return whether this is a valid network address
     */
    public static boolean checkNetworkAddress ( String val, ConstraintValidatorContext ctx, ValidNetworkAddress annot ) {

        AbstractIPAddress addr = checkAddressType(val, ctx, annot);

        if ( addr == null ) {
            return false;
        }

        if ( annot.parsableOnly() ) {
            return true;
        }

        return NetworkAddressValidator.checkNetworkAddress(addr, ctx, annot);
    }


    private static AbstractIPAddress checkAddressType ( String val, ConstraintValidatorContext ctx, ValidNetworkAddress annot ) {
        boolean v6Address = IpUtil.isV6Address(val);

        boolean v4Address = IpUtil.isV4Address(val);
        if ( v4Address ) {
            return checkV4Address(val, ctx, annot);
        }
        else if ( v6Address ) {
            return checkV6Address(val, ctx, annot);
        }

        ctx.buildConstraintViolationWithTemplate("{ipaddr.unknownFormat}").addConstraintViolation(); //$NON-NLS-1$
        return null;
    }


    private static IPv6Address checkV6Address ( String val, ConstraintValidatorContext ctx, ValidNetworkAddress annot ) {
        if ( annot.v6() ) {
            try {
                return IPv6Address.parseV6Address(val);
            }
            catch ( IllegalArgumentException e ) {
                log.debug("V6 Address parsing failed:", e); //$NON-NLS-1$
                ctx.buildConstraintViolationWithTemplate("{ipaddr.illegalV6}").addConstraintViolation(); //$NON-NLS-1$
            }
        }
        else {
            ctx.buildConstraintViolationWithTemplate("{ipaddr.v6NotAllowed}").addConstraintViolation(); //$NON-NLS-1$
        }
        return null;
    }


    private static IPv4Address checkV4Address ( String val, ConstraintValidatorContext ctx, ValidNetworkAddress annot ) {
        if ( annot.v4() ) {
            try {
                return IPv4Address.parseV4Address(val);
            }
            catch ( IllegalArgumentException e ) {
                log.debug("V4 Address parsing failed:", e); //$NON-NLS-1$
                ctx.buildConstraintViolationWithTemplate("{ipaddr.illegalV4}").addConstraintViolation(); //$NON-NLS-1$
            }
        }
        else {
            ctx.buildConstraintViolationWithTemplate("{ipaddr.v4NotAllowed}").addConstraintViolation(); //$NON-NLS-1$
        }

        return null;
    }

}
