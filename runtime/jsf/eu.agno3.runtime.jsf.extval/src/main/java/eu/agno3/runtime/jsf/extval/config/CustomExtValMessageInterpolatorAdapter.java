/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 10, 2017 by mbechler
 */
package eu.agno3.runtime.jsf.extval.config;


import java.util.Objects;

import javax.validation.MessageInterpolator;

import org.apache.myfaces.extensions.validator.beanval.validation.message.interpolator.ExtValMessageInterpolatorAdapter;
import org.apache.myfaces.extensions.validator.core.validation.message.resolver.MessageResolver;


/**
 * @author mbechler
 *
 */
public class CustomExtValMessageInterpolatorAdapter extends ExtValMessageInterpolatorAdapter {

    private MessageInterpolator wrapped;
    private MessageResolver messageResolver;


    /**
     * @param wrapped
     * @param messageResolver
     */
    public CustomExtValMessageInterpolatorAdapter ( MessageInterpolator wrapped, MessageResolver messageResolver ) {
        super(wrapped, messageResolver);
        this.wrapped = wrapped;
        this.messageResolver = messageResolver;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode () {
        return Objects.hash(this.wrapped, this.messageResolver);
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {

        if ( ! ( obj instanceof CustomExtValMessageInterpolatorAdapter ) ) {
            return false;
        }

        CustomExtValMessageInterpolatorAdapter o = (CustomExtValMessageInterpolatorAdapter) obj;

        return Objects.equals(this.wrapped, o.wrapped) && Objects.equals(this.wrapped, o.wrapped);
    }

}
