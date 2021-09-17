/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.01.2016 by mbechler
 */
package eu.agno3.orchestrator.types.validation;


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
    SIDStringValidator.class, SIDCollectionValidator.class
} )
public @interface ValidSID {

    /**
     * 
     * @return validation groups
     */
    Class<?>[] groups() default {};


    /**
     * 
     * @return the validation failed message
     */
    String message() default "{sid.invalid}";


    /**
     * 
     * @return payload
     */
    Class<? extends Payload>[] payload() default {};

}
