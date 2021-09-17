/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 14, 2016 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service;


import java.util.Set;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlAttachmentRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.cxf.annotations.EndpointProperty;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectConflictException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.license.LicenseInfo;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.xml.binding.adapter.UUIDAdapter;


/**
 * @author mbechler
 *
 */
@WebService ( targetNamespace = LicensingServiceDescriptor.NAMESPACE )
@EndpointProperty ( key = "motm-enabled", value = "true" )
public interface LicensingService extends SOAPWebService {

    /**
     * Add a license to the pool
     * 
     * @param anchor
     * @param data
     * @return parsed license data
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws ModelObjectConflictException
     */
    @WebResult ( name = "licenseInfo" )
    LicenseInfo addLicense ( @WebParam ( name = "anchor" ) StructuralObject anchor,
            @XmlAttachmentRef @XmlMimeType ( "application/octet-stream" ) DataHandler data)
                    throws ModelObjectNotFoundException, ModelServiceException, ModelObjectConflictException;


    /**
     * Remove a license from the pool
     * 
     * @param anchor
     * @param licenseId
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    void removeLicense ( @WebParam ( name = "anchor" ) StructuralObject anchor,
            @WebParam ( name = "licenseId" ) @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID licenseId)
                    throws ModelObjectNotFoundException, ModelServiceException;


    /**
     * Get license details
     * 
     * @param licenseId
     * @return info on the specified license
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    @WebResult ( name = "licenseInfo" )
    LicenseInfo getLicense ( @WebParam ( name = "licenseId" ) @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID licenseId)
            throws ModelObjectNotFoundException, ModelServiceException;


    /**
     * @param is
     * @return the license assignd to the host, null if none
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    @WebResult ( name = "licenseInfo" )
    LicenseInfo getAssignedLicense ( @WebParam ( name = "to" ) InstanceStructuralObject is)
            throws ModelObjectNotFoundException, ModelServiceException;


    /**
     * Assign a license to a host
     * 
     * @param licenseId
     * @param to
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    void assignLicense ( @WebParam ( name = "licenseId" ) @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID licenseId,
            @WebParam ( name = "to" ) InstanceStructuralObject to) throws ModelObjectNotFoundException, ModelServiceException;


    /**
     * 
     * @param anchor
     * @param includeInherited
     * @return licenses in the pool of the anchor object
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    @WebResult ( name = "licenses" )
    @XmlElementWrapper
    Set<LicenseInfo> getLicensesAt ( @WebParam ( name = "anchor" ) StructuralObject anchor,
            @WebParam ( name = "includeInherited" ) boolean includeInherited) throws ModelObjectNotFoundException, ModelServiceException;


    /**
     * 
     * @param anchor
     * @return free licenses available at the anchor that qualify for the instance
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    @WebResult ( name = "licenses" )
    @XmlElementWrapper
    Set<LicenseInfo> getApplicableLicenses ( @WebParam ( name = "anchor" ) InstanceStructuralObject anchor)
            throws ModelObjectNotFoundException, ModelServiceException;

}
