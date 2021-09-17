/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.04.2016 by mbechler
 */
package eu.agno3.fileshare.service;


/**
 * @author mbechler
 *
 */
public enum UploadState {

    /**
     * Context was just created
     */
    INITIAL,

    /**
     * Upload has started
     */
    UPLOADING,

    /**
     * Either a chunk upload was interrupted or a timeout waiting for new chunks has occured
     */
    FAILED,

    /**
     * All chunks are present
     */
    COMPLETE,

    /**
     * All chunks are present, but there still is some processing going on
     */
    PROCESSING,
}
