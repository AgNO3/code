/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.04.2014 by mbechler
 */
package eu.agno3.runtime.validation.util;


import java.lang.annotation.Annotation;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;

import org.eclipse.jdt.annotation.NonNull;


/**
 * @author mbechler
 * 
 */
final class FakeConstraintViolation <T, TAnnot extends Annotation> implements ConstraintViolation<Object> {

    private ValueConstraintValidatorContext<T, TAnnot> context;
    private String messageTpl;


    /**
     * @param context
     * @param messageTpl
     * 
     */
    public FakeConstraintViolation ( ValueConstraintValidatorContext<T, TAnnot> context, String messageTpl ) {
        this.context = context;
        this.messageTpl = messageTpl;
    }


    @Override
    public ConstraintDescriptor<?> getConstraintDescriptor () {
        return new FakeConstraintDescriptor<>(this.messageTpl, this.context.getAnnotation());
    }


    @Override
    public Object[] getExecutableParameters () {
        throw new UnsupportedOperationException();
    }


    @Override
    public Object getExecutableReturnValue () {
        throw new UnsupportedOperationException();
    }


    @Override
    public Object getInvalidValue () {
        return this.context.getValue();
    }


    @Override
    public Object getLeafBean () {
        return this.context.getValue();
    }


    @Override
    public String getMessage () {
        return this.context.getMessage(this.messageTpl, this.getConstraintDescriptor());
    }


    @Override
    public String getMessageTemplate () {
        return this.messageTpl;
    }


    @Override
    public Path getPropertyPath () {
        throw new UnsupportedOperationException();
    }


    @Override
    public T getRootBean () {
        return this.context.getValue();
    }


    @SuppressWarnings ( {
        "rawtypes", "unchecked"
    } )
    @Override
    public Class getRootBeanClass () {
        return this.context.getValueClass();
    }


    @Override
    public <@NonNull U> U unwrap ( Class<U> arg0 ) {
        throw new UnsupportedOperationException();
    }
}