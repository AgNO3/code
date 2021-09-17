/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.logging.service;


import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.runtime.eventlog.EventFilter;
import eu.agno3.runtime.ws.common.SOAPWebService;


/**
 * @author mbechler
 * 
 */
@WebService ( targetNamespace = LoggingServiceDescriptor.NAMESPACE )
public interface LoggingService extends SOAPWebService {

    /**
     * 
     * @param anchor
     * @param filter
     * @param startTime
     * @param offset
     * @param pageSize
     * @return the service runtime status
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    @WebResult ( name = "logEntries" )
    List<String> list ( @WebParam ( name = "anchor" ) StructuralObject anchor, @WebParam ( name = "filter" ) EventFilter filter, long startTime,
            int offset, int pageSize) throws ModelServiceException, ModelObjectNotFoundException;


    /**
     * @param anchor
     * @param filter
     * @param startTime
     * @return the number of log entries
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    long count ( @WebParam ( name = "anchor" ) StructuralObject anchor, @WebParam ( name = "filter" ) EventFilter filter, long startTime)
            throws ModelObjectNotFoundException, ModelServiceException;


    /**
     * @param selectedObject
     * @param id
     * @return the event with the given id
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    String getById ( StructuralObject selectedObject, String id ) throws ModelObjectNotFoundException, ModelServiceException;

}
