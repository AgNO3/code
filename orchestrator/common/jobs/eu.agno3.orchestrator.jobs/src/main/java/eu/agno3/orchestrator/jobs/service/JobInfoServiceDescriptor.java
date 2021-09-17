/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.service;


import javax.xml.namespace.QName;

import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.ws.common.AbstractSOAPServiceClientDescriptor;
import eu.agno3.runtime.ws.common.SOAPServiceClientDescriptor;


/**
 * @author mbechler
 * 
 */
@Component ( service = SOAPServiceClientDescriptor.class )
public class JobInfoServiceDescriptor extends AbstractSOAPServiceClientDescriptor<JobInfoService> {

    /**
     * 
     */
    public JobInfoServiceDescriptor () {
        super(JobInfoService.class, DEFAULT_SERVICE_NAME, "/job/info"); //$NON-NLS-1$
    }

    /**
     * 
     */
    public static final String NAMESPACE = "urn:agno3:job:1.0:service"; //$NON-NLS-1$
    /**
     * 
     */
    public static final QName DEFAULT_SERVICE_NAME = new QName(NAMESPACE, "jobInfoService"); //$NON-NLS-1$


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.ws.common.SOAPServiceClientDescriptor#isEagerInitialize()
     */
    @Override
    public boolean isEagerInitialize () {
        return true;
    }
}
