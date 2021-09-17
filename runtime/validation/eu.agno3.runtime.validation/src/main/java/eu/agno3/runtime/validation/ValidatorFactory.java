/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.10.2013 by mbechler
 */
package eu.agno3.runtime.validation;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.validation.ConstraintValidator;


/**
 * @author mbechler
 * 
 */
@Retention ( RetentionPolicy.RUNTIME )
@Documented
public @interface ValidatorFactory {

    /**
     * 
     * @return ConstraintValidator class created by this factory
     */
    Class<? extends ConstraintValidator<?, ?>> value();
}
