/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.03.2015 by mbechler
 */
package eu.agno3.runtime.validation.email;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;


/**
 * @author mbechler
 *
 */
@Retention ( RetentionPolicy.RUNTIME )
@Documented
@Target ( value = {
    ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE
} )
@Constraint ( validatedBy = EMailValidator.class )
public @interface ValidEmail {

    /**
     * 
     * @return whether to check the domain part via DNS
     */
    boolean hostDNSValidate() default false;


    /**
     * 
     * @return whether to reject IP addresses for the domain part
     */
    boolean noIPAddresses() default true;


    /**
     * @return whether to allow IDN domains
     */
    boolean allowIDN() default true;


    /**
     * 
     * @return whether to allow escapes in local part
     */
    boolean allowEscapes() default false;


    /**
     * 
     * @return the maximum length for the whole address
     */
    int maxLength() default 320;


    /**
     * 
     * @return validation error message
     */
    String message() default "";


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
