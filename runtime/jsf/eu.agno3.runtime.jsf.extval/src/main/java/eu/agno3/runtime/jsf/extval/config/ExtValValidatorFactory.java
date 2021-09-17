/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.06.2017 by mbechler
 */
package eu.agno3.runtime.jsf.extval.config;

import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.ParameterNameProvider;
import javax.validation.TraversableResolver;
import javax.validation.Validator;
import javax.validation.ValidatorContext;
import javax.validation.ValidatorFactory;


/**
 * @author mbechler
 *
 */
public class ExtValValidatorFactory implements ValidatorFactory {

    /**
      * {@inheritDoc}
      *
      * @see javax.validation.ValidatorFactory#close()
      */
    @Override
    public void close () {
        // TODO Auto-generated method stub

    }


    /**
      * {@inheritDoc}
      *
      * @see javax.validation.ValidatorFactory#getConstraintValidatorFactory()
      */
    @Override
    public ConstraintValidatorFactory getConstraintValidatorFactory () {
        // TODO Auto-generated method stub
        return null;
    }


    /**
      * {@inheritDoc}
      *
      * @see javax.validation.ValidatorFactory#getMessageInterpolator()
      */
    @Override
    public MessageInterpolator getMessageInterpolator () {
        // TODO Auto-generated method stub
        return null;
    }


    /**
      * {@inheritDoc}
      *
      * @see javax.validation.ValidatorFactory#getParameterNameProvider()
      */
    @Override
    public ParameterNameProvider getParameterNameProvider () {
        // TODO Auto-generated method stub
        return null;
    }


    /**
      * {@inheritDoc}
      *
      * @see javax.validation.ValidatorFactory#getTraversableResolver()
      */
    @Override
    public TraversableResolver getTraversableResolver () {
        // TODO Auto-generated method stub
        return null;
    }


    /**
      * {@inheritDoc}
      *
      * @see javax.validation.ValidatorFactory#getValidator()
      */
    @Override
    public Validator getValidator () {
        // TODO Auto-generated method stub
        return null;
    }


    /**
      * {@inheritDoc}
      *
      * @see javax.validation.ValidatorFactory#unwrap(java.lang.Class)
      */
    @Override
    public <T> T unwrap ( Class<T> arg0 ) {
        // TODO Auto-generated method stub
        return null;
    }


    /**
      * {@inheritDoc}
      *
      * @see javax.validation.ValidatorFactory#usingContext()
      */
    @Override
    public ValidatorContext usingContext () {
        // TODO Auto-generated method stub
        return null;
    }

}
