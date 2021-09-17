/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.07.2013 by mbechler
 */
package eu.agno3.runtime.http.service.handler;


import org.eclipse.jetty.server.Handler;


/**
 * @author mbechler
 * 
 */
public interface ExtendedHandler extends Handler {

    /**
     * @return the context's name
     */
    String getContextName ();


    /**
     * @return the virtualhost this handler binds to, null if bind to all
     */
    String[] getVirtualHosts ();


    /**
     * @return path under which the handler shall be registered
     */
    String getContextPath ();


    /**
     * @return priority used when ordering handlers
     */
    float getPriority ();
}
