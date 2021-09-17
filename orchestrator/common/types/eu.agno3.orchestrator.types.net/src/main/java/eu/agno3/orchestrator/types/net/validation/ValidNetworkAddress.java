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

import eu.agno3.orchestrator.types.net.NetworkAddressType;


/**
 * @author mbechler
 * 
 */
@Retention ( RetentionPolicy.RUNTIME )
@Documented
@Constraint ( validatedBy = {
    NetworkAddressValidator.class, NetworkAddressStringValidator.class, NetworkAddressListValidator.class
} )
public @interface ValidNetworkAddress {

    /**
     * 
     * @return the allowed address types, default unicast and loopback
     */
    NetworkAddressType[] allowedTypes() default {
        NetworkAddressType.UNICAST, NetworkAddressType.LOOPBACK
    };


    /**
     * 
     * @return whether to allow v4 addresses
     */
    boolean v4() default true;


    /**
     * 
     * @return whether to allow v6 addresses
     */
    boolean v6() default true;


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


    /**
     * @return whether to only check for parsing errors
     */
    boolean parsableOnly() default false;

}
