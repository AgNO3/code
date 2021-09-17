/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.09.2013 by mbechler
 */
package eu.agno3.orchestrator.server.connector;


/**
 * @author mbechler
 * 
 */
public class ServerConnectorException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 2757718001355947477L;


    /**
     * 
     */
    public ServerConnectorException () {}


    /**
     * @param msg
     */
    public ServerConnectorException ( String msg ) {
        super(msg);
    }


    /**
     * @param t
     */
    public ServerConnectorException ( Throwable t ) {
        super(t);
    }


    /**
     * @param msg
     * @param t
     */
    public ServerConnectorException ( String msg, Throwable t ) {
        super(msg, t);
    }

}
