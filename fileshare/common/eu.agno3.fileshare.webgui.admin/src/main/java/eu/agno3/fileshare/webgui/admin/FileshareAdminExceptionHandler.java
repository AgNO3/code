/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.06.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.admin;


import java.io.Serializable;


/**
 * @author mbechler
 *
 */
public interface FileshareAdminExceptionHandler extends Serializable {

    /**
     * @param e
     */
    void handleException ( Exception e );

}
