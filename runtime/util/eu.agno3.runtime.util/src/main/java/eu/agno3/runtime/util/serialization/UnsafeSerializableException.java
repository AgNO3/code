/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.11.2015 by mbechler
 */
package eu.agno3.runtime.util.serialization;


/**
 * @author mbechler
 *
 */
public class UnsafeSerializableException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 3188060272482230126L;


    /**
     * 
     */
    public UnsafeSerializableException () {
        super();
    }


    /**
     * @param message
     * @param cause
     */
    public UnsafeSerializableException ( String message, Throwable cause ) {
        super(message, cause);
    }


    /**
     * @param message
     */
    public UnsafeSerializableException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public UnsafeSerializableException ( Throwable cause ) {
        super(cause);
    }

}
