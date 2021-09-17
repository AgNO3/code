/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jan 19, 2017 by mbechler
 */
package eu.agno3.orchestrator.realms.service;


import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.realms.RealmLookupResult;
import eu.agno3.runtime.ws.common.SOAPWebService;


/**
 * @author mbechler
 *
 */
@WebService ( targetNamespace = RealmLookupServiceDescriptor.NAMESPACE )
public interface RealmLookupService extends SOAPWebService {

    /**
     * 
     * @param realm
     * @param sid
     * @return lookup result, null if not found
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    @WebMethod ( action = "lookupSID" )
    @WebResult ( name = "result" )
    RealmLookupResult getNameForSID ( @WebParam ( name = "realm" ) String realm, @WebParam ( name = "sid" ) String sid)
            throws ModelObjectNotFoundException, ModelServiceException;


    /**
     * @param domain
     * @param filter
     * @return search results
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */

    @WebMethod ( action = "search" )
    @WebResult ( name = "results" )
    @XmlElementWrapper ( name = "results", required = true )
    @XmlElement ( name = "result", required = false )
    List<RealmLookupResult> search ( String domain, String filter ) throws ModelObjectNotFoundException, ModelServiceException;


    /**
     * @param origin
     * @param name
     * @return domain by name
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    List<RealmLookupResult> lookupDomainByName ( String origin, String name ) throws ModelObjectNotFoundException, ModelServiceException;


    /**
     * @param origin
     * @param sid
     * @return domain by SID
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    RealmLookupResult lookupDomainSID ( String origin, String sid ) throws ModelObjectNotFoundException, ModelServiceException;
}
