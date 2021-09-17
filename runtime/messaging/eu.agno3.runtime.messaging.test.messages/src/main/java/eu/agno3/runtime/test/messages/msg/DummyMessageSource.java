/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.09.2013 by mbechler
 */
package eu.agno3.runtime.test.messages.msg;


import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.addressing.MessageSourceRegistration;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    MessageSource.class, MessageSourceRegistration.class
}, property = "type=dummy" )
public class DummyMessageSource implements MessageSource, MessageSourceRegistration {

    /**
     * 
     */
    private static final long serialVersionUID = -4257609295802574909L;


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.addressing.MessageSource#encode()
     */
    @Override
    public String encode () {
        return "dummy:"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.addressing.MessageSource#parse(java.lang.String)
     */
    @Override
    public void parse ( String encoded ) {
        // ignore
    }


    @Override
    public boolean equals ( Object o ) {
        return o instanceof DummyMessageSource;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode () {
        return 0;
    }
}
