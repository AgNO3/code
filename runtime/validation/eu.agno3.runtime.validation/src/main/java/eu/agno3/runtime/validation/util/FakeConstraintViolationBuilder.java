/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.04.2014 by mbechler
 */
package eu.agno3.runtime.validation.util;


import java.lang.annotation.Annotation;

import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;


class FakeConstraintViolationBuilder <T, TAnnot extends Annotation> implements ConstraintViolationBuilder {

    private ValueConstraintValidatorContext<T, TAnnot> context;
    private String messageTpl;


    /**
     * @param context
     * @param messageTpl
     */
    public FakeConstraintViolationBuilder ( ValueConstraintValidatorContext<T, TAnnot> context, String messageTpl ) {
        this.context = context;
        this.messageTpl = messageTpl;
    }


    @Override
    public LeafNodeBuilderCustomizableContext addBeanNode () {
        throw new UnsupportedOperationException();
    }


    @Override
    public ConstraintValidatorContext addConstraintViolation () {
        this.context.addViolation(new FakeConstraintViolation<>(this.context, this.messageTpl));
        return this.context;
    }


    @Override
    public NodeBuilderDefinedContext addNode ( String arg0 ) {
        throw new UnsupportedOperationException();
    }


    @Override
    public NodeBuilderDefinedContext addParameterNode ( int arg0 ) {
        throw new UnsupportedOperationException();
    }


    @Override
    public NodeBuilderCustomizableContext addPropertyNode ( String arg0 ) {
        throw new UnsupportedOperationException();
    }

}