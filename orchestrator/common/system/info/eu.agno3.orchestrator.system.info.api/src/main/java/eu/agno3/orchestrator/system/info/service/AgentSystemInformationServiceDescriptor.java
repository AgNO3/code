/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.09.2013 by mbechler
 */
package eu.agno3.orchestrator.system.info.service;


import javax.xml.namespace.QName;

import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.ws.common.AbstractSOAPServiceClientDescriptor;
import eu.agno3.runtime.ws.common.SOAPServiceClientDescriptor;


/**
 * @author mbechler
 * 
 */
@Component ( service = SOAPServiceClientDescriptor.class )
public final class AgentSystemInformationServiceDescriptor extends AbstractSOAPServiceClientDescriptor<AgentSystemInformationService> {

    /**
     * 
     */
    public AgentSystemInformationServiceDescriptor () {
        super(AgentSystemInformationService.class, DEFAULT_SERVICE_NAME, "/agent/sysInfo"); //$NON-NLS-1$
    }

    /**
     * 
     */
    public static final String NAMESPACE = "urn:agno3:model:1.0:system:info:service"; //$NON-NLS-1$
    /**
     * 
     */
    public static final QName DEFAULT_SERVICE_NAME = new QName(NAMESPACE, "agentSysInfoService"); //$NON-NLS-1$


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
