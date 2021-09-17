/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.09.2013 by mbechler
 */
package eu.agno3.runtime.messaging.xml.test.messages.msg;


import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.addressing.MessageSourceRegistration;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    MessageSource.class, MessageSourceRegistration.class
}, property = "type=dummyxml" )
public class DummyMessageSource implements MessageSource, MessageSourceRegistration {

    /**
     * 
     */
    private static final long serialVersionUID = 842388631013512279L;


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.addressing.MessageSource#encode()
     */
    @Override
    public String encode () {
        return "dummyxml:"; //$NON-NLS-1$
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

}
