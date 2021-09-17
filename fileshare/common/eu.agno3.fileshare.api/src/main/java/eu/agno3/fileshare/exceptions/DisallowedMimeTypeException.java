/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.02.2015 by mbechler
 */
package eu.agno3.fileshare.exceptions;


/**
 * @author mbechler
 *
 */
public class DisallowedMimeTypeException extends ContentException {

    /**
     * 
     */
    private static final long serialVersionUID = -9552203191046403L;
    private String mimeType;


    /**
     * 
     */
    public DisallowedMimeTypeException () {}


    /**
     * @param mimeType
     * 
     */
    public DisallowedMimeTypeException ( String mimeType ) {
        super();
        this.mimeType = mimeType;
    }


    /**
     * @param mimeType
     * @param msg
     * @param t
     */
    public DisallowedMimeTypeException ( String mimeType, String msg, Throwable t ) {
        super(msg, t);
        this.mimeType = mimeType;
    }


    /**
     * @param mimeType
     * @param msg
     */
    public DisallowedMimeTypeException ( String mimeType, String msg ) {
        super(msg);
        this.mimeType = mimeType;
    }


    /**
     * @param mimeType
     * @param cause
     */
    public DisallowedMimeTypeException ( String mimeType, Throwable cause ) {
        super(cause);
        this.mimeType = mimeType;
    }


    /**
     * @return the mimeType
     */
    public String getMimeType () {
        return this.mimeType;
    }
}
