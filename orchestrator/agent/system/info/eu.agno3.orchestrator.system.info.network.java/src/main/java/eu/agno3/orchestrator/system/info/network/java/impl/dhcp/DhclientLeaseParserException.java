/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.11.2016 by mbechler
 */
package eu.agno3.orchestrator.system.info.network.java.impl.dhcp;


/**
 * @author mbechler
 *
 */
public class DhclientLeaseParserException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -7574449756350950564L;


    /**
     * 
     */
    public DhclientLeaseParserException () {
        super();
    }


    /**
     * @param message
     * @param cause
     */
    public DhclientLeaseParserException ( String message, Throwable cause ) {
        super(message, cause);
    }


    /**
     * @param message
     */
    public DhclientLeaseParserException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public DhclientLeaseParserException ( Throwable cause ) {
        super(cause);
    }

}
