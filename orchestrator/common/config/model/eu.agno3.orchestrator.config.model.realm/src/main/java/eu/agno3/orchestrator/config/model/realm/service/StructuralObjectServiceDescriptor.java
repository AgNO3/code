/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.09.2013 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service;


import javax.xml.namespace.QName;

import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.ws.common.AbstractSOAPServiceClientDescriptor;
import eu.agno3.runtime.ws.common.SOAPServiceClientDescriptor;


/**
 * @author mbechler
 * 
 */
@Component ( service = SOAPServiceClientDescriptor.class )
public class StructuralObjectServiceDescriptor extends AbstractSOAPServiceClientDescriptor<StructuralObjectService> {

    /**
     * 
     */
    public StructuralObjectServiceDescriptor () {
        super(StructuralObjectService.class, DEFAULT_SERVICE_QNAME, "/realm/structure"); //$NON-NLS-1$
    }

    /**
     * 
     */
    public static final String NAMESPACE = "urn:agno3:model:1.0:realm:service:structure"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String DEFAULT_SERVICE_NAME = "structureService"; //$NON-NLS-1$

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
        return true;
    }
}
