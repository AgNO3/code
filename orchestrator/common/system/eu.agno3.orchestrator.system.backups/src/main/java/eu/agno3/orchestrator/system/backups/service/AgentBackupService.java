/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.backups.service;


import java.util.List;
import java.util.UUID;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import eu.agno3.orchestrator.config.model.base.exceptions.AgentCommunicationErrorException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentOfflineException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectReferentialIntegrityException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.system.backups.BackupInfo;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.xml.binding.adapter.UUIDAdapter;


/**
 * @author mbechler
 * 
 */
@WebService ( targetNamespace = AgentBackupServiceDescriptor.NAMESPACE )
public interface AgentBackupService extends SOAPWebService {

    /**
     * @param instance
     * @return available managed backups
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws ModelObjectReferentialIntegrityException
     * @throws AgentCommunicationErrorException
     * @throws AgentDetachedException
     * @throws AgentOfflineException
     */
    @WebResult ( name = "backup" )
    @XmlElementWrapper
    public List<BackupInfo> listBackups ( @WebParam ( name = "instance" ) InstanceStructuralObject instance)
            throws ModelServiceException, ModelObjectNotFoundException, ModelObjectReferentialIntegrityException, AgentCommunicationErrorException,
            AgentDetachedException, AgentOfflineException;


    /**
     * 
     * @param instance
     * @return the backup job info
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws ModelObjectReferentialIntegrityException
     * @throws AgentCommunicationErrorException
     * @throws AgentDetachedException
     * @throws AgentOfflineException
     */
    @WebResult ( name = "backupJob" )
    public JobInfo makeBackup ( @WebParam ( name = "instance" ) InstanceStructuralObject instance)
            throws ModelServiceException, ModelObjectNotFoundException, ModelObjectReferentialIntegrityException, AgentCommunicationErrorException,
            AgentDetachedException, AgentOfflineException;


    /**
     * 
     * @param instance
     * @param backupId
     * @return the restore job info
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws ModelObjectReferentialIntegrityException
     * @throws AgentCommunicationErrorException
     * @throws AgentDetachedException
     * @throws AgentOfflineException
     */
    @WebResult ( name = "restoreJob" )
    public JobInfo restoreBackup ( @WebParam ( name = "instance" ) InstanceStructuralObject instance,
            @XmlJavaTypeAdapter ( UUIDAdapter.class ) @WebParam ( name = "backupId" ) UUID backupId)
                    throws ModelServiceException, ModelObjectNotFoundException, ModelObjectReferentialIntegrityException,
                    AgentCommunicationErrorException, AgentDetachedException, AgentOfflineException;


    /**
     * 
     * @param instance
     * @param backupId
     * @return the updated backup list
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws ModelObjectReferentialIntegrityException
     * @throws AgentCommunicationErrorException
     * @throws AgentDetachedException
     * @throws AgentOfflineException
     */
    @WebResult ( name = "backup" )
    @XmlElementWrapper ( name = "backups" )
    public List<BackupInfo> removeBackup ( @WebParam ( name = "instance" ) InstanceStructuralObject instance,
            @XmlJavaTypeAdapter ( UUIDAdapter.class ) @WebParam ( name = "backupId" ) UUID backupId)
                    throws ModelServiceException, ModelObjectNotFoundException, ModelObjectReferentialIntegrityException,
                    AgentCommunicationErrorException, AgentDetachedException, AgentOfflineException;
}
