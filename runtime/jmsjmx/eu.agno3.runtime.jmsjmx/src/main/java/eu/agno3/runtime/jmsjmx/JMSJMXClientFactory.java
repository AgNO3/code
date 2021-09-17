/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.04.2015 by mbechler
 */
package eu.agno3.runtime.jmsjmx;


import javax.management.MalformedObjectNameException;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.addressing.MessageSource;


/**
 * @author mbechler
 *
 */
public interface JMSJMXClientFactory {

    /**
     * @param type
     * @param target
     * @return a JMSJMXClient for the given target
     * @throws MalformedObjectNameException
     */
    JMSJMXClient getClient ( AbstractJMXRequest<@NonNull MessageSource, ? extends JMXErrorResponse> type ) throws MalformedObjectNameException;

}