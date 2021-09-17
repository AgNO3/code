/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.12.2014 by mbechler
 */
package eu.agno3.orchestrator.server.gui.async;


import org.primefaces.push.EventBus;
import org.primefaces.push.RemoteEndpoint;


/**
 * @author mbechler
 *
 */
public interface CometConnectionListener {

    /**
     * @param r
     * @param e
     */
    void onOpen ( RemoteEndpoint r, EventBus e );


    /**
     * @param r
     * @param e
     */
    void onClose ( RemoteEndpoint r, EventBus e );

}