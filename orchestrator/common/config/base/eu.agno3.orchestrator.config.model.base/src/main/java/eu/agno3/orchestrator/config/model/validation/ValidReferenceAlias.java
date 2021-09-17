/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.validation;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;


/**
 * @author mbechler
 *
 */
@Retention ( RetentionPolicy.RUNTIME )
@Documented
@Constraint ( validatedBy = {} )
@Pattern ( flags = Pattern.Flag.CASE_INSENSITIVE, regexp = "\\p{Alpha}\\w*" )
@Size ( min = 1, max = 64 )
@ReportAsSingleViolation
public @interface ValidReferenceAlias {

    /**
     * 
     * @return message id
     */
    // TODO: I18N
    String message() default "Invalid alias";


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
