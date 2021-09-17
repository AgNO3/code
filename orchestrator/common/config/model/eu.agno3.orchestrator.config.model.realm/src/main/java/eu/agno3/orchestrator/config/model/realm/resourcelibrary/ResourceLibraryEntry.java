/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.resourcelibrary;


import java.io.Serializable;


/**
 * @author mbechler
 *
 */
public class ResourceLibraryEntry implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 4796206186047319538L;
    private String path;
    private String hash;
    private String oldHash;

    private byte[] content = new byte[0];


    /**
     * @return the path
     */
    public String getPath () {
        return this.path;
    }


    /**
     * @param path
     *            the path to set
     */
    public void setPath ( String path ) {
        this.path = path;
    }


    /**
     * @return the hash
     */
    public String getHash () {
        return this.hash;
    }


    /**
     * @param hash
     *            the hash to set
     */
    public void setHash ( String hash ) {
        this.hash = hash;
    }


    /**
     * @return the content
     */
    public byte[] getContent () {
        return this.content;
    }


    /**
     * @param content
     *            the content to set
     */
    public void setContent ( byte[] content ) {
        this.content = content;
    }


    /**
     * @param string
     */
    public void setOldHash ( String string ) {
        this.oldHash = string;
    }


    /**
     * @return the oldHash
     */
    public String getOldHash () {
        return this.oldHash;
    }
}
