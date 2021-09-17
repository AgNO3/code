/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.01.2017 by mbechler
 */
package eu.agno3.runtime.validation;


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
@Constraint ( validatedBy = ConditionalValidator.class )
public @interface ValidConditional {

    /**
     * 
     * @return EL condition to evaluate
     */
    String when();


    /**
     * 
     * @return the validation failed message
     */
    String message() default "{eu.agno3.runtime.validation:object.invalid}";


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
