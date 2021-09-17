/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.04.2014 by mbechler
 */
package eu.agno3.runtime.validation.domain;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.Pattern;


/**
 * @author mbechler
 * 
 */
@Retention ( RetentionPolicy.RUNTIME )
@Documented
@Constraint ( validatedBy = {} )
@ValidDNSLabel
@Pattern ( flags = {
    Pattern.Flag.CASE_INSENSITIVE
}, regexp = "[a-z].*", message = "{eu.agno3.orchestrator.types.net.validation:hostname.invalidSyntax}" )
public @interface ValidHostNameLabel {

    /**
     * 
     * @return whether IDN names are allowed
     */
    boolean allowIdn() default true;


    /**
     * 
     * @return the validation failed message
     */
    String message() default "{eu.agno3.runtime.validation.domain:hostname.invalid}";


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
