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


/**
 * @author mbechler
 * 
 */
@Retention ( RetentionPolicy.RUNTIME )
@Documented
@Constraint ( validatedBy = {
    NetworkSpecificationValidator.class, NetworkSpecificationStringValidator.class, NetworkSpecificationListValidator.class
} )
public @interface ValidNetworkSpecification {

    /**
     * 
     * @return restriction on the given address
     */
    ValidNetworkAddress restrictAddress() default @ValidNetworkAddress;


    /**
     * 
     * @return require that the given address is the network address
     */
    boolean requireNetworkAddress() default false;


    /**
     * 
     * @return whether to allow address only specification, should be interpreted as full match
     */
    boolean allowNoPrefix() default false;


    /**
     * 
     * @return the validation failed message
     */
    String message() default "{network.invalid}";


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
