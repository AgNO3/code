/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.03.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.ntlm;


/**
 * @author mbechler
 *
 */
public class NTLMException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1009968505735340041L;


    /**
     * 
     */
    public NTLMException () {
        super();
    }


    /**
     * @param message
     * @param cause
     */
    public NTLMException ( String message, Throwable cause ) {
        super(message, cause);
    }


    /**
     * @param message
     */
    public NTLMException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public NTLMException ( Throwable cause ) {
        super(cause);
    }

}
