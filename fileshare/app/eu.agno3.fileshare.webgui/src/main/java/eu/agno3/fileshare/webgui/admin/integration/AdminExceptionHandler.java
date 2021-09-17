/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.06.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.admin.integration;


import eu.agno3.fileshare.webgui.admin.FileshareAdminExceptionHandler;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;


/**
 * @author mbechler
 *
 */
public class AdminExceptionHandler implements FileshareAdminExceptionHandler {

    /**
     * 
     */
    private static final long serialVersionUID = 4023723218932450704L;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.admin.FileshareAdminExceptionHandler#handleException(java.lang.Exception)
     */
    @Override
    public void handleException ( Exception e ) {
        ExceptionHandler.handleException(e);
    }

}
