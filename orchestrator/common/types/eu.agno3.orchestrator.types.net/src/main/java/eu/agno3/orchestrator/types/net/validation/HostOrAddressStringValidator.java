/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.04.2014 by mbechler
 */
package eu.agno3.orchestrator.types.net.validation;


import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import eu.agno3.orchestrator.types.net.AbstractIPAddress;
import eu.agno3.runtime.validation.domain.FQDNStringValidator;
import eu.agno3.runtime.validation.domain.ValidFQDN;


/**
 * @author mbechler
 * 
 */
public class HostOrAddressStringValidator implements ConstraintValidator<ValidHostOrAddress, String> {

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
    public boolean isValid ( String val, ConstraintValidatorContext ctx ) {
        if ( val == null ) {
            return true;
        }
        ctx.disableDefaultConstraintViolation();
        return checkHostOrAddress(val, ctx, this.addr, this.host);
    }


    /**
     * @param val
     * @param ctx
     * @param addr
     * @param host
     * @return whether the given host name or address is valid
     */
    public static boolean checkHostOrAddress ( String val, ConstraintValidatorContext ctx, ValidNetworkAddress addr, ValidFQDN host ) {
        if ( AbstractIPAddress.isIPAddress(val) ) {
            return NetworkAddressStringValidator.checkNetworkAddress(val, ctx, addr);
        }

        return FQDNStringValidator.checkFQDN(val, ctx, host.allowIdn());
    }

}
