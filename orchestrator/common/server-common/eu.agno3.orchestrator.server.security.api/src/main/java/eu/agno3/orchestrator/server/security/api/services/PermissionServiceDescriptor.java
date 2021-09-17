/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.10.2014 by mbechler
 */
package eu.agno3.orchestrator.server.security.api.services;


import javax.xml.namespace.QName;

import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.ws.common.AbstractSOAPServiceClientDescriptor;
import eu.agno3.runtime.ws.common.SOAPServiceClientDescriptor;


/**
 * @author mbechler
 *
 */
@Component ( service = SOAPServiceClientDescriptor.class )
public class PermissionServiceDescriptor extends AbstractSOAPServiceClientDescriptor<PermissionService> {

    /**
     * 
     */
    public PermissionServiceDescriptor () {
        super(PermissionService.class, DEFAULT_SERVICE_NAME, "/security/permission"); //$NON-NLS-1$
    }

    /**
     * 
     */
    public static final String NAMESPACE = "urn:agno3:security:1.0:permissionService"; //$NON-NLS-1$
    /**
     * 
     */
    public static final QName DEFAULT_SERVICE_NAME = new QName(NAMESPACE, "permission"); //$NON-NLS-1$
}
