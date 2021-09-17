/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.01.2015 by mbechler
 */
package eu.agno3.fileshare.exceptions;


/**
 * @author mbechler
 *
 */
public class FiletypeDetectionException extends ContentException {

    /**
     * 
     */
    private static final long serialVersionUID = 1257217921306072630L;


    /**
     * 
     */
    public FiletypeDetectionException () {}


    /**
     * @param msg
     * @param t
     */
    public FiletypeDetectionException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public FiletypeDetectionException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public FiletypeDetectionException ( Throwable cause ) {
        super(cause);
    }

}
