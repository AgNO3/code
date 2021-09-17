/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.01.2017 by mbechler
 */
package eu.agno3.runtime.validation.internal;


import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.executable.ExecutableValidator;
import javax.validation.metadata.BeanDescriptor;

import eu.agno3.runtime.validation.ValidatorContext;


/**
 * @author mbechler
 *
 */
public class ValidatorProxy implements Validator {

    private Validator validator;


    /**
     * @param validator
     */
    public ValidatorProxy ( Validator validator ) {
        this.validator = validator;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.validation.Validator#forExecutables()
     */
    @Override
    public ExecutableValidator forExecutables () {
        return this.validator.forExecutables();
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.validation.Validator#getConstraintsForClass(java.lang.Class)
     */
    @Override
    public BeanDescriptor getConstraintsForClass ( Class<?> arg0 ) {
        return this.validator.getConstraintsForClass(arg0);
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.validation.Validator#unwrap(java.lang.Class)
     */
    @Override
    public <T> T unwrap ( Class<T> arg0 ) {
        return this.validator.unwrap(arg0);
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.validation.Validator#validate(java.lang.Object, java.lang.Class[])
     */
    @Override
    public <T> Set<ConstraintViolation<T>> validate ( T obj, Class<?>... groups ) {
        try {
            pushGroups(groups);
            return this.validator.validate(obj, groups);
        }
        finally {
            popGroups(groups);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.validation.Validator#validateProperty(java.lang.Object, java.lang.String, java.lang.Class[])
     */
    @Override
    public <T> Set<ConstraintViolation<T>> validateProperty ( T obj, String property, Class<?>... groups ) {
        try {
            pushGroups(groups);
            return this.validator.validateProperty(obj, property, groups);
        }
        finally {
            popGroups(groups);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.validation.Validator#validateValue(java.lang.Class, java.lang.String, java.lang.Object,
     *      java.lang.Class[])
     */
    @Override
    public <T> Set<ConstraintViolation<T>> validateValue ( Class<T> cl, String propertyName, Object value, Class<?>... groups ) {
        try {
            pushGroups(groups);
            return this.validator.validateValue(cl, propertyName, value, groups);
        }
        finally {
            popGroups(groups);
        }
    }


    /**
     * @param groups
     */
    protected void popGroups ( Class<?>[] groups ) {
        ValidatorContext.getInstance().pop();
    }


    /**
     * @param groups
     */
    protected void pushGroups ( Class<?>[] groups ) {
        ValidatorContext.getInstance().push(this, groups);
    }

}
