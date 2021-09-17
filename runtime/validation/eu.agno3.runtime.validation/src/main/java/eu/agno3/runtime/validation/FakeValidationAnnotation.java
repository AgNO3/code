/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.10.2014 by mbechler
 */
package eu.agno3.runtime.validation;


import java.lang.annotation.Annotation;

import javax.validation.Payload;


/**
 * @author mbechler
 *
 */
public class FakeValidationAnnotation {

    private Class<? extends Annotation> annotType;


    /**
     * @param annotType
     * 
     */
    public FakeValidationAnnotation ( Class<? extends Annotation> annotType ) {
        this.annotType = annotType;
    }


    /**
     * 
     * @return annotation type
     */
    public Class<? extends Annotation> annotationType () {
        return this.annotType;
    }


    /**
     * @return message id
     */
    public String message () {
        return null;
    }


    /**
     * @return validation groups
     */
    public Class<?>[] groups () {
        return new Class[] {};
    }


    /**
     * @return payload
     */
    @SuppressWarnings ( "unchecked" )
    public Class<? extends Payload>[] payload () {
        return new Class[] {};
    }
}
