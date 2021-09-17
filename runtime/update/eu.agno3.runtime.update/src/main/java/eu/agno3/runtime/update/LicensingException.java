/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.02.2016 by mbechler
 */
package eu.agno3.runtime.update;


/**
 * @author mbechler
 *
 */
public class LicensingException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 7445152841346719096L;


    /**
     * 
     */
    public LicensingException () {
        super();
    }


    /**
     * @param message
     * @param cause
     */
    public LicensingException ( String message, Throwable cause ) {
        super(message, cause);
    }


    /**
     * @param message
     */
    public LicensingException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public LicensingException ( Throwable cause ) {
        super(cause);
    }

}
