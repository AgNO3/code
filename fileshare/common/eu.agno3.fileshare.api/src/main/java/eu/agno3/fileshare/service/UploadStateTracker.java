/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 4, 2017 by mbechler
 */
package eu.agno3.fileshare.service;


/**
 * @author mbechler
 *
 */
public interface UploadStateTracker {

    /**
     * @return whether the context is valid
     */
    boolean isValid ();


    /**
     * @return overall current upload state
     */
    UploadState getState ();


    /**
     * @param state
     */
    void setState ( UploadState state );

}