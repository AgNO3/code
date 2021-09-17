/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.10.2013 by mbechler
 */
package eu.agno3.runtime.validation;


import javax.validation.ConstraintValidator;


/**
 * @author mbechler
 * @param <T>
 *            ConstraintValidator class
 * 
 */
public interface ConstraintValidatorFactory <T extends ConstraintValidator<?, ?>> {

    /**
     * 
     * @return a new validator instance
     */
    T createValidator ();
}
