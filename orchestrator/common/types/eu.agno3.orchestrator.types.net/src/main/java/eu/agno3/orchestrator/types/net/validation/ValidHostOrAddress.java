/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.04.2014 by mbechler
 */
package eu.agno3.orchestrator.types.net.validation;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.validation.Constraint;
import javax.validation.Payload;

import eu.agno3.runtime.validation.domain.ValidFQDN;


/**
 * @author mbechler
 * 
 */
@Retention ( RetentionPolicy.RUNTIME )
@Documented
@Constraint ( validatedBy = {
    HostOrAddressValidator.class, HostOrAddressStringValidator.class, HostOrAddressListValidator.class, HostOrAddressStringListValidator.class
} )
public @interface ValidHostOrAddress {

    /**
     * 
     * @return check if a hostname
     */
    ValidFQDN host() default @ValidFQDN ( allowIdn = true );


    /**
     * 
     * @return check if an address
     */
    ValidNetworkAddress addr() default @ValidNetworkAddress ( );


    /**
     * 
     * @return the validation failed message
     */
    String message() default "{hostoraddress.invalid}";


    /**
     * 
     * @return validation groups
     */
    Class<?>[] groups() default {};


    /**
     * 
     * @return payload
     */
    Class<? extends Payload>[] payload() default {};
}
