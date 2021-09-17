/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.4.2016 by mbechler
 */
package eu.agno3.fileshare.exceptions;


/**
 * @author mbechler
 *
 */
public class ChunkUploadCanceledException extends UploadException {

    /**
     * 
     */
    private static final long serialVersionUID = 3156534984118218783L;


    /**
     * 
     */
    public ChunkUploadCanceledException () {}


    /**
     * @param msg
     * @param t
     */
    public ChunkUploadCanceledException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public ChunkUploadCanceledException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public ChunkUploadCanceledException ( Throwable cause ) {
        super(cause);
    }

}
