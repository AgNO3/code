/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.06.2015 by mbechler
 */
package eu.agno3.fileshare.exceptions;


/**
 * @author mbechler
 *
 */
public class ContentVirusException extends ContentException {

    /**
     * 
     */
    private static final long serialVersionUID = -7735464154774388291L;
    private String signature;


    /**
     * 
     */
    public ContentVirusException () {
        super();
    }


    /**
     * @param signature
     * @param msg
     * @param t
     */
    public ContentVirusException ( String signature, String msg, Throwable t ) {
        super(msg, t);
        this.signature = signature;
    }


    /**
     * @param signature
     * @param msg
     */
    public ContentVirusException ( String signature, String msg ) {
        super(msg);
        this.signature = signature;
    }


    /**
     * @param signature
     * @param cause
     */
    public ContentVirusException ( String signature, Throwable cause ) {
        super(cause);
        this.signature = signature;
    }


    /**
     * @return the signature
     */
    public String getSignature () {
        return this.signature;
    }

}
