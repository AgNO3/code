/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.10.2015 by mbechler
 */
package eu.agno3.fileshare.model;


import org.joda.time.DateTime;


/**
 * @author mbechler
 *
 */
public interface VFSFileEntity extends VFSEntity {

    /**
     * @return the file size
     */
    long getFileSize ();


    /**
     * @param fileSize
     */
    void setFileSize ( long fileSize );


    /**
     * @return the content type
     */
    String getContentType ();


    /**
     * @param string
     */
    void setContentType ( String string );


    /**
     * @return the content encoding
     */
    String getContentEncoding ();


    /**
     * @param string
     */
    void setContentEncoding ( String string );


    /**
     * @return whether the file can be replaced
     */
    boolean canReplace ();


    /**
     * @return the content last modification time
     */
    DateTime getContentLastModified ();


    /**
     * @param lastMod
     */
    void setContentLastModified ( DateTime lastMod );

}
