/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 10, 2017 by mbechler
 */
package eu.agno3.runtime.jsf.extval.config;


import javax.validation.MessageInterpolator;

import org.apache.myfaces.extensions.validator.beanval.ExtValBeanValidationContext;
import org.apache.myfaces.extensions.validator.beanval.validation.message.interpolator.ExtValMessageInterpolatorAdapter;
import org.apache.myfaces.extensions.validator.core.validation.message.resolver.MessageResolver;


/**
 * @author mbechler
 *
 */
public class CustomExtValBeanValidationContext extends ExtValBeanValidationContext {

    private ExtValMessageInterpolatorAdapter messageResolverAdapter;


    /**
     * @param resolver
     * 
     */
    public CustomExtValBeanValidationContext ( MessageResolver resolver ) {
        this.messageResolver = resolver;
    }


    @Override
    public MessageInterpolator getMessageInterpolator () {
        if ( this.messageResolver != null ) {
            if ( this.messageResolverAdapter == null ) {
                this.messageResolverAdapter = new CustomExtValMessageInterpolatorAdapter(this.defaultMessageInterpolator, this.messageResolver);
            }
            return this.messageResolverAdapter;
        }
        return this.defaultMessageInterpolator;
    }


    @Override
    protected void initMessageResolver () {}
}
