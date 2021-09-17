/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2015 by mbechler
 */
package eu.agno3.fileshare.security.internal;


import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.security.AccessControlService;
import eu.agno3.fileshare.security.PermissionsService;
import eu.agno3.runtime.security.db.impl.AbstractPermissionService;


/**
 * @author mbechler
 *
 */
@Component ( service = PermissionsService.class )
public class PermissionsServiceImpl extends AbstractPermissionService implements PermissionsService {

    private AccessControlService accessControl;


    @Reference
    protected synchronized void setAccessControlService ( AccessControlService acs ) {
        this.accessControl = acs;
    }


    protected synchronized void unsetAccessControlService ( AccessControlService acs ) {
        if ( this.accessControl == acs ) {
            this.accessControl = null;
        }
    }
}
