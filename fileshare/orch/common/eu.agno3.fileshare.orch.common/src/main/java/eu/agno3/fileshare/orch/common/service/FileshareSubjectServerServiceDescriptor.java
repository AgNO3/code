/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.09.2013 by mbechler
 */
package eu.agno3.fileshare.orch.common.service;


import javax.xml.namespace.QName;

import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.ws.common.AbstractSOAPServiceClientDescriptor;
import eu.agno3.runtime.ws.common.SOAPServiceClientDescriptor;


/**
 * @author mbechler
 * 
 */
@Component ( service = SOAPServiceClientDescriptor.class )
public class FileshareSubjectServerServiceDescriptor extends AbstractSOAPServiceClientDescriptor<FileshareSubjectServerService> {

    /**
     */
    public FileshareSubjectServerServiceDescriptor () {
        super(FileshareSubjectServerService.class, DEFAULT_SERVICE_QNAME, "/fileshare/manage/subject"); //$NON-NLS-1$
    }

    /**
     * 
     */
    public static final String NAMESPACE = "urn:agno3:fileshare:1.0:service:subject"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String DEFAULT_SERVICE_NAME = "subjectServerService"; //$NON-NLS-1$

    /**
     * 
     */
    public static final QName DEFAULT_SERVICE_QNAME = new QName(NAMESPACE, DEFAULT_SERVICE_NAME);

}
