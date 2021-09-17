/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.04.2014 by mbechler
 */
package eu.agno3.runtime.jsf.types.base;


import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.el.ValueExpression;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.validation.ConstraintViolation;
import javax.validation.ValidatorFactory;
import javax.validation.executable.ExecutableValidator;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.runtime.jsf.i18n.BaseMessages;
import eu.agno3.runtime.validation.util.ValueConstraintValidatorContext;


/**
 * @author mbechler
 * 
 * @param <T>
 * @param <TConstraint>
 */
public abstract class AbstractValidatingConverter <T, TConstraint extends Annotation> implements Converter {

    private static final String LABEL = "label"; //$NON-NLS-1$

    private static final String CONVERTER_INTERNAL_ERROR = "Converter internal error:"; //$NON-NLS-1$

    private static final String CONVERT_FROM_STRING = "convertFromString"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(AbstractValidatingConverter.class);


    /**
     * 
     */
    public AbstractValidatingConverter () {
        super();
    }


    /**
     * @param obj
     * @return the object as string
     */
    public abstract String convertFromObject ( T obj );


    protected abstract Class<T> getObjectClass ();


    protected abstract ValidatorFactory getValidatorFactory ();


    protected abstract void validateObjectValue ( TConstraint constraint, T res, ValueConstraintValidatorContext<T, TConstraint> ctx );


    protected abstract TConstraint makeConstraint ( UIComponent c );


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.convert.Converter#getAsObject(javax.faces.context.FacesContext,
     *      javax.faces.component.UIComponent, java.lang.String)
     */
    @Override
    public Object getAsObject ( FacesContext ctx, UIComponent comp, String val ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Converting from string " + val); //$NON-NLS-1$
        }

        if ( val == null || val.isEmpty() ) {
            return null;
        }

        Set<ConstraintViolation<Object>> violations;
        Method m;
        try {
            m = this.getClass().getMethod(CONVERT_FROM_STRING, String.class);
            violations = getViolations(val, m);
        }
        catch ( Exception e ) {
            log.warn(CONVERTER_INTERNAL_ERROR, e);
            throw new ConverterException(CONVERTER_INTERNAL_ERROR, e);
        }

        handleViolations(ctx, comp, violations);
        try {
            @SuppressWarnings ( "unchecked" )
            T res = (T) m.invoke(this, val);
            TConstraint constraint = this.makeConstraint(comp);
            ValueConstraintValidatorContext<T, TConstraint> context = this.makeFakeConstraintValidatorContext(res, constraint);
            this.validateObjectValue(constraint, res, context);
            handleViolations(ctx, comp, context.getViolations());
            return res;
        }
        catch (
            IllegalAccessException |
            IllegalArgumentException |
            InvocationTargetException e ) {
            log.warn(CONVERTER_INTERNAL_ERROR, e);
            throw new ConverterException(CONVERTER_INTERNAL_ERROR, e);
        }
    }


    /**
     * @param res
     * @return
     */
    private ValueConstraintValidatorContext<T, TConstraint> makeFakeConstraintValidatorContext ( T res, TConstraint annot ) {
        return new ValueConstraintValidatorContext<>(res, annot, this.getValidatorFactory());
    }


    private static void handleViolations ( FacesContext ctx, UIComponent comp, Set<ConstraintViolation<Object>> violations ) {
        if ( !violations.isEmpty() ) {
            List<String> messages = new LinkedList<>();
            for ( ConstraintViolation<Object> violation : violations ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Found violation " + violation); //$NON-NLS-1$
                }
                messages.add(violation.getMessage());
            }

            throw new ConverterException(makeValidationFailedMessage(ctx, comp, messages));
        }
    }


    private static FacesMessage makeValidationFailedMessage ( FacesContext ctx, UIComponent comp, List<String> messages ) {
        String summaryString = BaseMessages.format("validatingConverter.failedFmt", getLabel(ctx, comp)); //$NON-NLS-1$
        String detailString = StringUtils.join(messages, ',');
        return new FacesMessage(FacesMessage.SEVERITY_ERROR, summaryString, detailString);
    }


    // from Myfaces javax.faces.convert._MessageUtils
    static Object getLabel ( FacesContext facesContext, UIComponent component ) {
        Object label = component.getAttributes().get(LABEL);
        ValueExpression expression = null;
        if ( label instanceof String && ! ( (String) label ).isEmpty() ) {
            expression = component.getValueExpression(LABEL);
            if ( expression != null ) {
                label = null;
            }
        }

        if ( label != null ) {
            return label;
        }

        expression = ( expression == null ) ? component.getValueExpression(LABEL) : expression;
        if ( expression != null ) {
            return expression.getValue(facesContext.getELContext());
        }

        // If no label is not specified, use clientId
        return component.getClientId(facesContext);
    }


    private Set<ConstraintViolation<Object>> getViolations ( String val, Method m ) {
        ExecutableValidator execValidator = this.getValidatorFactory().getValidator().forExecutables();
        Set<ConstraintViolation<Object>> violations = execValidator.validateParameters((Object) this, m, new Object[] {
            val
        });
        return violations;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.convert.Converter#getAsString(javax.faces.context.FacesContext,
     *      javax.faces.component.UIComponent, java.lang.Object)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public String getAsString ( FacesContext ctx, UIComponent comp, Object val ) {
        if ( val == null || !this.getObjectClass().isAssignableFrom(val.getClass()) ) {
            return null;
        }

        return convertFromObject((T) val);
    }

}