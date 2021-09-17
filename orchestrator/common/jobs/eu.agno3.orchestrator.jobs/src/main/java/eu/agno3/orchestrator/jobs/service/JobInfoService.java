/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.service;


import java.util.List;
import java.util.UUID;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import eu.agno3.orchestrator.jobs.JobStatusInfo;
import eu.agno3.orchestrator.jobs.exceptions.JobQueueException;
import eu.agno3.orchestrator.jobs.exceptions.JobUnknownException;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.xml.binding.adapter.UUIDAdapter;


/**
 * @author mbechler
 * 
 */
@WebService ( targetNamespace = JobInfoServiceDescriptor.NAMESPACE )
public interface JobInfoService extends SOAPWebService {

    /**
     * 
     * @param max
     * @return a list of all known jobs
     * @throws JobQueueException
     */
    @WebMethod ( action = "listAllJobs" )
    @WebResult ( name = "jobs" )
    @XmlElementWrapper ( name = "jobs", required = true )
    @XmlElement ( name = "job", required = false )
    List<JobStatusInfo> listJobs ( @WebParam ( name = "max" ) int max) throws JobQueueException;


    /**
     * @param jobId
     * @return the info about the given job
     * @throws JobUnknownException
     */
    @WebMethod ( action = "getJobInfo" )
    @WebResult ( name = "job" )
    JobStatusInfo getJobInfo ( @WebParam ( name = "jobId" ) @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID jobId) throws JobUnknownException;


    /**
     * @param jobId
     * @throws JobUnknownException
     */
    @WebMethod ( action = "cancelJob" )
    void cancelJob ( @WebParam ( name = "jobId" ) @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID jobId) throws JobUnknownException;


    /**
     * @param jobId
     * @param off
     *            offset from which to start output, if negative offset from the end
     * @return job output buffer
     * @throws JobUnknownException
     */
    @WebMethod ( action = "getJobOutput" )
    String getJobOutput ( @WebParam ( name = "jobId" ) @XmlJavaTypeAdapter ( value = UUIDAdapter.class ) UUID jobId, long off)
            throws JobUnknownException;

}
