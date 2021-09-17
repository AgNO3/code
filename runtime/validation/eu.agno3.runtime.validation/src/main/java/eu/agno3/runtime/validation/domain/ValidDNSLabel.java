/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.04.2014 by mbechler
 */
package eu.agno3.runtime.validation.domain;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.Size;


/**
 * @author mbechler
 * 
 */
@Retention ( RetentionPolicy.RUNTIME )
@Documented
@Size ( min = 1, max = 63, message = "{eu.agno3.orchestrator.types.net.validation:label.invalidLength}" )
@Constraint ( validatedBy = {
    DNSLabelValidator.class
} )
public @interface ValidDNSLabel {

    /**
     * 
     * @return whether IDN names are allowed
     */
    boolean allowIdn() default true;


    /**
     * 
     * @return the validation failed message
     */
    String message() default "{eu.agno3.runtime.validation.domain:label.invalid}";


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
