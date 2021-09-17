/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.06.2014 by mbechler
 */
package eu.agno3.orchestrator.server.component;


import java.net.URI;
import java.util.UUID;


/**
 * @author mbechler
 * 
 */
public interface ComponentConfig {

    /**
     * 
     * @return component id
     */
    UUID getId ();


    /**
     * 
     * @return the timeout value after which the server will consider the component dead, in seconds
     */
    int getPingTimeout ();


    /**
     * 
     * @return the base address of webservice endpoints
     */
    URI getWebServiceBaseAddress ();


    /**
     * @return the outgoing event queue for this component
     */
    String getEventOutQueue ();


    /**
     * @return the event listening topic for this component
     */
    String getEventTopic ();

}
