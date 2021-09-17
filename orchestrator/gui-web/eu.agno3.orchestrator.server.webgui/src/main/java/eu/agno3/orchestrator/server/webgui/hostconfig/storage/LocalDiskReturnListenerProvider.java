/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.02.2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.hostconfig.storage;


import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import eu.agno3.orchestrator.server.webgui.components.OuterWrapper;


/**
 * 
 * 
 * This is quite a dirty hack, the select listener does not get called with the proper context
 * 
 * @author mbechler
 *
 */
@RequestScoped
@Named ( "localDiskReturnListenerProvider" )
public class LocalDiskReturnListenerProvider {

    private LocalDiskReturnListener wrapper;


    public LocalDiskReturnListener makeLocalReturnListener ( OuterWrapper<?> wr ) {
        if ( this.wrapper == null ) {
            this.wrapper = new LocalDiskReturnListener(wr);
        }
        return this.wrapper;
    }
}
