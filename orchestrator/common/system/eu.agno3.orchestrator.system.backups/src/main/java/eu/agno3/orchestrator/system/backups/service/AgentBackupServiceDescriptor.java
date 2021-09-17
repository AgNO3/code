/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.09.2013 by mbechler
 */
package eu.agno3.orchestrator.system.backups.service;


import javax.xml.namespace.QName;

import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.ws.common.AbstractSOAPServiceClientDescriptor;
import eu.agno3.runtime.ws.common.SOAPServiceClientDescriptor;


/**
 * @author mbechler
 * 
 */
@Component ( service = SOAPServiceClientDescriptor.class )
public final class AgentBackupServiceDescriptor extends AbstractSOAPServiceClientDescriptor<AgentBackupService> {

    /**
     * 
     */
    public AgentBackupServiceDescriptor () {
        super(AgentBackupService.class, DEFAULT_SERVICE_QNAME, "/agent/backup"); //$NON-NLS-1$
    }

    /**
     * 
     */
    public static final String NAMESPACE = "urn:agno3:model:1.0:system:backup:service"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String DEFAULT_SERVICE_NAME = "agentBackupService"; //$NON-NLS-1$

    /**
     * 
     */
    public static final QName DEFAULT_SERVICE_QNAME = new QName(NAMESPACE, DEFAULT_SERVICE_NAME);


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ws.common.SOAPServiceClientDescriptor#isEagerInitialize()
     */
    @Override
    public boolean isEagerInitialize () {
        return false;
    }
}
