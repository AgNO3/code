/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.05.2015 by mbechler
 */
package eu.agno3.fileshare.model;


/**
 * @author mbechler
 *
 */
public interface LinkShareData {

    /**
     * @return the hideSensitive
     */
    boolean getHideSensitive ();


    /**
     * 
     * @return whether the link is previewable
     */
    boolean getViewable ();


    /**
     * 
     * @param viewable
     */
    void setViewable ( boolean viewable );


    /**
     * 
     * @return the download URL
     */
    String getDownloadURL ();


    /**
     * 
     * @param downloadURL
     */
    void setDownloadURL ( String downloadURL );


    /**
     * 
     * @return the view URL
     */
    String getViewURL ();


    /**
     * 
     * @param viewURL
     */
    void setViewURL ( String viewURL );

}
