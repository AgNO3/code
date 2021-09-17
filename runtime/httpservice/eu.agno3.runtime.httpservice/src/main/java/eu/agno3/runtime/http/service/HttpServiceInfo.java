/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.07.2013 by mbechler
 */
package eu.agno3.runtime.http.service;


import java.net.URL;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;

import eu.agno3.runtime.http.service.connector.ConnectorFactory;


/**
 * @author mbechler
 * 
 */
public interface HttpServiceInfo {

    /**
     * Active context handlers
     * 
     * @return the list of active context handlers
     */
    List<ContextHandler> getContextHandlers ();


    /**
     * Registered connector factories
     * 
     * @return a list of active connector factories
     */
    List<ConnectorFactory> getConnectorFactories ();


    /**
     * 
     * @param key
     * @return the connector
     */
    List<ServerConnector> getActiveConnectors ( String key );


    /**
     * 
     * @param key
     * @param ctx
     * @param overrideHostname
     * @return the full context base URL
     * @throws ServletException
     */
    URL getContextBaseUrl ( String key, ServletContext ctx, String overrideHostname ) throws ServletException;


    /**
     * @param key
     * @param contextPath
     * @param overrideHostname
     * @return the full context base URL
     * @throws ServletException
     */
    URL getContextBaseUrl ( String key, String contextPath, String overrideHostname ) throws ServletException;

}
