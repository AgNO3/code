/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.01.2017 by mbechler
 */
package eu.agno3.runtime.validation.internal;


import javax.el.ExpressionFactory;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.validation.ConditionalValidator;
import eu.agno3.runtime.validation.ConstraintValidatorFactory;
import eu.agno3.runtime.validation.ValidatorFactory;


/**
 * @author mbechler
 *
 */
@Component ( service = ConstraintValidatorFactory.class )
@ValidatorFactory ( ConditionalValidator.class )
public class ConditionalConstraintValidatorFactory implements ConstraintValidatorFactory<ConditionalValidator> {

    private ExpressionFactory expressionFactory;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.expressionFactory = ExpressionFactory.newInstance();
    }


    protected synchronized void deactivate ( ComponentContext ctx ) {
        this.expressionFactory = null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.validation.ConstraintValidatorFactory#createValidator()
     */
    @Override
    public ConditionalValidator createValidator () {
        return new ConditionalValidator(this.expressionFactory);
    }

}
