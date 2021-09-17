/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.04.2014 by mbechler
 */
package eu.agno3.runtime.validation.util;


import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.MessageInterpolator.Context;
import javax.validation.ValidatorFactory;
import javax.validation.metadata.ConstraintDescriptor;


/**
 * @author mbechler
 * @param <T>
 *            validated value type
 * @param <TAnnot>
 *            constraint type
 * 
 */
public class ValueConstraintValidatorContext <T, TAnnot extends Annotation> implements ConstraintValidatorContext {

    List<ConstraintViolation<Object>> violations = new LinkedList<>();
    private T value;
    private TAnnot annot;
    private ValidatorFactory validatorFactory;


    /**
     * @param value
     * @param annot
     * @param validatorFactory
     */
    public ValueConstraintValidatorContext ( T value, TAnnot annot, ValidatorFactory validatorFactory ) {
        this.value = value;
        this.annot = annot;
        this.validatorFactory = validatorFactory;
    }


    @Override
    public <U> U unwrap ( Class<U> arg0 ) {
        throw new UnsupportedOperationException();
    }


    @Override
    public String getDefaultConstraintMessageTemplate () {
        return null;
    }


    @Override
    public void disableDefaultConstraintViolation () {}


    @Override
    public ConstraintViolationBuilder buildConstraintViolationWithTemplate ( final String messageTpl ) {
        return new FakeConstraintViolationBuilder<>(this, messageTpl);
    }


    /**
     * @param constraintViolation
     */
    public void addViolation ( ConstraintViolation<Object> constraintViolation ) {
        this.violations.add(constraintViolation);
    }


    /**
     * @return the value class
     */
    @SuppressWarnings ( "unchecked" )
    public Class<T> getValueClass () {
        return (Class<T>) this.value.getClass();
    }


    /**
     * @return the validated value
     */
    public T getValue () {
        return this.value;
    }


    /**
     * @return the collected set of violations
     */
    public Set<ConstraintViolation<Object>> getViolations () {
        return new HashSet<>(this.violations);
    }


    /**
     * @return the constraint annotation
     */
    public TAnnot getAnnotation () {
        return this.annot;
    }


    /**
     * @param messageTpl
     * @param constraintDescriptor
     * @return the interpolated message
     */
    public String getMessage ( String messageTpl, final ConstraintDescriptor<?> constraintDescriptor ) {
        return this.validatorFactory.getMessageInterpolator().interpolate(messageTpl, new Context() {

            @Override
            public <U> U unwrap ( Class<U> arg0 ) {
                throw new UnsupportedOperationException();
            }


            @Override
            public Object getValidatedValue () {
                return getValue();
            }


            @Override
            public ConstraintDescriptor<?> getConstraintDescriptor () {
                return constraintDescriptor;
            }
        });
    }
}