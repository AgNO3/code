/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.04.2014 by mbechler
 */
package eu.agno3.runtime.validation.util;


import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintTarget;
import javax.validation.ConstraintValidator;
import javax.validation.Payload;
import javax.validation.metadata.ConstraintDescriptor;


/**
 * @author mbechler
 * @param <TAnnot>
 * 
 */
public class FakeConstraintDescriptor <TAnnot extends Annotation> implements ConstraintDescriptor<TAnnot> {

    private String messageTemplate;
    private TAnnot annotation;


    /**
     * @param messageTemplate
     * @param annotation
     */
    public FakeConstraintDescriptor ( String messageTemplate, TAnnot annotation ) {
        super();
        this.messageTemplate = messageTemplate;
        this.annotation = annotation;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.validation.metadata.ConstraintDescriptor#getAnnotation()
     */
    @Override
    public TAnnot getAnnotation () {
        return this.annotation;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.validation.metadata.ConstraintDescriptor#getAttributes()
     */
    @Override
    public Map<String, Object> getAttributes () {
        return Collections.EMPTY_MAP;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.validation.metadata.ConstraintDescriptor#getComposingConstraints()
     */
    @Override
    public Set<ConstraintDescriptor<?>> getComposingConstraints () {
        return Collections.EMPTY_SET;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.validation.metadata.ConstraintDescriptor#getConstraintValidatorClasses()
     */
    @Override
    public List<Class<? extends ConstraintValidator<TAnnot, ?>>> getConstraintValidatorClasses () {
        return Collections.EMPTY_LIST;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.validation.metadata.ConstraintDescriptor#getGroups()
     */
    @Override
    public Set<Class<?>> getGroups () {
        return Collections.EMPTY_SET;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.validation.metadata.ConstraintDescriptor#getMessageTemplate()
     */
    @Override
    public String getMessageTemplate () {
        return this.messageTemplate;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.validation.metadata.ConstraintDescriptor#getPayload()
     */
    @Override
    public Set<Class<? extends Payload>> getPayload () {
        return Collections.EMPTY_SET;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.validation.metadata.ConstraintDescriptor#getValidationAppliesTo()
     */
    @Override
    public ConstraintTarget getValidationAppliesTo () {
        return ConstraintTarget.IMPLICIT;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.validation.metadata.ConstraintDescriptor#isReportAsSingleViolation()
     */
    @Override
    public boolean isReportAsSingleViolation () {
        return false;
    }

}
