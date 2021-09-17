/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.06.2015 by mbechler
 */
package eu.agno3.fileshare.exceptions;


/**
 * @author mbechler
 *
 */
public class EntityNameBadCharactersException extends InvalidEntityNameException {

    /**
     * 
     */
    private static final long serialVersionUID = -2832827183063097487L;
    private String badCharsStr;


    /**
     * @param invalidName
     * @param badCharsStr
     * @param msg
     * @param t
     */
    public EntityNameBadCharactersException ( String invalidName, String badCharsStr, String msg, Throwable t ) {
        super(invalidName, msg, t);
        this.badCharsStr = badCharsStr;
    }


    /**
     * @param invalidName
     * @param badCharsStr
     * @param msg
     */
    public EntityNameBadCharactersException ( String invalidName, String badCharsStr, String msg ) {
        super(invalidName, msg);
        this.badCharsStr = badCharsStr;
    }


    /**
     * @param invalidName
     * @param badCharsStr
     * @param cause
     */
    public EntityNameBadCharactersException ( String invalidName, String badCharsStr, Throwable cause ) {
        super(invalidName, cause);
        this.badCharsStr = badCharsStr;
    }


    /**
     * @param invalidName
     * @param badCharsStr
     */
    public EntityNameBadCharactersException ( String invalidName, String badCharsStr ) {
        super(invalidName);
        this.badCharsStr = badCharsStr;
    }


    /**
     * @return the badCharsStr
     */
    public String getBadCharsStr () {
        return this.badCharsStr;
    }
}
