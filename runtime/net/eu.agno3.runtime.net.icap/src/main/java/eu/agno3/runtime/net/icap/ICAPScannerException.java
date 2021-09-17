/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.06.2015 by mbechler
 */
package eu.agno3.runtime.net.icap;


/**
 * @author mbechler
 *
 */
public class ICAPScannerException extends Exception {

    private String signature;


    /**
     * 
     */
    public ICAPScannerException () {
        super();
    }


    /**
     * @param signature
     * @param message
     */
    public ICAPScannerException ( String signature, String message ) {
        super(message);
        this.signature = signature;
    }


    /**
     * @param signature
     * @param message
     * @param cause
     */
    public ICAPScannerException ( String signature, String message, Throwable cause ) {
        super(message, cause);
        this.signature = signature;
    }


    /**
     * @param signature
     * @param cause
     */
    public ICAPScannerException ( String signature, Throwable cause ) {
        super(cause);
        this.signature = signature;
    }


    /**
     * @return the signature
     */
    public String getSignature () {
        return this.signature;
    }

    /**
     * 
     */
    private static final long serialVersionUID = -1064090513099765342L;

}
